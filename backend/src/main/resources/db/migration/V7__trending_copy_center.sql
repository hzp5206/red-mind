CREATE TABLE IF NOT EXISTS trending_copy_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_name VARCHAR(128) NOT NULL,
    platform_code VARCHAR(32) NOT NULL,
    keywords VARCHAR(500) NOT NULL,
    fetch_limit INT DEFAULT 10,
    cron_expr VARCHAR(64) NOT NULL,
    enabled TINYINT(1) DEFAULT 1,
    provider_code VARCHAR(32) NOT NULL DEFAULT 'mock',
    last_run_at DATETIME NULL,
    last_fetched_count INT DEFAULT 0,
    last_status VARCHAR(32) DEFAULT 'idle',
    last_message VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS trending_copy_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    platform_code VARCHAR(32) NOT NULL,
    source_id VARCHAR(128) NOT NULL,
    keyword VARCHAR(128),
    title VARCHAR(255) NOT NULL,
    content_text TEXT,
    author_name VARCHAR(128),
    note_url VARCHAR(500),
    likes_count INT DEFAULT 0,
    collects_count INT DEFAULT 0,
    comments_count INT DEFAULT 0,
    heat_score INT DEFAULT 0,
    tags_json JSON NULL,
    snapshot_json JSON NULL,
    published_at DATETIME NULL,
    fetched_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_task_source (task_id, source_id)
);

INSERT INTO admin_permissions (permission_code, permission_name, module_name)
SELECT 'trending_copy:manage', '爆文采集中心', 'trending_copy'
WHERE NOT EXISTS (SELECT 1 FROM admin_permissions WHERE permission_code = 'trending_copy:manage');

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'ADMIN', 'trending_copy:manage'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'ADMIN' AND permission_code = 'trending_copy:manage'
);

INSERT INTO admin_role_permissions (role_code, permission_code)
SELECT 'OPS', 'trending_copy:manage'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_role_permissions WHERE role_code = 'OPS' AND permission_code = 'trending_copy:manage'
);

INSERT INTO trending_copy_tasks (task_name, platform_code, keywords, fetch_limit, cron_expr, enabled, provider_code, last_status, last_message)
SELECT '默认爆文采集任务', 'xiaohongshu', '护肤,美妆,穿搭', 12, '0 0 8 * * ?', 1, 'mock', 'idle', '等待首次执行'
WHERE NOT EXISTS (SELECT 1 FROM trending_copy_tasks WHERE task_name = '默认爆文采集任务');
