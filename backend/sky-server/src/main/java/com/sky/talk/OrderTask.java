package com.sky.talk;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 每分钟自动处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * *")
    public void processTimeoutTask() {
        log.info("自动处理超时支付订单{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        //查询超时订单
        List<Orders> ordersList = orderMapper.getOrders(Orders.PENDING_PAYMENT, time);
        //修改订单状态
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }

    }

    /**
     * 每天自动处理未完成订单
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void processDeliveryOrder(){
        log.info("每天1点处理未完成（派送中）的订单{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        //查询订单
        List<Orders> ordersList = orderMapper.getOrders(Orders.DELIVERY_IN_PROGRESS, time);
        //修改状态
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
