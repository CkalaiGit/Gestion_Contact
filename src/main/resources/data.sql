-- USERS (remplacez les $2a$... par de vrais hashes BCrypt)
INSERT INTO users (id, username, password) VALUES (1, 'AdaLovelace', '$2a$10$2lqM19jTBcghqOfEMF8iWeSrl7/JppIcmDzPO1fUlBrqZiTivxTfm');
INSERT INTO users (id, username, password) VALUES (2, 'AlanTurin',    '$2a$10$I.fm.82S..1oUJn5IOTFhO.qoMdoaDB12/EWzsdf/z8hv1iJkuNUm');
INSERT INTO users (id, username, password) VALUES (3, 'GraceHopper',  '$2a$10$06apnTocwxCHr0mit9FuaOdoNxf7Saee4Nd5j8k40fdRN43xW/0z2');
INSERT INTO users (id, username, password) VALUES (4, 'CairedineKALAI','$2a$10$IWyZK6PtMn6WJJQsXUwGJ.smpaX.WbZ0dyYOBQJAPhUQzs3AuUjNa');
INSERT INTO users (id, username, password) VALUES (5, 'admin',        '$2a$10$GFFAdsv3TW6mc0asvzxiheiYYdRaERWd2XqS7.kaudbe/KG0lcdxS');

-- ROLES
INSERT INTO user_roles (user_id, role) VALUES (1, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (2, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (3, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (4, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (5, 'ADMIN');

-- CONTACTS
INSERT INTO contact (id, first_name, last_name, email, phone, user_id)
VALUES (1, 'Ada', 'Lovelace', 'ada@acme.com', '0601010101', 4);

INSERT INTO contact (id, first_name, last_name, email, phone, user_id)
VALUES (2, 'Alan', 'Turing', 'alan@acme.com', '0602020202', 4);

INSERT INTO contact (id, first_name, last_name, email, phone, user_id)
VALUES (3, 'Grace', 'Hopper', 'grace@acme.com', '0603030303', 4);


