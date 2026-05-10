-- =============================================
-- 饭搭子 - 奶茶咖啡售卖平台种子数据
-- 使用方法：在 meal_buddy 数据库执行此脚本
-- =============================================

USE meal_buddy;

-- 1. 清理旧数据
DELETE FROM setmeal_dish;
DELETE FROM dish_flavor;
DELETE FROM shopping_cart;
DELETE FROM order_detail;
DELETE FROM orders;
DELETE FROM setmeal;
DELETE FROM dish;
DELETE FROM category;

-- 2. 重置自增ID（可选）
ALTER TABLE category AUTO_INCREMENT = 1;
ALTER TABLE dish AUTO_INCREMENT = 1;
ALTER TABLE dish_flavor AUTO_INCREMENT = 1;
ALTER TABLE setmeal AUTO_INCREMENT = 1;
ALTER TABLE setmeal_dish AUTO_INCREMENT = 1;

-- =============================================
-- 3. 分类数据
-- =============================================

-- 饮品分类 (type=1)
INSERT INTO category (id, type, name, sort, status, create_time, update_time, create_user, update_user) VALUES
(1, 1, '经典奶茶', 1, 1, NOW(), NOW(), 1, 1),
(2, 1, '鲜果茶', 2, 1, NOW(), NOW(), 1, 1),
(3, 1, '醇香咖啡', 3, 1, NOW(), NOW(), 1, 1),
(4, 1, '清爽特饮', 4, 1, NOW(), NOW(), 1, 1),
(5, 1, '小食甜品', 5, 1, NOW(), NOW(), 1, 1);

-- 套餐分类 (type=2)
INSERT INTO category (id, type, name, sort, status, create_time, update_time, create_user, update_user) VALUES
(10, 2, '超值单人餐', 1, 1, NOW(), NOW(), 1, 1),
(11, 2, '下午茶双人餐', 2, 1, NOW(), NOW(), 1, 1);

-- =============================================
-- 4. 饮品数据
-- =============================================

