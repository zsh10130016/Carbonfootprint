DROP TABLE IF EXISTS ocr_parse_log;
DROP TABLE IF EXISTS points_ledger;
DROP TABLE IF EXISTS carbon_record;
DROP TABLE IF EXISTS advice_rule;
DROP TABLE IF EXISTS emission_factor;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    location VARCHAR(100),
    bio VARCHAR(255),
    carbon_goal DECIMAL(10, 2) DEFAULT 0,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE emission_factor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_type VARCHAR(50) NOT NULL,
    sub_type VARCHAR(50) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    factor_value DECIMAL(10, 4) NOT NULL,
    factor_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    UNIQUE KEY uk_factor (activity_type, sub_type, unit)
);

CREATE TABLE carbon_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    sub_type VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    emission_factor_value DECIMAL(10, 4) NOT NULL,
    emission_kg DECIMAL(10, 2) NOT NULL,
    points INT NOT NULL DEFAULT 0,
    note VARCHAR(255),
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES `user` (id)
);

CREATE INDEX idx_record_user_time ON carbon_record(user_id, occurred_at);
CREATE INDEX idx_record_user_type ON carbon_record(user_id, activity_type);

CREATE TABLE advice_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_type VARCHAR(50) NOT NULL,
    threshold_kg DECIMAL(10, 2) NOT NULL,
    period_days INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    suggestion VARCHAR(255) NOT NULL
);

CREATE TABLE points_ledger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    source_record_id BIGINT NOT NULL,
    points INT NOT NULL,
    reason VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_points_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_points_record FOREIGN KEY (source_record_id) REFERENCES carbon_record (id)
);

CREATE INDEX idx_points_user ON points_ledger(user_id);

CREATE TABLE article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    summary VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    cover_image VARCHAR(255),
    author VARCHAR(50) NOT NULL DEFAULT '低碳实验室',
    published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ocr_parse_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    document_type VARCHAR(50) NOT NULL,
    raw_text TEXT,
    provider VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
