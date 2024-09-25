SET @USER_EMAIL = "user@mail.com";
SET @ADMIN_EMAIL = "admin@mail.com";

INSERT INTO users(first_name, last_name, email, password, enabled) VALUES
("user_first_name", "user_last_name", @USER_EMAIL, "$2a$10$eP9JiXgbOBkIof/tfYvIHujjPU5NUqIdN/SW3NZnMVuoHejsRQRBO", true),
("admin_first_name", "admin_last_name", @ADMIN_EMAIL, "$2a$10$19Mh29xrhu6R3IAQiKIH..Pl9X00lNE1fPRvR1ShsWctIf7pZcvNi", true);

INSERT INTO user_role VALUES
((SELECT id FROM users WHERE email=@USER_EMAIL), "ROLE_USER"),
((SELECT id FROM users WHERE email=@ADMIN_EMAIL), "ROLE_USER"),
((SELECT id FROM users WHERE email=@ADMIN_EMAIL), "ROLE_ADMIN");
