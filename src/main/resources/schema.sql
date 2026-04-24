CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_info (
    id BIGSERIAL PRIMARY KEY,
    real_name VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(255),
    user_id BIGINT
);

-- 插入ID为1的用户
INSERT INTO sys_user (id, username, password) VALUES (1, 'admin', '123456') ON CONFLICT (id) DO NOTHING;
