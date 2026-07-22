package com.sky.talk;

import com.sky.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderTask {
    private final OrderMapper orderMapper;

    /**
     * 每分钟自动处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * *")
    public void processTimeoutTask() {
        LocalDateTime now = LocalDateTime.now();
        int count = orderMapper.cancelTimedOutOrders(now.minusMinutes(15), now);
        if (count > 0) {
            log.info("已自动取消 {} 个超时未支付订单", count);
        }
    }

    /**
     * 每天自动处理未完成订单
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void processDeliveryOrder(){
        LocalDateTime now = LocalDateTime.now();
        int count = orderMapper.completeStaleDeliveries(now.minusHours(1), now);
        if (count > 0) {
            log.info("已自动完成 {} 个长时间派送中的订单", count);
        }
    }
}
