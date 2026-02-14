CREATE TABLE IF NOT EXISTS users
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_uid           VARCHAR(32) NOT NULL UNIQUE,
    anonymous_id       VARCHAR(36) NOT NULL UNIQUE,
    google_sub         VARCHAR(255) UNIQUE  DEFAULT NULL,
    email              VARCHAR(255)         DEFAULT NULL,
    name               VARCHAR(255)         DEFAULT NULL,
    preferred_currency VARCHAR(10)          DEFAULT 'USD',
    preferred_language VARCHAR(10)          DEFAULT 'en',
    created_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE uniq_google_sub(google_sub),
    UNIQUE uniq_anonymous_id(anonymous_id),
    INDEX idx_email (email)
);

CREATE TABLE IF NOT EXISTS monthly_summary
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT         NOT NULL,
    month_key     CHAR(7)        NOT NULL,
    total_income  DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total_expense DECIMAL(15, 2) NOT NULL DEFAULT 0,
    updated_at    DATETIME       NOT NULL,
    UNIQUE KEY (user_id, month_key)
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

# ALTER TABLE risk_signal
#     ADD COLUMN month_key VARCHAR(7) NOT NULL,
#     ADD INDEX idx_user_month (user_id, month_key);

CREATE TABLE IF NOT EXISTS risk_rule_config
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name       VARCHAR(100)   NOT NULL,
    enabled         BOOLEAN        NOT NULL DEFAULT TRUE,
    severity        VARCHAR(10)    NOT NULL,
    threshold_value DECIMAL(10, 2) NULL,
    updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uniq_rule_name (rule_name)
);

# INSERT INTO risk_rule_config (rule_name, enabled, severity, threshold_value)
# VALUES ('NEGATIVE_CASHFLOW', TRUE, 'HIGH', 1.0);

# INSERT INTO risk_rule_config (rule_name, enabled, severity, threshold_value)
# VALUES ('EXPENSE_SPIKE', TRUE, 'MEDIUM', 2.0);

CREATE TABLE IF NOT EXISTS risk_state
(
    user_id    BIGINT PRIMARY KEY,
    last_level VARCHAR(10) NOT NULL,
    updated_at DATETIME    NOT NULL
);

CREATE TABLE IF NOT EXISTS risk_level_history
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    old_level       VARCHAR(20) NOT NULL,
    new_level       VARCHAR(20) NOT NULL,
    top_signal_type VARCHAR(50),
    occurred_at     DATETIME    NOT NULL,
    INDEX idx_user_time (user_id, occurred_at DESC, id DESC)
);

CREATE TABLE IF NOT EXISTS categories
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT       NULL,
    name       VARCHAR(100) NOT NULL,
    type       VARCHAR(20)  NOT NULL,
    icon       VARCHAR(50),
    color      VARCHAR(20),
    is_default BOOLEAN  DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_name_type (user_id, name, type)
);
# INSERT INTO categories (user_id, name, type, icon, color, is_default)
# VALUES
#     (NULL, 'Salary', 'INCOME', 'attach_money', '#2ECC71', TRUE),
#     (NULL, 'Bonus', 'INCOME', 'card_giftcard', '#27AE60', TRUE),
#     (NULL, 'Investment', 'INCOME', 'trending_up', '#1ABC9C', TRUE),
#     (NULL, 'Freelance', 'INCOME', 'work', '#16A085', TRUE);
#
# INSERT INTO categories (user_id, name, type, icon, color, is_default)
# VALUES
#     (NULL, 'Food', 'EXPENSE', 'restaurant', '#E67E22', TRUE),
#     (NULL, 'Shopping', 'EXPENSE', 'shopping_bag', '#E74C3C', TRUE),
#     (NULL, 'Transport', 'EXPENSE', 'directions_car', '#3498DB', TRUE),
#     (NULL, 'Bills', 'EXPENSE', 'receipt_long', '#9B59B6', TRUE),
#     (NULL, 'Entertainment', 'EXPENSE', 'movie', '#8E44AD', TRUE),
#     (NULL, 'Health', 'EXPENSE', 'local_hospital', '#E84393', TRUE),
#     (NULL, 'Education', 'EXPENSE', 'school', '#6C5CE7', TRUE),
#     (NULL, 'Travel', 'EXPENSE', 'flight', '#00CEC9', TRUE),
#     (NULL, 'Other', 'EXPENSE', 'category', '#95A5A6', TRUE);

CREATE TABLE IF NOT EXISTS budget_config
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT         NOT NULL,
    category_id   BIGINT         NOT NULL,
    monthly_limit DECIMAL(18, 2) NOT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_category (user_id, category_id),
    CONSTRAINT fk_budget_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT         NOT NULL,
    type         VARCHAR(10)    NOT NULL,
    amount       DECIMAL(15, 2) NOT NULL,
    category_id  BIGINT         NOT NULL,
    occurred_at  DATE           NOT NULL,
    created_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_category
        FOREIGN KEY (category_id)
            REFERENCES categories(id)
            ON DELETE RESTRICT,
    INDEX idx_tx_user_date (user_id, occurred_at),
    INDEX idx_tx_user_type_date (user_id, type, occurred_at),
    INDEX idx_tx_user_category_date (user_id, category_id, occurred_at),
    INDEX idx_tx_user_type (user_id, type)
);