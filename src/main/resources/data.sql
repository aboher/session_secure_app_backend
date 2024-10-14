SET @USER_EMAIL = "user@mail.com";
SET @MODERATOR_EMAIL = "moderator@mail.com";
SET @ADMIN_EMAIL = "admin@mail.com";

INSERT INTO user(first_name, last_name, email, password, enabled) VALUES
("user_first_name", "user_last_name", @USER_EMAIL, "$2a$10$eP9JiXgbOBkIof/tfYvIHujjPU5NUqIdN/SW3NZnMVuoHejsRQRBO", true),
("moderator_first_name", "moderator_last_name", @MODERATOR_EMAIL, "$2a$10$QIOVZkHv7zOHaTo4gCv6gulQ/SDVY.nelpO7zyF/ec7KFCYc.Q3Va", true),
("admin_first_name", "admin_last_name", @ADMIN_EMAIL, "$2a$10$19Mh29xrhu6R3IAQiKIH..Pl9X00lNE1fPRvR1ShsWctIf7pZcvNi", true);

INSERT INTO user_role VALUES
((SELECT id FROM user WHERE email=@USER_EMAIL), "ROLE_USER"),
((SELECT id FROM user WHERE email=@MODERATOR_EMAIL), "ROLE_USER"),
((SELECT id FROM user WHERE email=@MODERATOR_EMAIL), "ROLE_MODERATOR"),
((SELECT id FROM user WHERE email=@ADMIN_EMAIL), "ROLE_USER"),
((SELECT id FROM user WHERE email=@ADMIN_EMAIL), "ROLE_ADMIN");
