CREATE TABLE IF NOT EXISTS users
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    anonymous_id VARCHAR(36) NOT NULL UNIQUE,
    google_sub   VARCHAR(255) UNIQUE  DEFAULT NULL,
    email        VARCHAR(255)         DEFAULT NULL,
    name         VARCHAR(255)         DEFAULT NULL,
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_google_sub (google_sub),
    INDEX idx_email (email)
);

CREATE TABLE IF NOT EXISTS transactions
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT         NOT NULL,
    type        VARCHAR(10)    NOT NULL,
    amount      DECIMAL(15, 2) NOT NULL,
    category    VARCHAR(50),
    occurred_at DATE           NOT NULL,
    created_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_occurred (user_id, occurred_at),
    INDEX idx_user_type (user_id, type)
);

CREATE TABLE IF NOT EXISTS monthly_summary
(
    user_id       BIGINT         NOT NULL,
    `year_month`  CHAR(7)        NOT NULL,
    total_income  DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total_expense DECIMAL(15, 2) NOT NULL DEFAULT 0,
    updated_at    DATETIME       NOT NULL,
    PRIMARY KEY (user_id, `year_month`)
);

CREATE TABLE IF NOT EXISTS risk_signal
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    signal_type VARCHAR(50) NOT NULL,
    severity    VARCHAR(10) NOT NULL,
    detected_at DATETIME    NOT NULL,
    metadata    JSON,
    INDEX idx_user_detected (user_id, detected_at),
    INDEX idx_signal_type (signal_type)
);