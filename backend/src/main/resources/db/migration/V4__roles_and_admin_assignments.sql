ALTER TABLE users
ADD COLUMN role_code VARCHAR(32) NULL AFTER role;

UPDATE users
SET role_code = role
WHERE role_code IS NULL;

CREATE TABLE IF NOT EXISTS admin_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(32) NOT NULL UNIQUE,
    role_name VARCHAR(64) NOT NULL,
    description_text VARCHAR(255),
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO admin_roles (role_code, role_name, description_text, is_active)
SELECT 'ADMIN', '超级管理员', '拥有全部后台权限', 1
WHERE NOT EXISTS (SELECT 1 FROM admin_roles WHERE role_code = 'ADMIN');

INSERT INTO admin_roles (role_code, role_name, description_text, is_active)
SELECT 'OPS', '运营管理员', '负责模板、敏感词、日志查看', 1
WHERE NOT EXISTS (SELECT 1 FROM admin_roles WHERE role_code = 'OPS');

INSERT INTO admin_roles (role_code, role_name, description_text, is_active)
SELECT 'AUDITOR', '审计管理员', '负责查看后台数据和日志', 1
WHERE NOT EXISTS (SELECT 1 FROM admin_roles WHERE role_code = 'AUDITOR');

INSERT INTO admin_permissions (permission_code, permission_name, module_name)
SELECT 'role:manage', '角色权限管理', 'role'
WHERE NOT EXISTS (SELECT 1 FROM admin_permissions WHERE permission_code = 'role:manage');

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'ADMIN', 'role:manage'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'ADMIN' AND permission_code = 'role:manage'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'OPS', 'dashboard:view'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'OPS' AND permission_code = 'dashboard:view'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'OPS', 'template:manage'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'OPS' AND permission_code = 'template:manage'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'OPS', 'category:manage'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'OPS' AND permission_code = 'category:manage'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'OPS', 'sensitive_word:manage'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'OPS' AND permission_code = 'sensitive_word:manage'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'OPS', 'generation_log:view'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'OPS' AND permission_code = 'generation_log:view'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'OPS', 'operation_log:view'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'OPS' AND permission_code = 'operation_log:view'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'AUDITOR', 'dashboard:view'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'AUDITOR' AND permission_code = 'dashboard:view'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'AUDITOR', 'generation_log:view'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'AUDITOR' AND permission_code = 'generation_log:view'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'AUDITOR', 'operation_log:view'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'AUDITOR' AND permission_code = 'operation_log:view'
);
