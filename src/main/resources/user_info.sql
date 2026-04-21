-- 1. 创建 user_info 用户个人信息表
CREATE TABLE IF NOT EXISTS user_info (
    id SERIAL PRIMARY KEY,
    real_name VARCHAR(50),     -- 真实姓名
    phone VARCHAR(20),         -- 手机号码
    address VARCHAR(200),      -- 联系地址
    user_id INT NOT NULL UNIQUE -- 关联 sys_user 表的 id. UNIQUE 保证一个账号只前
);

-- 2. 插入几条测试数据（sys_user 表中插入 id 为 1 和 2 的用户）
INSERT INTO user_info (real_name, phone, address, user_id) VALUES ('张三', '13800138001', '北京市朝阳区', 1);
INSERT INTO user_info (real_name, phone, address, user_id) VALUES ('李四', '13900139001', '上海市浦东新区', 2);