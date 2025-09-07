-- 스키마 선택(이미 만들었으면 생략)
-- CREATE DATABASE IF NOT EXISTS ansimbank DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ansimbank;

-- 공통: 타임스탬프 기본값
-- created_at = NOW(), updated_at = NOW() ON UPDATE NOW()

/* 1) USERS */
CREATE TABLE IF NOT EXISTS users (
                                     user_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     email             VARCHAR(190) NOT NULL,
    password          VARCHAR(255) NOT NULL,
    name              VARCHAR(100) NOT NULL,
    phone             VARCHAR(30),
    birth_date        DATE,
    user_type         ENUM('PARENT','CHILD') NOT NULL DEFAULT 'PARENT',
    social_provider   ENUM('KAKAO','NAVER','NONE') NOT NULL DEFAULT 'NONE',
    social_id         VARCHAR(190),
    is_active         TINYINT(1) NOT NULL DEFAULT 1,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_social UNIQUE (social_provider, social_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 2) ACCOUNTS */
CREATE TABLE IF NOT EXISTS accounts (
                                        account_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        user_id        BIGINT NOT NULL,
                                        bank_code      VARCHAR(20) NOT NULL,
    bank_name      VARCHAR(50) NOT NULL,
    account_number VARCHAR(64) NOT NULL,
    account_holder VARCHAR(100) NOT NULL,
    is_primary     TINYINT(1) NOT NULL DEFAULT 0,
    is_active      TINYINT(1) NOT NULL DEFAULT 1,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT uk_account_per_user UNIQUE (user_id, account_number)
    -- (is_primary 한 사용자당 1개 제약은 애플리케이션/트리거로 관리 권장)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 3) FAMILY_CONNECTIONS */
CREATE TABLE IF NOT EXISTS family_connections (
                                                  connection_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  parent_id         BIGINT NOT NULL,
                                                  child_id          BIGINT NOT NULL,
                                                  connection_status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    monthly_limit     BIGINT DEFAULT NULL,
    monitoring_enabled TINYINT(1) NOT NULL DEFAULT 1,
    alert_threshold   BIGINT DEFAULT NULL,
    requested_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at       DATETIME DEFAULT NULL,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_fc_parent FOREIGN KEY (parent_id) REFERENCES users(user_id),
    CONSTRAINT fk_fc_child  FOREIGN KEY (child_id)  REFERENCES users(user_id),
    CONSTRAINT uk_fc_pair UNIQUE (parent_id, child_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 4) DELEGATIONS (위임장) */
CREATE TABLE IF NOT EXISTS delegations (
                                           delegation_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           delegator_id      BIGINT NOT NULL,  -- 위임자(부모)
                                           delegate_id       BIGINT NOT NULL,  -- 대리인(자녀)
                                           delegation_type   VARCHAR(50) NOT NULL,
    delegation_scope  TEXT,
    valid_from        DATE,
    valid_until       DATE,
    daily_limit       BIGINT DEFAULT NULL,
    monthly_limit     BIGINT DEFAULT NULL,
    allowed_accounts  TEXT, -- 여러 계좌면 JSON 권장. 지금은 TEXT로.
    approval_status   ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    delegation_status ENUM('ACTIVE','SUSPENDED','TERMINATED') NOT NULL DEFAULT 'ACTIVE',
    document_path     VARCHAR(255),
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_delegations_delegator FOREIGN KEY (delegator_id) REFERENCES users(user_id),
    CONSTRAINT fk_delegations_delegate  FOREIGN KEY (delegate_id)  REFERENCES users(user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 5) TRANSACTIONS (송금) */
CREATE TABLE IF NOT EXISTS transactions (
                                            transaction_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            sender_id             BIGINT NOT NULL,
                                            delegation_id         BIGINT DEFAULT NULL,
                                            sender_account        VARCHAR(64) NOT NULL,
    receiver_account      VARCHAR(64) NOT NULL,
    receiver_name         VARCHAR(100) NOT NULL,
    receiver_bank         VARCHAR(50) NOT NULL,
    amount                BIGINT NOT NULL,
    transaction_type      VARCHAR(30) NOT NULL, -- DIRECT, DELEGATION, ONE_CLICK 등
    memo                  TEXT,
    transaction_status    ENUM('PENDING','COMPLETED','FAILED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    external_transaction_id VARCHAR(100),
    requested_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at          DATETIME DEFAULT NULL,
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_sender      FOREIGN KEY (sender_id)    REFERENCES users(user_id),
    CONSTRAINT fk_tx_delegation  FOREIGN KEY (delegation_id) REFERENCES delegations(delegation_id),
    INDEX ix_tx_sender (sender_id),
    INDEX ix_tx_status (transaction_status),
    INDEX ix_tx_requested_at (requested_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 6) ONE_CLICK_PRESETS */
CREATE TABLE IF NOT EXISTS one_click_presets (
                                                 preset_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 user_id         BIGINT NOT NULL,
                                                 preset_name     VARCHAR(100) NOT NULL,
    receiver_account VARCHAR(64) NOT NULL,
    receiver_name   VARCHAR(100) NOT NULL,
    receiver_bank   VARCHAR(50) NOT NULL,
    default_amount  BIGINT DEFAULT NULL,
    button_color    VARCHAR(20),
    display_order   INT DEFAULT 0,
    is_active       TINYINT(1) NOT NULL DEFAULT 1,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_preset_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX ix_preset_user (user_id),
    CONSTRAINT uk_user_preset_name UNIQUE (user_id, preset_name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 7) FRAUD_ACCOUNTS (사기 계좌 DB) */
CREATE TABLE IF NOT EXISTS fraud_accounts (
                                              fraud_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              account_number VARCHAR(64) NOT NULL,
    bank_code      VARCHAR(20) NOT NULL,
    bank_name      VARCHAR(50) NOT NULL,
    fraud_type     VARCHAR(50) NOT NULL, -- ENUM 대신 문자열(외부 분류 확장 고려)
    description    TEXT,
    risk_level     ENUM('HIGH','MEDIUM','LOW') NOT NULL DEFAULT 'MEDIUM',
    reported_at    DATETIME DEFAULT NULL,
    reporter_source VARCHAR(100),
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active      TINYINT(1) NOT NULL DEFAULT 1,
    INDEX uk_fraud_account (account_number, bank_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 8) NOTIFICATIONS */
CREATE TABLE IF NOT EXISTS notifications (
                                             notification_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             user_id           BIGINT NOT NULL,
                                             transaction_id    BIGINT DEFAULT NULL,
                                             notification_type VARCHAR(50) NOT NULL, -- FRAUD_ALERT, TRANSACTION_REQUEST, ...
    title             VARCHAR(200) NOT NULL,
    message           TEXT,
    priority          ENUM('HIGH','MEDIUM','LOW') NOT NULL DEFAULT 'MEDIUM',
    is_read           TINYINT(1) NOT NULL DEFAULT 0,
    delivery_method   ENUM('WEBSOCKET','PUSH','SMS') NOT NULL DEFAULT 'WEBSOCKET',
    sent_at           DATETIME DEFAULT NULL,
    read_at           DATETIME DEFAULT NULL,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user  FOREIGN KEY (user_id)        REFERENCES users(user_id),
    CONSTRAINT fk_notif_tx    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id),
    INDEX ix_notif_user (user_id),
    INDEX ix_notif_read (is_read, created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 9) BRANCH_RESERVATIONS (지점 예약) */
CREATE TABLE IF NOT EXISTS branch_reservations (
                                                   reservation_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                   user_id          BIGINT NOT NULL,
                                                   branch_code      VARCHAR(30) NOT NULL,
    branch_name      VARCHAR(100) NOT NULL,
    branch_address   VARCHAR(255),
    service_type     VARCHAR(100) NOT NULL,
    reservation_date DATE NOT NULL,
    reservation_time TIME NOT NULL,
    reservation_status ENUM('CONFIRMED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'CONFIRMED',
    memo             TEXT,
    family_shared    TINYINT(1) NOT NULL DEFAULT 0,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reserve_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX ix_reserve_user (user_id, reservation_date)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 10) VOICE_COMMANDS (음성 인식 로그) */
CREATE TABLE IF NOT EXISTS voice_commands (
                                              command_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              user_id           BIGINT,
                                              original_text     TEXT,
                                              processed_text    TEXT,
                                              intent            VARCHAR(50),
    extracted_entities JSON,
    command_status    ENUM('SUCCESS','FAILED','PARTIAL') NOT NULL DEFAULT 'SUCCESS',
    action_taken      VARCHAR(100),
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vc_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX ix_vc_user (user_id, created_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/* 11) USER_SESSIONS (세션/JWT) */
CREATE TABLE IF NOT EXISTS user_sessions (
                                             session_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             user_id       BIGINT NOT NULL,
                                             jwt_token     VARCHAR(500) NOT NULL,
    token_type    ENUM('ACCESS','REFRESH','DELEGATION') NOT NULL DEFAULT 'ACCESS',
    expires_at    DATETIME NOT NULL,
    ip_address    VARCHAR(45),
    user_agent    VARCHAR(255),
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active     TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX ix_session_user (user_id, is_active),
    CONSTRAINT uk_jwt UNIQUE (jwt_token)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



