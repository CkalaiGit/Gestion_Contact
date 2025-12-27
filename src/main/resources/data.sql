------------------------------------------------------------
-- DBUser initial : ADMIN (cairedine@gmail.com)
-- Tous les autres utilisateurs seront créés automatiquement
-- lors de la connexion Google (OIDC)
------------------------------------------------------------
INSERT INTO users (sub, email, username, role)
VALUES (
    '110736165454351850927',         -- Valeur SUB OIDC factice pour le dev
    'cairedine@gmail.com',
    'Cairedine Kalai',
    'ADMIN'
);

------------------------------------------------------------
-- CONTACTS appartenant à l’admin (user_id = 1)
------------------------------------------------------------
INSERT INTO contact (first_name, last_name, email, phone, user_id)
VALUES ('Ada',   'Lovelace', 'ada@acme.com',   '0601010101', 1);

INSERT INTO contact (first_name, last_name, email, phone, user_id)
VALUES ('Alan',  'Turing',   'alan@acme.com',  '0602020202', 1);

INSERT INTO contact (first_name, last_name, email, phone, user_id)
VALUES ('Grace', 'Hopper',   'grace@acme.com', '0603030303', 1);
