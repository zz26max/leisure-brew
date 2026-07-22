USE meal_buddy;

-- 订单号定位、状态筛选、用户历史与时间范围查询。
ALTER TABLE orders ADD INDEX idx_orders_number (number);
ALTER TABLE orders ADD INDEX idx_orders_status (status);
ALTER TABLE orders ADD INDEX idx_orders_user_time (user_id, order_time);
ALTER TABLE orders ADD INDEX idx_orders_status_time (status, order_time);
ALTER TABLE orders ADD INDEX idx_orders_order_time (order_time);

-- 订单详情按订单回查。
ALTER TABLE order_detail ADD INDEX idx_detail_order_id (order_id);
