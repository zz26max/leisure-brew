package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.Combo;
import com.sky.entity.Drink;
import com.sky.entity.Message;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.ComboMapper;
import com.sky.mapper.DrinkMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.payment.PaymentGateway;
import com.sky.result.PageResult;
import com.sky.service.MessageService;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal DELIVERY_FEE = new BigDecimal("6.00");
    private static final BigDecimal PACKAGING_FEE_PER_ITEM = BigDecimal.ONE;
    private static final DateTimeFormatter ORDER_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final AddressBookMapper addressBookMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final DrinkMapper drinkMapper;
    private final ComboMapper comboMapper;
    private final WebSocketServer webSocketServer;
    private final MessageService messageService;
    private final PaymentGateway paymentGateway;

    @Override
    @Transactional
    public OrderSubmitVO orderSubmit(OrdersSubmitDTO submit) {
        if (submit == null || submit.getAddressBookId() == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = currentUserId();
        AddressBook address = addressBookMapper.getByIdAndUserId(submit.getAddressBookId(), userId);
        if (address == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }

        List<ShoppingCart> cart = shoppingCartMapper.listByUserIdForUpdate(userId);
        if (cart.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Pricing pricing = priceCart(cart);
        Orders order = new Orders();
        BeanUtils.copyProperties(submit, order, "amount", "packAmount");
        order.setNumber(newOrderNumber());
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setAmount(pricing.total);
        order.setPackAmount(pricing.itemCount);
        order.setPhone(address.getPhone());
        order.setConsignee(address.getConsignee());
        order.setAddress(fullAddress(address));
        orderMapper.insert(order);

        List<OrderDetail> details = cart.stream()
                .map(item -> toOrderDetail(item, order.getId()))
                .collect(Collectors.toList());
        orderDetailMapper.insertEach(details);
        shoppingCartMapper.cleanByUserId(userId);

        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderTime(order.getOrderTime())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .build();
    }

    @Override
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO payment) {
        if (payment == null || payment.getOrderNumber() == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_OWNED);
        }

        Orders order = orderMapper.getByNumberAndUserId(payment.getOrderNumber(), currentUserId());
        requirePayable(order);
        validatePayMethod(payment.getPayMethod());
        order.setPayMethod(payment.getPayMethod());

        OrderPaymentVO response = paymentGateway.pay(order);
        if (paymentGateway.confirmsImmediately()) {
            markPaid(order, payment.getPayMethod());
        }
        return response;
    }

    @Override
    @Transactional
    public void paySuccess(String orderNumber) {
        Orders order = orderMapper.getByNumber(orderNumber);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (Orders.PAID.equals(order.getPayStatus())) {
            return;
        }
        requirePayable(order);
        markPaid(order, order.getPayMethod() == null ? 1 : order.getPayMethod());
    }

    @Override
    public PageResult pageQuery(int pageNum, int pageSize, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        OrdersPageQueryDTO query = new OrdersPageQueryDTO();
        query.setUserId(currentUserId());
        query.setStatus(status);
        Page<Orders> page = orderMapper.pageQuery(query);

        List<OrderVO> records = page.stream()
                .map(this::toOrderVO)
                .collect(Collectors.toList());
        return new PageResult(page.getTotal(), records);
    }

    @Override
    public OrderVO getById(Long id) {
        return toOrderVO(requireUserOrder(id));
    }

    @Override
    @Transactional
    public void repetition(Long id) {
        Orders order = requireUserOrder(id);
        List<OrderDetail> details = orderDetailMapper.getByOrderId(order.getId());
        if (details.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Long userId = currentUserId();
        LocalDateTime now = LocalDateTime.now();
        List<ShoppingCart> cart = details.stream().map(detail -> {
            ShoppingCart item = new ShoppingCart();
            BeanUtils.copyProperties(detail, item, "id");
            item.setUserId(userId);
            item.setCreateTime(now);
            refreshCartLine(item);
            return item;
        }).collect(Collectors.toList());
        shoppingCartMapper.insertBatch(cart);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(query);
        return new PageResult(page.getTotal(), getOrderVOList(page));
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO statistics = new OrderStatisticsVO();
        statistics.setToBeConfirmed(orderMapper.countStatus(Orders.TO_BE_CONFIRMED));
        statistics.setConfirmed(orderMapper.countStatus(Orders.CONFIRMED));
        statistics.setDeliveryInProgress(orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS));
        return statistics;
    }

    @Override
    public OrderVO details(Long id) {
        Orders order = requireOrder(id);
        return toOrderVO(order);
    }

    @Override
    public void confirm(OrdersConfirmDTO confirmation) {
        Orders current = requireOrder(confirmation.getId());
        requireStatus(current, Orders.TO_BE_CONFIRMED);
        updateStatus(current.getId(), Orders.CONFIRMED);
    }

    @Override
    @Transactional
    public void rejection(OrdersRejectionDTO rejection) {
        Orders current = requireOrder(rejection.getId());
        requireStatus(current, Orders.TO_BE_CONFIRMED);

        Orders update = cancelledOrder(current.getId());
        update.setRejectionReason(rejection.getRejectionReason());
        if (Orders.PAID.equals(current.getPayStatus())) {
            paymentGateway.refund(current);
            update.setPayStatus(Orders.REFUND);
        }
        orderMapper.update(update);
    }

    @Override
    @Transactional
    public void cancel(OrdersCancelDTO cancellation) {
        Orders current = requireOrder(cancellation.getId());
        if (Orders.COMPLETED.equals(current.getStatus()) || Orders.CANCELLED.equals(current.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders update = cancelledOrder(current.getId());
        update.setCancelReason(cancellation.getCancelReason());
        if (Orders.PAID.equals(current.getPayStatus())) {
            paymentGateway.refund(current);
            update.setPayStatus(Orders.REFUND);
        }
        orderMapper.update(update);
    }

    @Override
    public void delivery(Long id) {
        Orders current = requireOrder(id);
        requireStatus(current, Orders.CONFIRMED);
        updateStatus(id, Orders.DELIVERY_IN_PROGRESS);
    }

    @Override
    public void complete(Long id) {
        Orders current = requireOrder(id);
        requireStatus(current, Orders.DELIVERY_IN_PROGRESS);

        Orders update = new Orders();
        update.setId(id);
        update.setStatus(Orders.COMPLETED);
        update.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(update);
    }

    @Override
    @Transactional
    public void userCancel(Long id) {
        Orders current = requireUserOrder(id);
        if (current.getStatus() > Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders update = cancelledOrder(id);
        update.setCancelReason("用户取消");
        if (Orders.PAID.equals(current.getPayStatus())) {
            paymentGateway.refund(current);
            update.setPayStatus(Orders.REFUND);
        }
        orderMapper.update(update);
    }

    @Override
    @Transactional
    public void reminder(Long id) {
        Orders order = requireUserOrder(id);
        Integer status = order.getStatus();
        if (status < Orders.TO_BE_CONFIRMED || status > Orders.DELIVERY_IN_PROGRESS) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Message message = reminderMessage(order);
        messageService.save(message);
        webSocketServer.sendToAllClient(JSONObject.toJSONString(reminderPayload(order)));
    }

    private Pricing priceCart(List<ShoppingCart> cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;
        for (ShoppingCart item : cart) {
            if (item.getNumber() == null || item.getNumber() <= 0) {
                throw new OrderBusinessException(MessageConstant.ITEM_UNAVAILABLE);
            }
            refreshCartLine(item);
            itemCount = Math.addExact(itemCount, item.getNumber());
            subtotal = subtotal.add(item.getAmount().multiply(BigDecimal.valueOf(item.getNumber())));
        }

        BigDecimal packaging = PACKAGING_FEE_PER_ITEM.multiply(BigDecimal.valueOf(itemCount));
        return new Pricing(itemCount, subtotal.add(packaging).add(DELIVERY_FEE));
    }

    private void refreshCartLine(ShoppingCart item) {
        boolean hasDrink = item.getDishId() != null;
        boolean hasCombo = item.getSetmealId() != null;
        if (hasDrink == hasCombo) {
            throw new OrderBusinessException(MessageConstant.ITEM_UNAVAILABLE);
        }

        if (hasDrink) {
            Drink drink = drinkMapper.getById(item.getDishId());
            if (drink == null || !StatusConstant.ENABLE.equals(drink.getStatus())) {
                throw new OrderBusinessException(MessageConstant.ITEM_UNAVAILABLE);
            }
            item.setName(drink.getName());
            item.setImage(drink.getImage());
            item.setAmount(drink.getPrice());
            return;
        }

        Combo combo = comboMapper.getById(item.getSetmealId());
        if (combo == null || !StatusConstant.ENABLE.equals(combo.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ITEM_UNAVAILABLE);
        }
        item.setName(combo.getName());
        item.setImage(combo.getImage());
        item.setAmount(combo.getPrice());
    }

    private void markPaid(Orders order, Integer payMethod) {
        int updated = orderMapper.markPaid(order.getId(), payMethod, LocalDateTime.now());
        if (updated == 0) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        notifyNewOrder(order);
    }

    private void notifyNewOrder(Orders order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", 1);
        payload.put("orderId", order.getId());
        payload.put("content", "订单号：" + order.getNumber());
        webSocketServer.sendToAllClient(JSONObject.toJSONString(payload));
    }

    private List<OrderVO> getOrderVOList(List<Orders> orders) {
        return orders.stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            vo.setOrderDishes(orderDetailMapper.getByOrderId(order.getId()).stream()
                    .map(item -> item.getName() + "*" + item.getNumber())
                    .collect(Collectors.joining(";")));
            return vo;
        }).collect(Collectors.toList());
    }

    private OrderVO toOrderVO(Orders order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setOrderDetailList(orderDetailMapper.getByOrderId(order.getId()));
        return vo;
    }

    private OrderDetail toOrderDetail(ShoppingCart cart, Long orderId) {
        OrderDetail detail = new OrderDetail();
        BeanUtils.copyProperties(cart, detail, "id");
        detail.setOrderId(orderId);
        return detail;
    }

    private Orders requireUserOrder(Long id) {
        Orders order = id == null ? null : orderMapper.getByIdAndUserId(id, currentUserId());
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_OWNED);
        }
        return order;
    }

    private Orders requireOrder(Long id) {
        Orders order = id == null ? null : orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        return order;
    }

    private void requirePayable(Orders order) {
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_OWNED);
        }
        if (!Orders.PENDING_PAYMENT.equals(order.getStatus()) || !Orders.UN_PAID.equals(order.getPayStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    }

    private void requireStatus(Orders order, Integer expected) {
        if (!expected.equals(order.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    }

    private void validatePayMethod(Integer payMethod) {
        if (!Integer.valueOf(1).equals(payMethod) && !Integer.valueOf(2).equals(payMethod)) {
            throw new OrderBusinessException("不支持的支付方式");
        }
    }

    private void updateStatus(Long id, Integer status) {
        Orders update = new Orders();
        update.setId(id);
        update.setStatus(status);
        orderMapper.update(update);
    }

    private Orders cancelledOrder(Long id) {
        Orders update = new Orders();
        update.setId(id);
        update.setStatus(Orders.CANCELLED);
        update.setCancelTime(LocalDateTime.now());
        return update;
    }

    private Message reminderMessage(Orders order) {
        Message message = new Message();
        message.setType(4);
        message.setContent("订单号：" + order.getNumber() + " 客户催单");
        message.setOrderId(order.getId());
        message.setStatus(1);
        message.setDetails(JSONObject.toJSONString(reminderDetails(order)));
        message.setCreateTime(LocalDateTime.now());
        return message;
    }

    private Map<String, Object> reminderPayload(Orders order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", 2);
        payload.put("orderId", order.getId());
        payload.put("content", "订单号：" + order.getNumber());
        return payload;
    }

    private Map<String, Object> reminderDetails(Orders order) {
        Map<String, Object> details = new HashMap<>();
        details.put("orderTime", order.getOrderTime());
        details.put("estimatedDeliveryTime", order.getEstimatedDeliveryTime());
        details.put("consignee", order.getConsignee());
        details.put("phone", order.getPhone());
        details.put("address", order.getAddress());
        String items = orderDetailMapper.getByOrderId(order.getId()).stream()
                .map(item -> item.getName() + " x " + item.getNumber())
                .collect(Collectors.joining("，"));
        details.put("orderDishes", items);
        return details;
    }

    private String fullAddress(AddressBook address) {
        return Stream.of(address.getProvinceName(), address.getCityName(), address.getDistrictName(), address.getDetail())
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }

    private String newOrderNumber() {
        int suffix = ThreadLocalRandom.current().nextInt(100, 1000);
        return LocalDateTime.now().format(ORDER_NUMBER_FORMAT) + suffix;
    }

    private Long currentUserId() {
        return BaseContext.getCurrentId();
    }

    private static final class Pricing {
        private final int itemCount;
        private final BigDecimal total;

        private Pricing(int itemCount, BigDecimal total) {
            this.itemCount = itemCount;
            this.total = total;
        }
    }
}
