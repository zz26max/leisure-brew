USE meal_buddy;

-- 只保留一个本地演示管理员，避免把历史账号与个人数据带进新环境。
DELETE FROM shopping_cart;
DELETE FROM address_book;
DELETE FROM user;
DELETE FROM message;
DELETE FROM employee;
ALTER TABLE employee AUTO_INCREMENT = 1;

INSERT INTO employee (
    id, name, username, password, phone, sex, id_number, status,
    create_time, update_time, create_user, update_user
) VALUES (
    1,
    '店主',
    'admin',
    '$2b$10$bJYf.Ze8fm.hBsUcITvcZePI3oDP0wsUdArYnXIV/BrjWa4bX3voO',
    '10000000000',
    '0',
    '000000000000000000',
    1,
    NOW(),
    NOW(),
    1,
    1
);
