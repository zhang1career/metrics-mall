
CREATE TABLE tenant (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id VARCHAR(64) UNIQUE NOT NULL COMMENT '租户唯一标识，格式: t_{uuid}',
    name VARCHAR(256) NOT NULL DEFAULT '' COMMENT '租户名称',
    plan_type ENUM('free', 'standard', 'pro', 'enterprise') NOT NULL DEFAULT 'free' COMMENT '套餐类型',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态: 1=active, 0=suspended, 2=deleted',
    owner_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '所有者用户ID',
    max_users INT UNSIGNED NOT NULL DEFAULT 5 COMMENT '最大用户数',
    trial_ends_at INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '试用期结束时间（Unix时间戳）',
    subscription_ends_at INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '订阅结束时间',
    ct INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间（Unix时间戳，秒）',
    ut INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_id (tenant_id),
    INDEX idx_status (status),
    INDEX idx_plan_type (plan_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';

CREATE TABLE tenant_quota (
    tenant_id VARCHAR(64) PRIMARY KEY COMMENT '租户ID',
    plan_type ENUM('free', 'standard', 'pro', 'enterprise') NOT NULL,
    event_limit_monthly BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '月度事件量限制',
    storage_limit_gb INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '存储限制（GB）',
    gms_qps_limit INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'GMS QPS限制',
    gma_qps_limit INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'GMA QPS限制',
    retention_days INT UNSIGNED NOT NULL DEFAULT 7 COMMENT '数据保留天数',
    current_usage JSON COMMENT '当前使用量: {events_this_month, storage_used_gb, ...}',
    ct INT UNSIGNED NOT NULL DEFAULT 0,
    ut INT UNSIGNED NOT NULL DEFAULT 0,
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户配额表';

CREATE TABLE tenant_usage (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    date DATE NOT NULL COMMENT '统计日期',
    event_count BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '事件数量',
    gms_requests BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'GMS请求数',
    gma_requests BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'GMA请求数',
    storage_bytes BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '存储字节数',
    ct INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_date (tenant_id, date),
    INDEX idx_date (date),
    INDEX idx_tenant_id (tenant_id),
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户使用量统计表';

CREATE TABLE tenant_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    email VARCHAR(256) NOT NULL COMMENT '邮箱（全局唯一）',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希（BCrypt）',
    name VARCHAR(128) NOT NULL DEFAULT '' COMMENT '用户姓名',
    role ENUM('owner', 'admin', 'developer', 'viewer') NOT NULL DEFAULT 'viewer' COMMENT '角色',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态: 1=active, 0=inactive',
    last_login_at INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '最后登录时间',
    ct INT UNSIGNED NOT NULL DEFAULT 0,
    ut INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_email (email),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_tenant_role (tenant_id, role),
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户用户表';

CREATE TABLE api_key (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL COMMENT '创建者用户ID',
    key_hash VARCHAR(255) NOT NULL COMMENT 'API Key哈希值',
    key_prefix VARCHAR(16) NOT NULL COMMENT 'API Key前缀（用于识别，如 mm_live_abc123）',
    name VARCHAR(128) NOT NULL DEFAULT '' COMMENT '密钥名称',
    key_type ENUM('live', 'test') NOT NULL DEFAULT 'live' COMMENT '密钥类型',
    permissions JSON COMMENT '权限: ["read", "write"]',
    rate_limit JSON COMMENT '限流配置: {gms_qps: 1000, gma_qps: 10}',
    last_used_at INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '最后使用时间',
    expires_at INT UNSIGNED DEFAULT NULL COMMENT '过期时间（NULL表示永不过期）',
    status TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态: 1=active, 0=revoked',
    ct INT UNSIGNED NOT NULL DEFAULT 0,
    ut INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_key_hash (key_hash),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_key_prefix (key_prefix),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES tenant_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API密钥表';


-- 每个租户有独立的指标定义表，表名格式为 `{tenant_id}_metric`
-- 示例: t_abc123_metric
CREATE TABLE t_abc123_metric (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(128) NOT NULL COMMENT '指标代码',
    name VARCHAR(256) NOT NULL DEFAULT '' COMMENT '指标名称（中文）',
    description TEXT COMMENT '描述',
    metric_type INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '指标类型: 0=ATOMIC, 1=DERIVED, 2=COMPOSITE',
    value_type INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '值类型: 0=STR, 1=INT, 2=LONG, 3=FLOAT, 4=BOOL, 5=DATE, 6=OBJ',
    agg_type INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '聚合类型: 0=sum, 1=avg, 2=max, 3=min, 4=count',
    unit VARCHAR(32) NOT NULL DEFAULT '' COMMENT '单位',
    validation VARCHAR(500) NOT NULL DEFAULT '' COMMENT '验证规则（JSON格式）',
    ct INT UNSIGNED NOT NULL DEFAULT 0,
    ut INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标定义表';

-- 为租户创建指标定义表
CREATE PROCEDURE create_tenant_metric_table(IN p_tenant_id VARCHAR(64))
BEGIN
    SET @sql = CONCAT('CREATE TABLE IF NOT EXISTS ', p_tenant_id, '_metric (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        code VARCHAR(128) NOT NULL,
        name VARCHAR(256) NOT NULL DEFAULT "",
        description TEXT,
        metric_type INT UNSIGNED NOT NULL DEFAULT 0,
        value_type INT UNSIGNED NOT NULL DEFAULT 0,
        agg_type INT UNSIGNED NOT NULL DEFAULT 0,
        unit VARCHAR(32) NOT NULL DEFAULT "",
        validation VARCHAR(500) NOT NULL DEFAULT "",
        ct INT UNSIGNED NOT NULL DEFAULT 0,
        ut INT UNSIGNED NOT NULL DEFAULT 0,
        PRIMARY KEY (id),
        UNIQUE KEY uk_code (code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci');
    
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END;

CREATE TABLE t_abc123_dim (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(128) NOT NULL,
    name VARCHAR(256) NOT NULL DEFAULT '',
    value_type INT UNSIGNED NOT NULL DEFAULT 0,
    validation VARCHAR(500) NOT NULL DEFAULT '',
    card_limit INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '基数限制',
    ct INT UNSIGNED NOT NULL DEFAULT 0,
    ut INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维度定义表';

CREATE TABLE t_abc123_entity_meta (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(128) NOT NULL,
    name VARCHAR(256) NOT NULL DEFAULT '',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体元数据表';

CREATE TABLE t_abc123_metric_lineage (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    src_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '源指标ID',
    dest_id BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '目标指标ID',
    depend_type INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '依赖类型: 0=derived, 1=aggregated, 2=joined',
    trans_logic TEXT COMMENT '转换逻辑（SQL或Flink代码）',
    ct INT UNSIGNED NOT NULL DEFAULT 0,
    ut INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_depend (src_id, dest_id),
    INDEX idx_dest (dest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标血缘表';

-- 所有租户共享一张表，通过 tenant_id 字段区分
CREATE TABLE metrics_all_tenants (
    tenant_id String,
    metric_name String,
    timestamp UInt64,
    dims Map(String, String),
    value Float64
) ENGINE = MergeTree()
PARTITION BY (tenant_id, toYYYYMM(toDateTime(timestamp)))
ORDER BY (tenant_id, metric_name, timestamp)
SETTINGS index_granularity = 8192;

-- 创建物化视图（按租户预聚合）
CREATE MATERIALIZED VIEW metrics_daily_mv
ENGINE = SummingMergeTree()
PARTITION BY (tenant_id, toYYYYMM(date))
ORDER BY (tenant_id, metric_name, date)
AS SELECT
    tenant_id,
    metric_name,
    toDate(toDateTime(timestamp)) AS date,
    sum(value) AS total_value,
    count() AS event_count
FROM metrics_all_tenants
GROUP BY tenant_id, metric_name, date;

-- 企业版租户使用独立表
CREATE TABLE metrics_t_enterprise_123 (
    metric_name String,
    timestamp UInt64,
    dims Map(String, String),
    value Float64
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(toDateTime(timestamp))
ORDER BY (metric_name, timestamp);
