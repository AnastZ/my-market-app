SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE ORDER_ITEM;
TRUNCATE TABLE ORDER_TABLE;
TRUNCATE TABLE CART_ITEM;
TRUNCATE TABLE CART;
TRUNCATE TABLE ITEM;
TRUNCATE TABLE USERS;
TRUNCATE TABLE USER_ROLES;
SET REFERENTIAL_INTEGRITY TRUE;

ALTER TABLE ITEM ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE CART ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE CART_ITEM ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE ORDER_TABLE ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE ORDER_ITEM ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE USERS ALTER COLUMN  ID RESTART WITH 1;
ALTER TABLE USER_ROLES ALTER COLUMN ID RESTART WITH 1;

-- Добавление тестовых товаров
INSERT INTO ITEM (TITLE, DESCRIPTION, IMG_PATH, PRICE, VERSION) VALUES
                                                                    ('Смартфон X100', 'Современный смартфон с отличной камерой и большим экраном', '/images/smartphone.jpg', 50000, 0),
                                                                    ('Ноутбук Pro', 'Мощный ноутбук для работы и игр', '/images/laptop.jpg', 120000, 0),
                                                                    ('Наушники Wireless', 'Беспроводные наушники с шумоподавлением', '/images/headphones.jpg', 15000, 0),
                                                                    ('Планшет Tab 10', 'Легкий и компактный планшет для повседневных задач', '/images/tablet.jpg', 40000, 0),
                                                                    ('Умные часы Watch 5', 'Стильные умные часы с мониторингом здоровья', '/images/watch.jpg', 25000, 0),
                                                                    ('Фитнес-браслет Fit', 'Трекер активности и сна', '/images/fitness.jpg', 5000, 0),
                                                                    ('Внешний аккумулятор Power 20000', 'Емкий повербанк для зарядки устройств', '/images/powerbank.jpg', 3000, 0),
                                                                    ('Карта памяти 128GB', 'Быстрая карта памяти для фото и видео', '/images/sdcard.jpg', 2000, 0),
                                                                    ('Мышь беспроводная', 'Эргономичная мышь для комфортной работы', '/images/mouse.jpg', 2500, 0),
                                                                    ('Клавиатура механическая', 'Игровая механическая клавиатура с подсветкой', '/images/keyboard.jpg', 7000, 0),
                                                                    ('Монитор 27" 4K', 'Профессиональный монитор с высоким разрешением', '/images/monitor.jpg', 60000, 0),
                                                                    ('Принтер лазерный', 'Многофункциональное устройство для печати', '/images/printer.jpg', 35000, 0);

-- Создание тестовых корзин
INSERT INTO CART (USERNAME, VERSION) VALUES
                                           ('test-session-123', 0),
                                           ('test-session-456', 0),
                                           ('empty-cart-session', 0),
                                           ('full-cart-session', 0);

-- Добавление товаров в корзину
INSERT INTO CART_ITEM (CART_ID, ITEM_ID, TITLE, COUNT, ONE_ITEM_PRICE, VERSION) VALUES
                                                                                    (1, 1, 'Смартфон X100', 2, 50000, 0),
                                                                                    (1, 2, 'Ноутбук Pro', 1, 120000, 0),
                                                                                    (1, 3, 'Наушники Wireless', 3, 15000, 0),
                                                                                    (2, 4, 'Планшет Tab 10', 1, 40000, 0),
                                                                                    (2, 5, 'Умные часы Watch 5', 2, 25000, 0);

-- Добавление множества товаров в корзину для тестирования пагинации
INSERT INTO CART_ITEM (CART_ID, ITEM_ID, TITLE, COUNT, ONE_ITEM_PRICE, VERSION)
SELECT 4, ID, TITLE, 1, PRICE, 0 FROM ITEM WHERE ID <= 10;

-- Создание тестовых заказов
INSERT INTO ORDER_TABLE (VERSION) VALUES
                                      (0),
                                      (0);

-- Добавление товаров в заказы (с добавленным полем TITLE)
INSERT INTO ORDER_ITEM (ORDER_ID, ITEM_ID, TITLE, COUNT, PRICE_AT_ORDER, VERSION) VALUES
                                                                                      (1, 1, 'Смартфон X100', 1, 50000, 0),
                                                                                      (1, 2, 'Ноутбук Pro', 1, 120000, 0),
                                                                                      (1, 3, 'Наушники Wireless', 2, 15000, 0),
                                                                                      (2, 4, 'Планшет Tab 10', 1, 40000, 0),
                                                                                      (2, 5, 'Умные часы Watch 5', 1, 25000, 0);
INSERT INTO USERS (USERNAME) VALUES
                                     (TEST1),
                                     (TEST2);
INSERT INTO USER_ROLES(USERNAME, USER_ROLE) VALUES
                                                (TEST1, CLIENT),
                                                (TEST2, USER);
