-- ============================================================
-- 清理 OrderDataGenerator 生成的测试数据
-- 执行前先确认：SELECT MAX(id) FROM orders;
-- 如果 max(id) > 1,000,013（即原始数据 13 条 + 100 万），请调整范围
-- ============================================================

-- 1. 删除生成的订单明细（约 400 万条）
--    生成器插入的 order_id 范围：14 ~ 1,000,013
DELETE FROM order_detail WHERE order_id >= 14;

-- 2. 删除生成的订单（100 万条）
DELETE FROM orders WHERE id >= 14;

-- 3. 删除生成的测试用户
DELETE FROM user WHERE id >= 5;

-- 4. 重置自增 ID（可选，让后续插入从原始位置开始）
ALTER TABLE orders AUTO_INCREMENT = 14;
ALTER TABLE order_detail AUTO_INCREMENT = 32;
ALTER TABLE user AUTO_INCREMENT = 5;
