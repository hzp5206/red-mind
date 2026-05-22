CREATE TABLE IF NOT EXISTS admin_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_code VARCHAR(64) NOT NULL UNIQUE,
    permission_name VARCHAR(128) NOT NULL,
    module_name VARCHAR(64) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(32) NOT NULL,
    permission_code VARCHAR(64) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_code, permission_code)
);

CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL,
    operator_nickname VARCHAR(64),
    module_name VARCHAR(64) NOT NULL,
    action_name VARCHAR(64) NOT NULL,
    target_type VARCHAR(64),
    target_id BIGINT,
    detail_text VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS template_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_code VARCHAR(64) NOT NULL UNIQUE,
    category_name VARCHAR(64) NOT NULL,
    sort_order INT DEFAULT 0,
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO admin_permissions (permission_code, permission_name, module_name)
SELECT 'dashboard:view', '查看后台首页', 'dashboard'
WHERE NOT EXISTS (SELECT 1 FROM admin_permissions WHERE permission_code = 'dashboard:view');

INSERT INTO admin_permissions (permission_code, permission_name, module_name)
SELECT 'template:manage', '模板管理', 'template'
WHERE NOT EXISTS (SELECT 1 FROM admin_permissions WHERE permission_code = 'template:manage');

INSERT INTO admin_permissions (permission_code, permission_name, module_name)
SELECT 'category:manage', '模板分类管理', 'category'
WHERE NOT EXISTS (SELECT 1 FROM admin_permissions WHERE permission_code = 'category:manage');

INSERT INTO admin_permissions (permission_code, permission_name, module_name)
SELECT 'sensitive_word:manage', '敏感词管理', 'sensitive_word'
WHERE NOT EXISTS (SELECT 1 FROM admin_permissions WHERE permission_code = 'sensitive_word:manage');

INSERT INTO admin_permissions (permission_code, permission_name, module_name)
SELECT 'user:manage', '用户管理', 'user'
WHERE NOT EXISTS (SELECT 1 FROM admin_permissions WHERE permission_code = 'user:manage');

INSERT INTO admin_permissions (permission_code, permission_name, module_name)
SELECT 'generation_log:view', '查看生成日志', 'generation_log'
WHERE NOT EXISTS (SELECT 1 FROM admin_permissions WHERE permission_code = 'generation_log:view');

INSERT INTO admin_permissions (permission_code, permission_name, module_name)
SELECT 'operation_log:view', '查看操作日志', 'operation_log'
WHERE NOT EXISTS (SELECT 1 FROM admin_permissions WHERE permission_code = 'operation_log:view');

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'ADMIN', 'dashboard:view'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'ADMIN' AND permission_code = 'dashboard:view'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'ADMIN', 'template:manage'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'ADMIN' AND permission_code = 'template:manage'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'ADMIN', 'category:manage'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'ADMIN' AND permission_code = 'category:manage'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'ADMIN', 'sensitive_word:manage'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'ADMIN' AND permission_code = 'sensitive_word:manage'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'ADMIN', 'user:manage'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'ADMIN' AND permission_code = 'user:manage'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'ADMIN', 'generation_log:view'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'ADMIN' AND permission_code = 'generation_log:view'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'ADMIN', 'operation_log:view'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'ADMIN' AND permission_code = 'operation_log:view'
);

INSERT INTO template_categories (category_code, category_name, sort_order, is_active)
SELECT 'beauty', '美妆', 10, 1
WHERE NOT EXISTS (SELECT 1 FROM template_categories WHERE category_code = 'beauty');

INSERT INTO template_categories (category_code, category_name, sort_order, is_active)
SELECT 'skincare', '护肤', 20, 1
WHERE NOT EXISTS (SELECT 1 FROM template_categories WHERE category_code = 'skincare');

INSERT INTO template_categories (category_code, category_name, sort_order, is_active)
SELECT 'fashion', '穿搭', 30, 1
WHERE NOT EXISTS (SELECT 1 FROM template_categories WHERE category_code = 'fashion');

INSERT INTO template_categories (category_code, category_name, sort_order, is_active)
SELECT 'food', '美食', 40, 1
WHERE NOT EXISTS (SELECT 1 FROM template_categories WHERE category_code = 'food');
