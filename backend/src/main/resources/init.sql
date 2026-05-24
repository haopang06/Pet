-- 创建数据库
CREATE DATABASE IF NOT EXISTS pet_feeding_system;

-- 使用数据库
USE pet_feeding_system;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    avatar LONGTEXT
);

-- 创建宠物表
CREATE TABLE IF NOT EXISTS pets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    pet_type VARCHAR(50) NOT NULL,
    breed VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    weight DOUBLE NOT NULL,
    activity_level VARCHAR(50) NOT NULL,
    note VARCHAR(1000),
    photo LONGTEXT,
    note_images LONGTEXT,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建健康记录表
CREATE TABLE IF NOT EXISTS health_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    weight DOUBLE NOT NULL,
    water_intake DOUBLE,
    food_intake DOUBLE,
    mental_state VARCHAR(50) NOT NULL,
    defecation VARCHAR(50) NOT NULL,
    date DATETIME NOT NULL,
    pet_id BIGINT,
    FOREIGN KEY (pet_id) REFERENCES pets(id)
);

-- 创建健康预警表
CREATE TABLE IF NOT EXISTS health_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message VARCHAR(255) NOT NULL,
    date DATETIME NOT NULL,
    severity VARCHAR(50) NOT NULL,
    handled BIT DEFAULT 0,
    pet_id BIGINT,
    FOREIGN KEY (pet_id) REFERENCES pets(id)
);

-- 创建宠物日记表
CREATE TABLE IF NOT EXISTS pet_diary_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    diary_date DATE NOT NULL,
    content VARCHAR(2000),
    images LONGTEXT,
    favorite BIT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    pet_id BIGINT NOT NULL,
    UNIQUE KEY uk_pet_diary_date (pet_id, diary_date),
    FOREIGN KEY (pet_id) REFERENCES pets(id)
);

-- 创建喂养方案表
CREATE TABLE IF NOT EXISTS feeding_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    daily_energy DOUBLE NOT NULL,
    protein INT NOT NULL,
    fat INT NOT NULL,
    carbs INT NOT NULL,
    frequency INT NOT NULL,
    portion_size DOUBLE NOT NULL,
    canned_portion_size DOUBLE,
    freeze_dried_portion_size DOUBLE,
    pet_id BIGINT,
    FOREIGN KEY (pet_id) REFERENCES pets(id)
);

-- 插入测试数据
INSERT INTO users (username, phone, password, email) VALUES
('admin', '13800000000', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'admin@example.com');

INSERT INTO pets (name, pet_type, breed, age, weight, activity_level, user_id) VALUES
('小白', 'dog', '拉布拉多', 2, 25.5, 'medium', 1),
('小花', 'cat', '英短', 1, 4.2, 'low', 1);
