CREATE SCHEMA IF NOT EXISTS session_secure_app;
USE session_secure_app;

CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(45) NOT NULL,
    last_name VARCHAR(45) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_type ENUM('ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN'),
    PRIMARY KEY (user_id, role_type),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE confirmation_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(36) NOT NULL,
    token_type ENUM('EMAIL_CONFIRMATION_TOKEN', 'PASSWORD_CHANGE_TOKEN', 'ACCOUNT_DELETION_TOKEN') NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE
);