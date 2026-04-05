
DELETE FROM ORDER_ITEM;
DELETE FROM ORDER_TABLE;
DELETE FROM CART_ITEM;
DELETE FROM CART;
DELETE FROM USER_ROLES;
DELETE FROM USERS;
DELETE FROM ITEM;
-- 1. Добавление пользователей
INSERT INTO USERS (USERNAME, PASSWORD) VALUES
                                           ('username', '$2a$10$YourHashedPasswordHere1'),
                                           ('bob', '$2a$10$YourHashedPasswordHere2'),     -- пароль: bob123
                                           ('charlie', '$2a$10$YourHashedPasswordHere3'); -- пароль: charlie123

-- 2. Добавление ролей пользователей
INSERT INTO USER_ROLES (USERNAME, USER_ROLE) VALUES
                                                 ('username', 'ROLE_USER'),
                                                 ('username', 'ROLE_ADMIN'),
                                                 ('bob', 'ROLE_USER'),
                                                 ('charlie', 'ROLE_USER');

-- 3. Добавление товаров
INSERT INTO ITEM (TITLE, DESCRIPTION, IMG_PATH, PRICE, VERSION) VALUES
                                                                    ('Смартфон X100', 'Мощный смартфон с отличной камерой', '/images/phone.jpg', 29990, 0),
                                                                    ('Ноутбук Pro 15', '15-дюймовый ноутбук для работы и игр', '/images/laptop.jpg', 69990, 0),
                                                                    ('Беспроводные наушники', 'Качественный звук и шумоподавление', '/images/headphones.jpg', 4990, 0),
                                                                    ('Клавиатура Mechanical', 'Механическая клавиатура с подсветкой', '/images/keyboard.jpg', 3990, 0),
                                                                    ('Мышь Gaming', 'Игровая мышь с 6 кнопками', '/images/mouse.jpg', 1990, 0),
                                                                    ('Монитор 27" 4K', '27-дюймовый 4K монитор', '/images/monitor.jpg', 24990, 0);

-- 4. Добавление корзин для пользователей (связь с USERS через USERNAME)
INSERT INTO CART (USERNAME, VERSION) VALUES
                                         ('username', 0),
                                         ('bob', 0),
                                         ('charlie', 0);

-- 5. Добавление товаров в корзины
INSERT INTO CART_ITEM (CART_ID, ITEM_ID, TITLE, COUNT, ONE_ITEM_PRICE)
SELECT c.ID, i.ID, i.TITLE, 2, i.PRICE
FROM CART c, ITEM i
WHERE c.USERNAME = 'username' AND i.TITLE = 'Смартфон X100';

INSERT INTO CART_ITEM (CART_ID, ITEM_ID, TITLE, COUNT, ONE_ITEM_PRICE)
SELECT c.ID, i.ID, i.TITLE, 1, i.PRICE
FROM CART c, ITEM i
WHERE c.USERNAME = 'username' AND i.TITLE = 'Беспроводные наушники';

-- Корзина bob
INSERT INTO CART_ITEM (CART_ID, ITEM_ID, TITLE, COUNT, ONE_ITEM_PRICE)
SELECT c.ID, i.ID, i.TITLE, 1, i.PRICE
FROM CART c, ITEM i
WHERE c.USERNAME = 'bob' AND i.TITLE = 'Ноутбук Pro 15';

INSERT INTO CART_ITEM (CART_ID, ITEM_ID, TITLE, COUNT, ONE_ITEM_PRICE)
SELECT c.ID, i.ID, i.TITLE, 1, i.PRICE
FROM CART c, ITEM i
WHERE c.USERNAME = 'bob' AND i.TITLE = 'Мышь Gaming';

-- ============================================
-- 6. Добавление заказов
-- ============================================
INSERT INTO ORDER_TABLE (VERSION, USERNAME) VALUES
                                                (0, 'username'),
                                                (0, 'bob'),
                                                (0, 'username');

-- ============================================
-- 7. Добавление товаров в заказы (ИСПРАВЛЕННАЯ ВЕРСИЯ)
-- ============================================

-- Заказ для username (первый заказ - самый старый)
INSERT INTO ORDER_ITEM (ORDER_ID, ITEM_ID, TITLE, COUNT, PRICE_AT_ORDER)
SELECT o.ID, i.ID, i.TITLE, 1, i.PRICE
FROM ORDER_TABLE o, ITEM i
WHERE o.USERNAME = 'username'
  AND o.CREATED_AT = (SELECT MIN(CREATED_AT) FROM ORDER_TABLE WHERE USERNAME = 'username')
  AND i.TITLE = 'Смартфон X100';

INSERT INTO ORDER_ITEM (ORDER_ID, ITEM_ID, TITLE, COUNT, PRICE_AT_ORDER)
SELECT o.ID, i.ID, i.TITLE, 2, i.PRICE
FROM ORDER_TABLE o, ITEM i
WHERE o.USERNAME = 'username'
  AND o.CREATED_AT = (SELECT MIN(CREATED_AT) FROM ORDER_TABLE WHERE USERNAME = 'username')
  AND i.TITLE = 'Беспроводные наушники';

-- Заказ для bob
INSERT INTO ORDER_ITEM (ORDER_ID, ITEM_ID, TITLE, COUNT, PRICE_AT_ORDER)
SELECT o.ID, i.ID, i.TITLE, 1, i.PRICE
FROM ORDER_TABLE o, ITEM i
WHERE o.USERNAME = 'bob'
  AND i.TITLE = 'Ноутбук Pro 15';

-- Заказ для username (второй заказ - самый новый)
INSERT INTO ORDER_ITEM (ORDER_ID, ITEM_ID, TITLE, COUNT, PRICE_AT_ORDER)
SELECT o.ID, i.ID, i.TITLE, 1, i.PRICE
FROM ORDER_TABLE o, ITEM i
WHERE o.USERNAME = 'username'
  AND o.CREATED_AT = (SELECT MAX(CREATED_AT) FROM ORDER_TABLE WHERE USERNAME = 'username')
  AND i.TITLE = 'Клавиатура Mechanical';