-- 经典奶茶 (category_id=1)
INSERT INTO dish (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) VALUES
(1, '珍珠奶茶', 1, 12.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/41bfcacf-7ad4-4927-8b26-df366553a94c.png', '经典原味奶茶搭配Q弹黑珍珠，回味无穷', 1, NOW(), NOW(), 1, 1),
(2, '黑糖啵啵奶茶', 1, 15.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/4451d4be-89a2-4939-9c69-3a87151cb979.png', '浓郁黑糖挂壁，搭配Q弹珍珠，甜蜜满分', 1, NOW(), NOW(), 1, 1),
(3, '红豆布丁奶茶', 1, 14.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/bf8cbfc1-04d2-40e8-9826-061ee41ab87c.png', '绵密红豆配嫩滑布丁，料足味浓', 1, NOW(), NOW(), 1, 1),
(4, '芋泥波波奶茶', 1, 16.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/76752350-2121-44d2-b477-10791c23a8ec.png', '软糯芋泥搭配Q弹脆啵啵，口感丰富', 1, NOW(), NOW(), 1, 1),
(5, '芝士奶盖奶茶', 1, 18.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/475cc599-8661-4899-8f9e-121dd8ef7d02.png', '醇厚芝士奶盖邂逅丝滑奶茶，咸甜交织', 1, NOW(), NOW(), 1, 1);

-- 鲜果茶 (category_id=2)
INSERT INTO dish (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) VALUES
(6, '满杯百香果', 2, 16.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/4a9cefba-6a74-467e-9fde-6e687ea725d7.png', '新鲜百香果搭配茉莉绿茶底，满杯维C', 1, NOW(), NOW(), 1, 1),
(7, '葡萄冰茶', 2, 18.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/5260ff39-986c-4a97-8850-2ec8c7583efc.png', '颗颗手剥葡萄果肉，冰爽解暑', 1, NOW(), NOW(), 1, 1),
(8, '芒果冰茶', 2, 16.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/a6953d5a-4c18-4b30-9319-4926ee77261f.png', '热带芒果清香四溢，夏日必点', 1, NOW(), NOW(), 1, 1),
(9, '柠檬绿茶', 2, 12.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/3613d38e-5614-41c2-90ed-ff175bf50716.png', '鲜切柠檬搭配茉莉绿茶，清爽回甘', 1, NOW(), NOW(), 1, 1),
(10, '莓莓果茶', 2, 20.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/4879ed66-3860-4b28-ba14-306ac025fdec.png', '草莓与蓝莓的双重暴击，酸甜好滋味', 1, NOW(), NOW(), 1, 1);

-- 醇香咖啡 (category_id=3)
INSERT INTO dish (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) VALUES
(11, '美式咖啡', 3, 15.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/e9ec4ba4-4b22-4fc8-9be0-4946e6aeb937.png', '精选阿拉比卡豆，醇厚纯粹，唤醒每一天', 1, NOW(), NOW(), 1, 1),
(12, '拿铁咖啡', 3, 18.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/22f59feb-0d44-430e-a6cd-6a49f27453ca.png', '丝滑牛奶邂逅浓缩咖啡，温柔醇香', 1, NOW(), NOW(), 1, 1),
(13, '卡布奇诺', 3, 18.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/c18b5c67-3b71-466c-a75a-e63c6449f21c.png', '绵密奶泡覆盖醇厚咖啡，一口入魂', 1, NOW(), NOW(), 1, 1),
(14, '摩卡咖啡', 3, 22.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/a80a4b8c-c93e-4f43-ac8a-856b0d5cc451.png', '巧克力与咖啡的完美融合，甜蜜能量', 1, NOW(), NOW(), 1, 1),
(15, '生椰拿铁', 3, 20.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/6080b118-e30a-4577-aab4-45042e3f88be.png', '清甜椰乳搭配醇香咖啡，热带风情', 1, NOW(), NOW(), 1, 1);

-- 清爽特饮 (category_id=4)
INSERT INTO dish (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) VALUES
(16, '茉莉绿茶', 4, 10.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/13da832f-ef2c-484d-8370-5934a1045a06.png', '清香茉莉，淡雅回甘，纯粹茶味', 1, NOW(), NOW(), 1, 1),
(17, '柠檬气泡水', 4, 12.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/7694a5d8-7938-4e9d-8b9e-2075983a2e38.png', '清凉气泡搭配鲜切柠檬，夏日解暑神器', 1, NOW(), NOW(), 1, 1),
(18, '酸奶紫米露', 4, 15.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/f5ac8455-4793-450c-97ba-173795c34626.png', '酸甜酸奶配上软糯紫米，健康又好喝', 1, NOW(), NOW(), 1, 1);

-- 小食甜品 (category_id=5)
INSERT INTO dish (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) VALUES
(19, '提拉米苏', 5, 22.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/7a55b845-1f2b-41fa-9486-76d187ee9ee1.png', '经典意式甜点，可可与奶油的完美邂逅', 1, NOW(), NOW(), 1, 1),
(20, '抹茶蛋糕', 5, 18.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png', '日式抹茶搭配松软蛋糕，清新微甜', 1, NOW(), NOW(), 1, 1),
(21, '鸡蛋仔', 5, 12.00, 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png', '现烤港式鸡蛋仔，外酥里嫩，蛋香浓郁', 1, NOW(), NOW(), 1, 1);

-- =============================================
-- 5. 规格数据 (dish_flavor)
-- =============================================

-- 杯型 (所有饮品通用)
INSERT INTO dish_flavor (id, dish_id, name, value) VALUES
(1, 1, '杯型', '["中杯","大杯","超大杯"]'),
(2, 2, '杯型', '["中杯","大杯","超大杯"]'),
(3, 3, '杯型', '["中杯","大杯","超大杯"]'),
(4, 4, '杯型', '["中杯","大杯","超大杯"]'),
(5, 5, '杯型', '["中杯","大杯","超大杯"]'),
(6, 6, '杯型', '["中杯","大杯","超大杯"]'),
(7, 7, '杯型', '["中杯","大杯","超大杯"]'),
(8, 8, '杯型', '["中杯","大杯","超大杯"]'),
(9, 9, '杯型', '["中杯","大杯","超大杯"]'),
(10, 10, '杯型', '["中杯","大杯","超大杯"]'),
(11, 11, '杯型', '["中杯","大杯","超大杯"]'),
(12, 12, '杯型', '["中杯","大杯","超大杯"]'),
(13, 13, '杯型', '["中杯","大杯","超大杯"]'),
(14, 14, '杯型', '["中杯","大杯","超大杯"]'),
(15, 15, '杯型', '["中杯","大杯","超大杯"]'),
(16, 16, '杯型', '["中杯","大杯","超大杯"]'),
(17, 17, '杯型', '["中杯","大杯","超大杯"]'),
(18, 18, '杯型', '["中杯","大杯","超大杯"]');

-- 甜度 (奶茶和果茶)
INSERT INTO dish_flavor (id, dish_id, name, value) VALUES
(19, 1, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(20, 2, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(21, 3, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(22, 4, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(23, 5, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(24, 6, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(25, 7, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(26, 8, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(27, 9, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(28, 10, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(29, 12, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(30, 13, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(31, 14, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]'),
(32, 15, '甜度', '["无糖","三分糖","半糖","七分糖","全糖"]');

-- 冰量 (所有饮品)
INSERT INTO dish_flavor (id, dish_id, name, value) VALUES
(33, 1, '冰量', '["常温","去冰","少冰","多冰"]'),
(34, 2, '冰量', '["常温","去冰","少冰","多冰"]'),
(35, 3, '冰量', '["常温","去冰","少冰","多冰"]'),
(36, 4, '冰量', '["常温","去冰","少冰","多冰"]'),
(37, 5, '冰量', '["常温","去冰","少冰","多冰"]'),
(38, 6, '冰量', '["常温","去冰","少冰","多冰"]'),
(39, 7, '冰量', '["常温","去冰","少冰","多冰"]'),
(40, 8, '冰量', '["常温","去冰","少冰","多冰"]'),
(41, 9, '冰量', '["常温","去冰","少冰","多冰"]'),
(42, 10, '冰量', '["常温","去冰","少冰","多冰"]'),
(43, 11, '冰量', '["常温","去冰","少冰","多冰"]'),
(44, 12, '冰量', '["常温","去冰","少冰","多冰"]'),
(45, 13, '冰量', '["常温","去冰","少冰","多冰"]'),
(46, 14, '冰量', '["常温","去冰","少冰","多冰"]'),
(47, 15, '冰量', '["常温","去冰","少冰","多冰"]'),
(48, 16, '冰量', '["常温","去冰","少冰","多冰"]'),
(49, 17, '冰量', '["常温","去冰","少冰","多冰"]'),
(50, 18, '冰量', '["常温","去冰","少冰","多冰"]');

-- 加料 (奶茶专用)
INSERT INTO dish_flavor (id, dish_id, name, value) VALUES
(51, 1, '加料', '["不加","珍珠","椰果","脆啵啵","芋圆"]'),
(52, 2, '加料', '["不加","珍珠","椰果","脆啵啵","芋圆"]'),
(53, 3, '加料', '["不加","珍珠","椰果","脆啵啵","芋圆"]'),
(54, 4, '加料', '["不加","珍珠","椰果","脆啵啵","芋圆"]'),
(55, 5, '加料', '["不加","珍珠","椰果","脆啵啵","芋圆"]');

-- =============================================
-- 6. 套餐数据
-- =============================================

-- 超值单人餐 (category_id=10)
INSERT INTO setmeal (id, category_id, name, price, status, description, image, create_time, update_time, create_user, update_user) VALUES
(1, 10, '经典奶茶单人餐', 25.00, 1, '任选一款经典奶茶 + 一份小食甜品', 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/c09a0ee8-9d19-428d-81b9-746221824113.png', NOW(), NOW(), 1, 1),
(2, 10, '果茶单人餐', 30.00, 1, '任选一款鲜果茶 + 一份小食甜品', 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/a101a1e9-8f8b-47b2-afa4-1abd47ea0a87.png', NOW(), NOW(), 1, 1),
(3, 10, '咖啡单人餐', 32.00, 1, '任选一款咖啡 + 一份小食甜品', 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/8cfcc576-4b66-4a09-ac68-ad5b273c2590.png', NOW(), NOW(), 1, 1);

-- 下午茶双人餐 (category_id=11)
INSERT INTO setmeal (id, category_id, name, price, status, description, image, create_time, update_time, create_user, update_user) VALUES
(4, 11, '甜蜜双人下午茶', 56.00, 1, '任选两杯饮品 + 两份小食甜品，适合分享', 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/b544d3ba-a1ae-4d20-a860-81cb5dec9e03.png', NOW(), NOW(), 1, 1),
(5, 11, '闺蜜奶茶双人餐', 48.00, 1, '任选两杯经典奶茶 + 两份小食甜品', 'https://sky-itcast.oss-cn-beijing.aliyuncs.com/7a55b845-1f2b-41fa-9486-76d187ee9ee1.png', NOW(), NOW(), 1, 1);

-- =============================================
-- 7. 套餐-饮品关联数据
-- =============================================

-- 经典奶茶单人餐：珍珠奶茶 + 提拉米苏
INSERT INTO setmeal_dish (id, setmeal_id, dish_id, name, price, copies) VALUES
(1, 1, 1, '珍珠奶茶', 12.00, 1),
(2, 1, 19, '提拉米苏', 22.00, 1);

-- 果茶单人餐：满杯百香果 + 抹茶蛋糕
INSERT INTO setmeal_dish (id, setmeal_id, dish_id, name, price, copies) VALUES
(3, 2, 6, '满杯百香果', 16.00, 1),
(4, 2, 20, '抹茶蛋糕', 18.00, 1);

-- 咖啡单人餐：生椰拿铁 + 鸡蛋仔
INSERT INTO setmeal_dish (id, setmeal_id, dish_id, name, price, copies) VALUES
(5, 3, 15, '生椰拿铁', 20.00, 1),
(6, 3, 21, '鸡蛋仔', 12.00, 1);

-- 甜蜜双人下午茶：满杯百香果 + 芒果冰茶 + 提拉米苏 + 抹茶蛋糕
INSERT INTO setmeal_dish (id, setmeal_id, dish_id, name, price, copies) VALUES
(7, 4, 6, '满杯百香果', 16.00, 1),
(8, 4, 8, '芒果冰茶', 16.00, 1),
(9, 4, 19, '提拉米苏', 22.00, 1),
(10, 4, 20, '抹茶蛋糕', 18.00, 1);

-- 闺蜜奶茶双人餐：黑糖啵啵奶茶 + 芋泥波波奶茶 + 提拉米苏 + 鸡蛋仔
INSERT INTO setmeal_dish (id, setmeal_id, dish_id, name, price, copies) VALUES
(11, 5, 2, '黑糖啵啵奶茶', 15.00, 1),
(12, 5, 4, '芋泥波波奶茶', 16.00, 1),
(13, 5, 19, '提拉米苏', 22.00, 1),
(14, 5, 21, '鸡蛋仔', 12.00, 1);

-- =============================================
-- 完成！
-- =============================================
SELECT '数据导入完成！分类数：' AS msg, COUNT(*) AS total FROM category;
SELECT '饮品数：' AS msg, COUNT(*) AS total FROM dish;
SELECT '规格数：' AS msg, COUNT(*) AS total FROM dish_flavor;
SELECT '套餐数：' AS msg, COUNT(*) AS total FROM setmeal;
SELECT '套餐明细数：' AS msg, COUNT(*) AS total FROM setmeal_dish;
