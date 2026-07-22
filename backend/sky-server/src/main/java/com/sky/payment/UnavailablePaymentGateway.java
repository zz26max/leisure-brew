package com.sky.payment;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.vo.OrderPaymentVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sky.payment.simulation-enabled", havingValue = "false", matchIfMissing = true)
public class UnavailablePaymentGateway implements PaymentGateway {

    @Override
    public OrderPaymentVO pay(Orders order) {
        throw unavailable();
    }

    @Override
    public void refund(Orders order) {
        throw unavailable();
    }

    @Override
    public boolean confirmsImmediately() {
        return false;
    }

    private OrderBusinessException unavailable() {
        return new OrderBusinessException(MessageConstant.PAYMENT_CHANNEL_UNAVAILABLE);
    }
}
