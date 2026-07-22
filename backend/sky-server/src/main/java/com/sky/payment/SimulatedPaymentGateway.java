package com.sky.payment;

import com.sky.entity.Orders;
import com.sky.vo.OrderPaymentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sky.payment.simulation-enabled", havingValue = "true")
@Slf4j
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public OrderPaymentVO pay(Orders order) {
        log.info("模拟支付完成，orderId={}", order.getId());
        return OrderPaymentVO.builder()
                .packageStr("SIMULATED_PAYMENT")
                .build();
    }

    @Override
    public void refund(Orders order) {
        log.info("模拟退款完成，orderId={}", order.getId());
    }

    @Override
    public boolean confirmsImmediately() {
        return true;
    }
}
