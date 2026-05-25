CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(128) NOT NULL UNIQUE,
    setting_value TEXT NULL,
    description_text VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO admin_permissions (permission_code, permission_name, module_name)
SELECT 'ai_setting:manage', 'AI 配置管理', 'ai_setting'
WHERE NOT EXISTS (SELECT 1 FROM admin_permissions WHERE permission_code = 'ai_setting:manage');

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'ADMIN', 'ai_setting:manage'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'ADMIN' AND permission_code = 'ai_setting:manage'
);

INSERT INTO system_settings (setting_key, setting_value, description_text)
SELECT 'ai.provider', 'mock', 'AI Provider'
WHERE NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_key = 'ai.provider');

INSERT INTO system_settings (setting_key, setting_value, description_text)
SELECT 'ai.base_url', 'https://api.openai.com', 'AI Base URL'
WHERE NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_key = 'ai.base_url');

INSERT INTO system_settings (setting_key, setting_value, description_text)
SELECT 'ai.api_key', 'replace-me', 'AI API Key'
WHERE NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_key = 'ai.api_key');

INSERT INTO system_settings (setting_key, setting_value, description_text)
SELECT 'ai.model', 'gpt-4o-mini', 'AI Model'
WHERE NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_key = 'ai.model');

INSERT INTO system_settings (setting_key, setting_value, description_text)
SELECT 'ai.chat_path', '/v1/chat/completions', 'AI Chat Path'
WHERE NOT EXISTS (SELECT 1 FROM system_settings WHERE setting_key = 'ai.chat_path');
