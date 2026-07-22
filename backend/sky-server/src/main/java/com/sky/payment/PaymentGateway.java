package com.sky.payment;

import com.sky.entity.Orders;
import com.sky.vo.OrderPaymentVO;

public interface PaymentGateway {

    OrderPaymentVO pay(Orders order);

    void refund(Orders order);

    boolean confirmsImmediately();
}
