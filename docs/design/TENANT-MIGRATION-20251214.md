
[schema](../../src/main/resources/db/schema-tenant.sql)


## 5. Redis Key 设计（多租户）

### 5.1 Key 命名规范


格式: {tenant_id}:{namespace}:{key}

示例:
- t_abc123:metric:coin_balance:user_12345
- t_abc123:quota:events:202501
- t_abc123:rate_limit:gms:20250115


### 5.2 配额管理 Key

redis
# 月度事件量计数
Key: {tenant_id}:quota:events:{YYYYMM}
Value: {count}
TTL: 90 days

# QPS 限流计数
Key: {tenant_id}:rate_limit:{api_type}:{timestamp}
Value: {count}
TTL: 1 second




-- 初始化脚本

-- 1. 创建租户

-- 1.1. 创建租户记录
INSERT INTO tenant (tenant_id, name, plan_type, owner_id, ct, ut)
VALUES ('t_abc123', '示例公司', 'standard', 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP());

-- 1.2. 创建配额记录
INSERT INTO tenant_quota (tenant_id, plan_type, event_limit_monthly, storage_limit_gb,
gms_qps_limit, gma_qps_limit, retention_days, ct, ut)
VALUES ('t_abc123', 'standard', 1000000, 10, 1000, 10, 30, UNIX_TIMESTAMP(), UNIX_TIMESTAMP());

-- 1.3. 创建租户专属表
CALL create_tenant_metric_table('t_abc123');
CALL create_tenant_dim_table('t_abc123');
CALL create_tenant_entity_meta_table('t_abc123');
CALL create_tenant_metric_lineage_table('t_abc123');


-- 2. 创建用户

-- 2.1. 创建租户用户
INSERT INTO tenant_user (tenant_id, email, password_hash, name, role, ct, ut)
VALUES ('t_abc123', 'admin@example.com', '$2a$10$...', '管理员', 'owner',
UNIX_TIMESTAMP(), UNIX_TIMESTAMP());


## 7. 数据迁移脚本

### 7.1 从单租户迁移到多租户


-- 1. 添加 tenant_id 列（如果表已存在）
ALTER TABLE metric ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT 't_migrated' AFTER id;
ALTER TABLE dim ADD COLUMN tenant_id VARCHAR(64) NOT NULL DEFAULT 't_migrated' AFTER id;

-- 2. 创建租户
INSERT INTO tenant (tenant_id, name, plan_type, ct, ut)
VALUES ('t_migrated', '迁移租户', 'pro', UNIX_TIMESTAMP(), UNIX_TIMESTAMP());

-- 3. 更新现有数据的 tenant_id
UPDATE metric SET tenant_id = 't_migrated';
UPDATE dim SET tenant_id = 't_migrated';

-- 4. 重命名表（可选：迁移到租户专属表）
RENAME TABLE metric TO t_migrated_metric;
RENAME TABLE dim TO t_migrated_dim;




## 8. 索引优化建议

### 8.1 租户相关索引


-- tenant_user 表
CREATE INDEX idx_tenant_email ON tenant_user(tenant_id, email);

-- tenant_usage 表
CREATE INDEX idx_tenant_date_range ON tenant_usage(tenant_id, date);

-- api_key 表
CREATE INDEX idx_tenant_status ON api_key(tenant_id, status);


### 8.2 查询优化


-- 查询租户的指标列表（使用租户专属表）
SELECT * FROM t_abc123_metric WHERE code = 'coin_balance';

-- 查询租户的使用量
SELECT * FROM tenant_usage
WHERE tenant_id = 't_abc123'
AND date >= '2025-01-01'
AND date <= '2025-01-31';




## 9. 数据清理策略

### 9.1 过期数据清理


-- 清理过期的使用量统计（保留最近 12 个月）
DELETE FROM tenant_usage
WHERE date < DATE_SUB(CURDATE(), INTERVAL 12 MONTH);

-- 清理过期的 API Key（已撤销且超过 90 天）
DELETE FROM api_key
WHERE status = 0
AND ut < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 90 DAY));


### 9.2 租户数据清理（删除租户时）


-- 删除租户的所有数据
CREATE PROCEDURE delete_tenant_data(IN p_tenant_id VARCHAR(64))
BEGIN
-- 删除租户表
SET @sql = CONCAT('DROP TABLE IF EXISTS ', p_tenant_id, '_metric');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

    -- 删除使用量统计
    DELETE FROM tenant_usage WHERE tenant_id = p_tenant_id;
    
    -- 删除配额记录
    DELETE FROM tenant_quota WHERE tenant_id = p_tenant_id;
    
    -- 删除 API Key
    DELETE FROM api_key WHERE tenant_id = p_tenant_id;
    
    -- 删除用户
    DELETE FROM tenant_user WHERE tenant_id = p_tenant_id;
    
    -- 删除租户
    DELETE FROM tenant WHERE tenant_id = p_tenant_id;
END;



## 10. 总结

SaaS 版本的数据库设计核心要点：

1. ✅ **租户隔离**: 通过 `tenant_id` 字段或独立表实现数据隔离
2. ✅ **配额管理**: 通过 `tenant_quota` 和 `tenant_usage` 表管理资源
3. ✅ **权限控制**: 通过 `tenant_user` 和 `api_key` 表实现认证授权
4. ✅ **灵活扩展**: 支持共享表和独立表混合使用
5. ✅ **数据安全**: 外键约束、级联删除保障数据一致性
