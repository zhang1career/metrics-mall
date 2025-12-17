# 指标服务快照写入功能详细设计

**文档版本**: v1.0  
**创建日期**: 2025-12-17  
**涉及模块**: GMS Service, Redis, Kafka, Flink/ClickHouse  
**技术负责人**: 架构组  
**文档类型**: 技术设计文档

---

## 1. 总体架构设计

采用 **"同步写热 + 异步写冷"** 的双写架构策略，以平衡写入性能与数据持久性。

### 1.1 同步路径

**API Service -> Redis (Hash结构)**

- 保证业务方写完即可读（Read-your-writes）
- 写入失败则接口返回错误，保证强一致性
- 用于支撑高频、低延迟的核对查询

### 1.2 异步路径

**API Service -> Kafka -> Flink/Consumer -> ClickHouse**

- 通过消息队列削峰填谷，保证海量历史数据的落盘
- 异步写入失败不影响接口返回，但需保证最终一致性
- 用于长期归档和批量导出

### 1.3 架构图

```
业务系统
    |
    v
API Service
    |
    +---> Redis (同步写入，Hash结构) [热存储]
    |
    +---> Kafka (异步发送)
            |
            v
        Flink/Consumer
            |
            v
        ClickHouse (批量写入) [冷存储]
```

---

## 2. API 接口设计

### 2.1 写入快照接口

**Path**: `POST /api/v1/m_snap/write`

**Content-Type**: `application/json`

#### 2.1.1 Request Body 定义

```json
{
  "project": "credit_loan",          // 项目/租户标识 (必填)
  "entity_type": "user",             // 实体类型 (必填)
  "entity_id": "u88888",             // 实体ID (必填)
  "biz_id": "txn_20231212_001",      // 业务流水号，将作为查询凭证 (必填)
  "trace_id": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",  // 分布式追踪ID (必填)
  "write_mode": 0,            // 写入模式: "client" | "server" | "auto" (选填，默认"client")
  "ttl_seconds": 86400,              // 热数据存活时间，默认 86400 (选填)
  "metrics": {                       // 核心指标数据 KV
    "risk_score": 85.5,              // client模式：必须提供；server模式：可选，未提供则由服务端查询
    "ctr_rate": 0.12
  },
  "context": {                       // 上下文环境数据 KV（可包含用户自定义动态维度）
    "client_ip": "10.0.0.1",         // 约定字段
    "device_model": "iPhone 15",     // 约定字段
    "custom_dim_1": "value1",         // 用户自定义动态维度
    "custom_dim_2": "value2"          // 用户自定义动态维度
  }
}
```

#### 2.1.2 参数说明

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `project` | String | 是 | 项目/租户标识，用于多租户隔离 |
| `entity_type` | String | 是 | 实体类型，如 user、device、transaction |
| `entity_id` | String | 是 | 实体ID，唯一标识一个实体 |
| `biz_id` | String | 是 | 业务流水号，作为快照查询的主键 |
| `trace_id` | String | 是 | 分布式追踪ID，用于链路追踪和审计溯源（支持OpenTelemetry TraceID格式） |
| `write_mode` | String | 否 | 写入模式："client"（客户端主导）、"server"（服务端主导）、"auto"（自动选择），默认"client" |
| `ttl_seconds` | Integer | 否 | 热数据存活时间（秒），默认 86400（24小时），最长 604800（7天） |
| `metrics` | Object | 是 | 指标数据键值对，Key 为指标名称，Value 为指标值（支持 Number 类型）。client模式必须提供所有指标值；server模式可选，未提供的指标由服务端查询 |
| `context` | Object | 否 | 上下文环境数据键值对，Key 为上下文名称，Value 为上下文值（String 类型）。可包含约定字段和用户自定义动态维度 |

#### 2.1.3 Response 定义

**200 OK**: 写入成功

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "biz_id": "txn_20231212_001",
    "write_time": 1701234567890,
    "snapshot_count": 2
  }
}
```

**400 Bad Request**: 参数缺失或 Payload 过大

```json
{
  "code": 400,
  "message": "Invalid request: biz_id is required",
  "data": null
}
```

**500 Internal Error**: Redis 写入失败（此时认为快照失败）

```json
{
  "code": 500,
  "message": "Failed to write snapshot to Redis",
  "data": null
}
```

### 2.2 查询快照接口

**Path**: `POST /api/v1/m_snap`

**Content-Type**: `application/json`

**Request Body**:
```json
{
  "bid": biz_id,
  "tid": trace_id
}
```

#### 2.2.1 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `biz_id` | String | 是（与trace_id二选一） | 业务流水号，作为查询主键 |
| `trace_id` | String | 是（与biz_id二选一） | 分布式追踪ID，用于链路追踪查询（仅支持冷存储查询） |

#### 2.2.2 Response 定义

**200 OK**: 查询成功

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "biz_id": "transaction_20251217_001",
    "trace_id": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
    "timestamp": 1701234567890,
    "entity": {
      "type": "user",
      "id": "u88888"
    },
    "metrics": {
      "risk_score": 85.5,
      "ctr_rate": 0.12
    },
    "context": {
      "client_ip": "10.0.0.1",
      "device_model": "iPhone 15",
      "custom_dim_1": "value1",
      "custom_dim_2": "value2"
    }
  }
}
```

**404 Not Found**: 快照不存在

```json
{
  "code": 404,
  "message": "Snapshot not found",
  "data": null
}
```

**400 Bad Request**: 参数缺失

```json
{
  "code": 400,
  "message": "Invalid request: biz_id or trace_id is required",
  "data": null
}
```

#### 2.2.3 查询逻辑

1. **通过 biz_id 查询**：
   - **优先查询热存储（Redis）**：
     - 通过 `biz_id` 查询 Redis Hash
     - 如果命中，直接返回数据
     - 响应时间目标：P99 < 10ms
   - **降级查询冷存储（ClickHouse）**：
     - 如果 Redis 未命中，查询 ClickHouse
     - 适用于超过 TTL 的历史数据查询
     - 响应时间目标：P95 < 1s

2. **通过 trace_id 查询**（仅支持冷存储）：
   - 直接查询 ClickHouse，通过 `trace_id` 字段查询
   - 适用于链路追踪和审计场景
   - 响应时间目标：P95 < 1s
   - 注意：`trace_id` 查询不支持热存储，因为 Redis 的 Key 是基于 `biz_id` 构建的

---

## 3. 存储层设计

### 3.1 Redis 设计 (热存储)

用于支撑高频、低延迟的核对查询。

#### 3.1.1 集群策略

- 建议将 **Snapshot Redis** 与 **State Redis** (实时状态) 物理隔离
- 防止快照写入挤占带宽或内存
- 独立的 Redis Cluster 集群，便于独立扩容和监控

#### 3.1.2 Key 命名规范

**格式**: `snap:{project}:{biz_id}`

**示例**: `snap:credit_loan:txn_20231212_001`

#### 3.1.3 数据结构

使用 **HASH** 结构存储快照数据：

**字段映射**:

| 字段名 | 说明 | 示例 |
|--------|------|------|
| `_ts` | 写入时间戳（毫秒） | `1701234567890` |
| `_trace_id` | 分布式追踪ID | `00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01` |
| `_entity` | 实体信息（entity_type:entity_id） | `user:u88888` |
| `{metric_name}` | 指标值 | `risk_score: 85.5` |
| `op_{metric_name}` | 指标操作日志ID（前缀 op_） | `op_risk_score: 12345` |
| `ctx_{context_key}` | 上下文值（前缀 ctx_） | `ctx_client_ip: 10.0.0.1` |

**示例数据**:

```
HASH Key: snap:credit_loan:txn_20231212_001
{
  "_ts": "1701234567890",
  "_trace_id": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
  "_entity": "user:u88888",
  "risk_score": "85.5",
  "op_risk_score": "12345",
  "ctr_rate": "0.12",
  "op_ctr_rate": "12346",
  "ctx_client_ip": "10.0.0.1",
  "ctx_device_model": "iPhone 15",
  "ctx_custom_dim_1": "value1",
  "ctx_custom_dim_2": "value2"
}
```

#### 3.1.4 过期策略

- **每次写入必须设置 EXPIRE**，严禁无 TTL 的 Key
- 默认 TTL: 86400 秒（24小时）
- 支持通过 `ttl_seconds` 参数自定义，最长不超过 604800 秒（7天）
- 过期后自动删除，释放内存

---

### 3.2 ClickHouse 设计 (冷存储)

用于长期归档和批量导出。

#### 3.2.1 表设计

**表名**: `dwd_metric_snapshot_log`

**引擎**: `MergeTree`

**分区**: `PARTITION BY toYYYYMMDD(event_time)`

#### 3.2.2 Schema（分阶段演进）

**短期方案（MVP阶段）：JSON + 物化列混合**

```sql
CREATE TABLE dwd_metric_snapshot_log (
    `event_time` DateTime64(3) COMMENT '快照写入时间',
    `project` LowCardinality(String),
    `entity_type` LowCardinality(String),
    `entity_id` String,
    `biz_id` String,
    `trace_id` String COMMENT '分布式追踪ID',
    
    -- 原始JSON存储（保留灵活性）
    `context_json` String COMMENT '原始上下文JSON，支持任意字段',
    
    -- 物化列（高频查询的维度，手动配置）
    `campaign_id` String MATERIALIZED JSONExtractString(context_json, 'campaign_id'),
    `ad_group_id` String MATERIALIZED JSONExtractString(context_json, 'ad_group_id'),
    `keyword` String MATERIALIZED JSONExtractString(context_json, 'keyword'),
    
    -- Map作为fallback（兼容未注册维度）
    `context_data` Map(String, String) MATERIALIZED 
        JSONExtractKeysAndValues(context_json, 'String'),
    
    -- 使用 Map 存储动态指标
    `metrics_data` Map(String, Float64) COMMENT '指标数值集合',
    `metrics_op_id` Map(String, UInt64) COMMENT '指标操作日志ID集合，用于可解释性追溯'
)
ENGINE = MergeTree()
ORDER BY (project, event_time, biz_id, trace_id)
TTL event_time + INTERVAL 6 MONTH;

-- 为高频查询的物化列创建索引
ALTER TABLE dwd_metric_snapshot_log 
ADD INDEX idx_campaign_id campaign_id TYPE bloom_filter GRANULARITY 1;
ALTER TABLE dwd_metric_snapshot_log 
ADD INDEX idx_ad_group_id ad_group_id TYPE bloom_filter GRANULARITY 1;
```

**中期方案（Beta阶段）：Schema 版本化**

```sql
-- Schema注册表（MySQL）
CREATE TABLE snapshot_dim_schema (
    id BIGINT UNSIGNED PRIMARY KEY,
    project VARCHAR(64) NOT NULL,
    schema_version INT UNSIGNED NOT NULL COMMENT 'Schema版本号',
    schema_definition JSON COMMENT '维度定义：{"campaign_id": "String", "ad_group_id": "String"}',
    is_active TINYINT DEFAULT 1 COMMENT '是否激活',
    ct INT UNSIGNED,
    ut INT UNSIGNED,
    UNIQUE KEY uk_project_version (project, schema_version)
);

-- ClickHouse表（包含schema_version）
CREATE TABLE dwd_metric_snapshot_log (
    `event_time` DateTime64(3) COMMENT '快照写入时间',
    `project` LowCardinality(String),
    `schema_version` UInt16 COMMENT '使用的schema版本',
    `entity_type` LowCardinality(String),
    `entity_id` String,
    `biz_id` String,
    `trace_id` String COMMENT '分布式追踪ID',
    
    -- 原始JSON存储
    `context_json` String COMMENT '原始上下文JSON',
    
    -- 物化列（基于schema_version条件解析）
    `campaign_id` String MATERIALIZED 
        if(schema_version >= 1, JSONExtractString(context_json, 'campaign_id'), ''),
    `ad_group_id` String MATERIALIZED 
        if(schema_version >= 1, JSONExtractString(context_json, 'ad_group_id'), ''),
    `keyword` String MATERIALIZED 
        if(schema_version >= 2, JSONExtractString(context_json, 'keyword'), ''),
    
    -- Map作为fallback
    `context_data` Map(String, String) MATERIALIZED 
        JSONExtractKeysAndValues(context_json, 'String'),
    
    `metrics_data` Map(String, Float64) COMMENT '指标数值集合',
    `metrics_op_id` Map(String, UInt64) COMMENT '指标操作日志ID集合，用于可解释性追溯'
)
ENGINE = MergeTree()
ORDER BY (project, schema_version, event_time, biz_id, trace_id)
TTL event_time + INTERVAL 6 MONTH;
```

**长期方案（GA阶段）：动态物化列注册**

```sql
-- 基础表（始终包含JSON原始数据）
CREATE TABLE dwd_metric_snapshot_log (
    `event_time` DateTime64(3) COMMENT '快照写入时间',
    `project` LowCardinality(String),
    `entity_type` LowCardinality(String),
    `entity_id` String,
    `biz_id` String,
    `trace_id` String COMMENT '分布式追踪ID',
    
    -- 原始JSON存储（必须保留）
    `context_json` String COMMENT '原始上下文JSON，支持任意字段',
    
    -- 物化列（通过动态注册自动添加）
    -- 注：物化列通过 ALTER TABLE ADD COLUMN 动态添加，此处不列出所有列
    
    -- Map作为fallback（兼容未注册维度）
    `context_data` Map(String, String) MATERIALIZED 
        JSONExtractKeysAndValues(context_json, 'String'),
    
    `metrics_data` Map(String, Float64) COMMENT '指标数值集合',
    `metrics_op_id` Map(String, UInt64) COMMENT '指标操作日志ID集合，用于可解释性追溯'
)
ENGINE = MergeTree()
ORDER BY (project, event_time, biz_id, trace_id)
TTL event_time + INTERVAL 6 MONTH;

-- Schema注册表（MySQL）- 增强版
CREATE TABLE snapshot_dim_schema (
    id BIGINT UNSIGNED PRIMARY KEY,
    project VARCHAR(64) NOT NULL,
    dim_code VARCHAR(128) NOT NULL COMMENT '维度代码',
    dim_name VARCHAR(256) COMMENT '维度名称',
    value_type INT UNSIGNED DEFAULT 0 COMMENT '0=String, 1=Int, 2=Float',
    is_materialized TINYINT DEFAULT 1 COMMENT '是否物化为列',
    priority INT UNSIGNED DEFAULT 0 COMMENT '优先级，用于决定哪些维度物化',
    query_frequency INT UNSIGNED DEFAULT 0 COMMENT '查询频率（用于智能优化）',
    ct INT UNSIGNED,
    ut INT UNSIGNED,
    UNIQUE KEY uk_project_dim (project, dim_code)
);
```

#### 3.2.3 设计说明

**短期方案（MVP阶段）**：
- **JSON + 物化列混合**：平衡性能与灵活性
- **无需预注册**：动态维度可直接使用
- **手动物化**：高频查询的维度手动配置物化列
- **向后兼容**：保留 JSON 原始数据，历史数据可用

**中期方案（Beta阶段）**：
- **Schema 版本化**：支持多版本并存
- **注册机制**：动态维度需要注册，定义完整 schema
- **版本管理**：每次注册创建新版本，历史数据无需迁移
- **写入约束**：写入时不允许动态修改已注册的 schema

**长期方案（GA阶段）**：
- **动态物化列注册**：自动化的维度注册和管理
- **智能优化**：根据查询频率自动决定是否物化
- **自动扩展**：系统自动检测新维度并创建物化列
- **完整管理**：版本管理、迁移策略、性能监控

---

## 4. 用户自定义动态维度设计

### 4.1 设计概述

用户自定义动态维度允许业务方在每次快照写入时，灵活地传入自定义的上下文信息，无需预先注册。这些动态维度与约定字段一起存储在 `context` 字段中，支持后续的查询和分析。

**设计原则**：
- **无需预注册**：动态维度可以直接在请求中传入，无需预先定义
- **灵活扩展**：支持任意键值对，满足不同业务场景的需求
- **统一存储**：动态维度与约定字段统一存储在 `context` 字段中
- **查询支持**：在冷存储（ClickHouse）中支持通过动态维度进行查询和分析

### 4.2 使用方式

#### 4.2.1 请求格式

动态维度直接在 `context` 字段中传入，与约定字段混合使用：

```json
{
  "project": "credit_loan",
  "entity_type": "user",
  "entity_id": "u88888",
  "biz_id": "txn_20231212_001",
  "trace_id": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
  "metrics": {
    "risk_score": 85.5
  },
  "context": {
    "client_ip": "10.0.0.1",           // 约定字段
    "device_model": "iPhone 15",        // 约定字段
    "campaign_id": "camp_20231212",     // 用户自定义动态维度
    "ad_group_id": "adg_001",           // 用户自定义动态维度
    "keyword": "credit card",           // 用户自定义动态维度
    "custom_attr_1": "value1"           // 用户自定义动态维度
  }
}
```

#### 4.2.2 命名规范

**约定字段**：
- 由研发方和业务方约定，如 `client_ip`、`device_model`、`user_agent` 等
- 建议使用下划线命名（snake_case）

**用户自定义动态维度**：
- 建议使用有意义的命名，避免与约定字段冲突
- 建议使用下划线命名（snake_case）
- 建议使用业务相关的命名前缀，如 `campaign_`、`ad_`、`custom_` 等
- 系统不强制命名规范，但建议遵循最佳实践

### 4.3 存储设计

#### 4.3.1 Redis 存储

在 Redis Hash 中，所有上下文信息（包括约定字段和动态维度）统一使用 `ctx_` 前缀存储：

```
HASH Key: snap:credit_loan:txn_20231212_001
{
  "_ts": "1701234567890",
  "_trace_id": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
  "_entity": "user:u88888",
  "risk_score": "85.5",
  "ctx_client_ip": "10.0.0.1",              // 约定字段
  "ctx_device_model": "iPhone 15",            // 约定字段
  "ctx_campaign_id": "camp_20231212",         // 动态维度
  "ctx_ad_group_id": "adg_001",               // 动态维度
  "ctx_keyword": "credit card",               // 动态维度
  "ctx_custom_attr_1": "value1"               // 动态维度
}
```

**设计说明**：
- 所有上下文字段统一使用 `ctx_` 前缀，便于区分和查询
- 约定字段和动态维度在存储层面无区别，统一处理
- 查询时通过字段名区分约定字段和动态维度

#### 4.3.2 ClickHouse 存储

在 ClickHouse 中，所有上下文信息存储在 `context_data` Map 字段中：

```sql
-- context_data Map 示例
{
  "client_ip": "10.0.0.1",
  "device_model": "iPhone 15",
  "campaign_id": "camp_20231212",
  "ad_group_id": "adg_001",
  "keyword": "credit card",
  "custom_attr_1": "value1"
}
```

**设计说明（分阶段演进）**：

**短期方案（MVP阶段）**：
- 使用 JSON 原始存储 + Map 类型作为 fallback
- 高频查询的维度通过物化列优化
- 约定字段和动态维度统一存储在 `context_json` 中

**中期方案（Beta阶段）**：
- 使用 JSON 原始存储 + 基于 schema_version 的物化列
- 支持多版本 schema 并存
- 查询时根据 schema_version 过滤

**长期方案（GA阶段）**：
- 使用 JSON 原始存储 + 动态物化列
- 物化列通过注册机制自动添加
- 支持智能优化和自动扩展

### 4.4 查询设计

#### 4.4.1 热存储查询（Redis）

通过 `biz_id` 查询时，返回完整的 `context` 信息，包含所有约定字段和动态维度：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "biz_id": "txn_20231212_001",
    "trace_id": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
    "timestamp": 1701234567890,
    "metrics": {
      "risk_score": {
        "value": 85.5,
        "op_id": 12345
      }
    },
    "context": {
      "client_ip": "10.0.0.1",
      "device_model": "iPhone 15",
      "campaign_id": "camp_20231212",
      "ad_group_id": "adg_001",
      "keyword": "credit card",
      "custom_attr_1": "value1"
    }
  }
}
```

**限制**：
- 热存储仅支持通过 `biz_id` 点查，不支持通过动态维度查询
- 动态维度查询需要在冷存储（ClickHouse）中实现

#### 4.4.2 冷存储查询（ClickHouse）

**短期方案（MVP阶段）：优先使用物化列**

```sql
-- 示例1：按物化列过滤（性能最优）
SELECT 
    biz_id,
    trace_id,
    metrics_data,
    context_json
FROM dwd_metric_snapshot_log
WHERE project = 'credit_loan'
  AND event_time >= '2025-12-01 00:00:00'
  AND campaign_id = 'camp_20231212'  -- 使用物化列
  AND ad_group_id = 'adg_001'         -- 使用物化列
ORDER BY event_time DESC
LIMIT 100;

-- 示例2：按物化列聚合（性能最优）
SELECT 
    campaign_id,      -- 物化列
    ad_group_id,     -- 物化列
    count(*) AS snapshot_count,
    avg(metrics_data['risk_score']) AS avg_risk_score
FROM dwd_metric_snapshot_log
WHERE project = 'credit_loan'
  AND event_time >= '2025-12-01 00:00:00'
  AND campaign_id != ''
GROUP BY campaign_id, ad_group_id
ORDER BY snapshot_count DESC;

-- 示例3：未物化的维度使用JSON或Map（fallback）
SELECT 
    JSONExtractString(context_json, 'custom_dim_1') AS custom_dim_1,
    context_data['custom_dim_2'] AS custom_dim_2,
    count(*) AS snapshot_count
FROM dwd_metric_snapshot_log
WHERE project = 'credit_loan'
  AND event_time >= '2025-12-01 00:00:00'
  AND has(context_data, 'custom_dim_1')
GROUP BY custom_dim_1, custom_dim_2
ORDER BY snapshot_count DESC;
```

**中期方案（Beta阶段）：基于 schema_version 查询**

```sql
-- 示例1：按 schema_version 和物化列过滤
SELECT 
    biz_id,
    trace_id,
    metrics_data,
    context_json
FROM dwd_metric_snapshot_log
WHERE project = 'credit_loan'
  AND schema_version = 1  -- 指定schema版本
  AND event_time >= '2025-12-01 00:00:00'
  AND campaign_id = 'camp_20231212'
  AND ad_group_id = 'adg_001'
ORDER BY event_time DESC
LIMIT 100;

-- 示例2：跨版本查询（兼容多个版本）
SELECT 
    campaign_id,
    ad_group_id,
    count(*) AS snapshot_count
FROM dwd_metric_snapshot_log
WHERE project = 'credit_loan'
  AND schema_version IN (1, 2)  -- 支持多版本
  AND event_time >= '2025-12-01 00:00:00'
  AND campaign_id != ''
GROUP BY campaign_id, ad_group_id
ORDER BY snapshot_count DESC;
```

**长期方案（GA阶段）：智能查询优化**

```sql
-- 系统自动选择最优查询方式
-- 如果维度已物化，使用物化列；否则使用JSON或Map

-- 示例1：物化列查询（自动优化）
SELECT 
    campaign_id,      -- 系统自动识别为物化列
    ad_group_id,      -- 系统自动识别为物化列
    count(*) AS snapshot_count
FROM dwd_metric_snapshot_log
WHERE project = 'credit_loan'
  AND event_time >= '2025-12-01 00:00:00'
  AND campaign_id = 'camp_20231212'
GROUP BY campaign_id, ad_group_id;

-- 示例2：未物化维度查询（自动fallback）
SELECT 
    JSONExtractString(context_json, 'custom_dim_1') AS custom_dim_1,
    count(*) AS snapshot_count
FROM dwd_metric_snapshot_log
WHERE project = 'credit_loan'
  AND event_time >= '2025-12-01 00:00:00'
  AND has(context_data, 'custom_dim_1')
GROUP BY custom_dim_1;
```

**设计说明**：
- **短期**：优先使用物化列查询，未物化的维度使用 JSON 或 Map fallback
- **中期**：基于 schema_version 进行版本化查询，支持多版本并存
- **长期**：系统智能选择最优查询方式，自动优化查询性能
- **通用建议**：
  - 查询时优先使用索引字段（如 `project`、`event_time`）进行过滤
  - 物化列查询性能最优，建议高频查询的维度进行物化
  - 使用 `has()` 函数检查维度是否存在，避免空值错误

### 4.5 限制和约束

#### 4.5.1 数量限制

- **单个快照的上下文字段总数**：建议不超过 20 个（包括约定字段和动态维度）
- **单个请求的 Payload 大小**：不超过 10KB（包括所有字段）
- **动态维度键名长度**：建议不超过 64 个字符
- **动态维度值长度**：建议不超过 256 个字符

#### 4.5.2 命名约束

- **键名规范**：
  - 建议使用字母、数字、下划线组成
  - 建议使用小写字母
  - 避免使用特殊字符（如空格、中划线等）
- **值类型**：
  - 目前仅支持字符串类型
  - 数值类型建议转换为字符串存储
  - 复杂对象建议序列化为 JSON 字符串

#### 4.5.3 性能考虑

- **热存储（Redis）**：
  - 动态维度会增加 Redis Hash 的字段数量
  - 建议控制动态维度的数量，避免单个 Hash 过大
  - 单个 Hash 的字段数建议不超过 50 个
- **冷存储（ClickHouse）**：
  - Map 类型的查询性能取决于数据分布和查询条件
  - 建议在查询时使用索引字段（如 `project`、`event_time`）进行过滤
  - 对于高频查询的动态维度，可以考虑创建物化视图

### 4.6 动态维度管理功能（分阶段实现）

#### 4.6.1 短期方案（MVP阶段）：基础支持

**功能**：
- 动态维度无需注册，可直接使用
- 支持在请求中传入任意键值对
- 高频查询的维度手动配置物化列

**实现方式**：
- 写入时直接存储到 `context_json`
- 物化列通过 DDL 手动添加
- 查询时优先使用物化列，未物化的使用 JSON/Map

#### 4.6.2 中期方案（Beta阶段）：Schema 注册

**功能**：
- 支持动态维度注册功能
- 每次注册定义完整的 schema
- 写入时不允许动态修改已注册的 schema
- 如需修改，需要再次更新注册（创建新版本）

**表设计**：

```sql
-- Schema注册表（MySQL）
CREATE TABLE snapshot_dim_schema (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    project VARCHAR(64) NOT NULL,
    schema_version INT UNSIGNED NOT NULL COMMENT 'Schema版本号',
    schema_definition JSON COMMENT '维度定义：{"campaign_id": "String", "ad_group_id": "String"}',
    is_active TINYINT DEFAULT 1 COMMENT '是否激活',
    description TEXT COMMENT '版本描述',
    ct INT UNSIGNED NOT NULL DEFAULT 0,
    ut INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_version (project, schema_version),
    INDEX idx_project (project)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快照维度Schema注册表';
```

**注册流程**：
1. 用户注册维度 → 写入 `snapshot_dim_schema` 表，创建新版本
2. 系统根据 schema_definition 自动创建物化列（如果不存在）
3. 写入数据时，必须指定 `schema_version`，系统验证是否符合该版本的 schema
4. 如需修改 schema，创建新版本，旧版本数据保持不变

**验证逻辑**：
- 写入时检查 `schema_version` 是否有效
- 检查传入的维度是否符合该版本的 schema_definition
- 不允许传入未在 schema 中定义的维度（可选，根据业务需求）

#### 4.6.3 长期方案（GA阶段）：智能动态注册

**功能**：
- 自动化的动态维度注册和管理
- 系统自动检测新维度并创建物化列
- 支持智能优化（根据查询频率自动决定是否物化）
- 完整的版本管理和迁移策略

**表设计（增强版）**：

```sql
-- Schema注册表（MySQL）- 增强版
CREATE TABLE snapshot_dim_schema (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    project VARCHAR(64) NOT NULL,
    dim_code VARCHAR(128) NOT NULL COMMENT '维度代码',
    dim_name VARCHAR(256) COMMENT '维度名称',
    value_type INT UNSIGNED DEFAULT 0 COMMENT '0=String, 1=Int, 2=Float',
    is_materialized TINYINT DEFAULT 1 COMMENT '是否物化为列',
    priority INT UNSIGNED DEFAULT 0 COMMENT '优先级，用于决定哪些维度物化',
    query_frequency INT UNSIGNED DEFAULT 0 COMMENT '查询频率（用于智能优化）',
    validation VARCHAR(500) NOT NULL DEFAULT '' COMMENT '验证规则（JSON格式）',
    card_limit INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '基数限制',
    is_required TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否必填：0=否，1=是',
    default_value VARCHAR(256) NOT NULL DEFAULT '' COMMENT '默认值',
    description TEXT COMMENT '描述',
    ct INT UNSIGNED NOT NULL DEFAULT 0,
    ut INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_dim (project, dim_code),
    INDEX idx_project (project),
    INDEX idx_materialized (is_materialized, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快照自定义维度定义表';
```

**自动注册流程**：
1. 用户写入数据时，系统检测到新的维度键
2. 系统自动注册新维度到 `snapshot_dim_schema` 表
3. 根据 `priority` 和 `query_frequency` 判断是否需要物化
4. 如果需要物化，自动执行 `ALTER TABLE ADD COLUMN` 创建物化列
5. 为物化列自动创建索引（可选）

**智能优化**：
- 监控每个维度的查询频率
- 自动将高频查询的维度物化
- 自动将低频查询的维度从物化列中移除（节省存储）
- 提供管理后台查看和手动调整

**验证逻辑**：
- 写入时检查维度值是否符合验证规则
- 检查维度值的基数是否超过限制
- 检查必填维度是否已提供
- 支持自动验证和手动验证两种模式

#### 4.6.4 动态维度统计（企业版）

所有阶段都支持动态维度的统计分析：

**功能**：
- 统计每个动态维度的使用频率
- 统计每个动态维度的基数分布
- 识别未使用的动态维度
- 分析维度查询性能

**实现方式**：
- 通过 ClickHouse 查询统计
- 定期生成统计报告
- 提供管理后台查看
- 长期方案支持自动统计和告警

### 4.7 最佳实践

#### 4.7.1 命名建议

- **使用有意义的命名**：`campaign_id` 比 `c1` 更清晰
- **使用业务前缀**：`ad_campaign_id`、`ad_group_id` 便于识别业务域
- **避免冲突**：避免与约定字段重名
- **保持一致性**：同一业务域使用统一的命名规范

#### 4.7.2 使用建议

- **控制数量**：建议每个快照的动态维度不超过 10 个
- **控制大小**：动态维度的值建议保持简洁，避免存储大文本
- **合理使用**：动态维度适用于业务相关的上下文信息，不适用于指标数据
- **文档化**：建议在业务文档中记录动态维度的含义和使用场景

#### 4.7.3 查询优化

- **索引字段优先**：查询时优先使用 `project`、`event_time` 等索引字段
- **避免全表扫描**：避免仅使用动态维度进行查询，应结合索引字段
- **物化视图**：对于高频查询的动态维度组合，考虑创建物化视图
- **定期清理**：定期清理不再使用的动态维度数据

---

## 5. 可解释性设计（基于 op_id）

### 5.1 设计概述

为了支撑"历史交易发生时的原始指标状态"的可解释性功能，系统通过 `op_id` 字段记录每个指标定义的操作日志ID，从而支持指标定义的变更历史追溯和复原。

**核心价值**：
- **审计溯源**：支持监管合规检查，提供不可篡改的历史记录
- **可解释性**：通过 `op_id` 追溯指标定义的变更历史，理解指标值的构成逻辑
- **版本恢复**：支持通过操作日志恢复到任意历史版本的指标定义

### 5.2 op_log 表设计

**表设计（MySQL）**：

```sql
CREATE TABLE `op_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `event` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '操作类型：0=create_metric, 1=create_metric_version, 2=update_metric, 3=set_main, 4=rollback',
    `table_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '操作的表名：metric, metric_version等',
    `record_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '操作的记录ID',
    `src` TEXT COMMENT '操作前的快照（JSON格式），用于恢复',
    `mod` TEXT COMMENT '修改详情（JSON格式），记录变更内容',
    `operator` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '操作者用户ID',
    `ct` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间，UNIX时间戳（秒）',
    PRIMARY KEY (`id`),
    INDEX `idx_table_record` (`table_name`, `record_id`),
    INDEX `idx_event` (`event`),
    INDEX `idx_operator` (`operator`),
    INDEX `idx_ct` (`ct`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表，用于记录所有对metric表及相关表的修改操作';
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键，自增，作为 `op_id` 使用 |
| `event` | TINYINT | 操作类型：0=创建指标, 1=创建指标版本, 2=更新指标, 3=设置主版本, 4=回滚 |
| `table_name` | VARCHAR(64) | 操作的表名，如 `metric`、`metric_version` |
| `record_id` | BIGINT | 操作的记录ID，对应表中的主键 |
| `src` | TEXT | 操作前的快照（JSON格式），包含完整的记录内容，用于恢复 |
| `mod` | TEXT | 修改详情（JSON格式），记录变更的字段和值 |
| `operator` | BIGINT | 操作者用户ID |
| `ct` | INT | 创建时间，UNIX时间戳（秒） |

**操作记录规则**：

1. **创建指标**（`event=0`）：
   - `table_name='metric'`
   - `record_id` 为新创建的指标ID
   - `src` 为空（创建操作无前置状态）
   - `mod` 包含新指标的完整定义（JSON格式）

2. **创建指标版本**（`event=1`）：
   - `table_name='metric_version'`
   - `record_id` 为新创建的版本ID
   - `src` 为空
   - `mod` 包含新版本的完整定义（JSON格式）

3. **更新指标**（`event=2`）：
   - `table_name='metric'` 或 `'metric_version'`
   - `record_id` 为被更新的记录ID
   - `src` 包含更新前的完整记录（JSON格式）
   - `mod` 包含变更的字段和值（JSON格式）

4. **设置主版本**（`event=3`）：
   - `table_name='metric'`
   - `record_id` 为指标ID
   - `src` 包含设置前的状态（包括旧的主版本信息）
   - `mod` 包含新的主版本信息

5. **回滚操作**（`event=4`）：
   - `table_name='metric'` 或 `'metric_version'`
   - `record_id` 为被回滚的记录ID
   - `src` 包含回滚前的状态
   - `mod` 包含回滚目标的状态（从 `op_log` 中获取）

### 5.3 指标表扩展

**metric 表扩展**：

```sql
ALTER TABLE `metric` 
ADD COLUMN `op_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '达成当前状态的操作日志ID，用于可解释性追溯';
```

**metric_version 表扩展**：

```sql
ALTER TABLE `metric_version` 
ADD COLUMN `op_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '达成当前状态的操作日志ID，用于可解释性追溯';
```

**设计说明**：
- 每次对 `metric` 或 `metric_version` 表的修改操作，都会：
  1. 插入一条记录到 `op_log` 表
  2. 更新对应记录的 `op_id` 字段为刚插入的 `op_log.id`
- 通过 `op_id` 可以追溯到该记录最后一次修改的操作日志
- 通过操作日志的 `src` 字段，可以恢复到修改前的状态

### 5.4 快照写入中的 op_id 处理

#### 5.4.1 写入流程

在快照写入时，需要记录每个指标对应的 `op_id`：

1. **查询指标定义**：
   - 根据 `metric_code` 查询 `metric` 表，获取当前指标的 `op_id`
   - 如果指定了版本，查询 `metric_version` 表，获取该版本的 `op_id`

2. **存储 op_id**：
   - Redis：在 Hash 中存储 `op_{metric_name}` 字段，值为 `op_id`
   - ClickHouse：在 `metrics_op_id` Map 中存储，key 为 `metric_name`，value 为 `op_id`

3. **数据一致性**：
   - 确保 `metrics_data` 和 `metrics_op_id` 的 key 一一对应
   - 每个指标值都有对应的 `op_id`

#### 5.4.2 存储格式

**Redis 存储**：
```
HASH Key: snap:credit_loan:txn_20231212_001
{
  "risk_score": "85.5",
  "op_risk_score": "12345",  // 对应 risk_score 指标的 op_id
  "ctr_rate": "0.12",
  "op_ctr_rate": "12346"    // 对应 ctr_rate 指标的 op_id
}
```

**ClickHouse 存储**：
```sql
-- metrics_data Map
{
  "risk_score": 85.5,
  "ctr_rate": 0.12
}

-- metrics_op_id Map
{
  "risk_score": 12345,
  "ctr_rate": 12346
}
```

### 5.5 查询接口扩展

#### 5.5.1 快照查询返回格式

查询快照时，返回每个指标的 `op_id`：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "biz_id": "txn_20231212_001",
    "trace_id": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
    "timestamp": 1701234567890,
    "metrics": {
      "risk_score": {
        "value": 85.5,
        "op_id": 12345
      },
      "ctr_rate": {
        "value": 0.12,
        "op_id": 12346
      }
    },
    "context": {
      "client_ip": "10.0.0.1",
      "device_model": "iPhone 15"
    }
  }
}
```

#### 5.5.2 可解释性查询接口

**接口**: `GET /api/v1/snapshot/explain?biz_id={biz_id}&metric_code={metric_code}`

**功能**：查询指定快照中某个指标的可解释性信息

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "biz_id": "txn_20231212_001",
    "metric_code": "risk_score",
    "metric_value": 85.5,
    "op_id": 12345,
    "op_log": {
      "id": 12345,
      "event": 2,
      "table_name": "metric",
      "record_id": 1001,
      "src": {
        "id": 1001,
        "code": "risk_score",
        "name": "风险评分",
        "calc_logic": "old_logic",
        "op_id": 12344
      },
      "mod": {
        "calc_logic": "new_logic",
        "version": 2
      },
      "operator": 10001,
      "ct": 1701234567
    },
    "metric_definition": {
      "id": 1001,
      "code": "risk_score",
      "name": "风险评分",
      "calc_logic": "new_logic",
      "version": 2,
      "op_id": 12345
    },
    "change_history": [
      {
        "op_id": 12345,
        "event": 2,
        "ct": 1701234567,
        "description": "更新计算逻辑"
      },
      {
        "op_id": 12344,
        "event": 1,
        "ct": 1701234500,
        "description": "创建指标版本"
      }
    ]
  }
}
```

### 5.6 变更历史复原

#### 5.6.1 复原流程

通过 `op_id` 复原指标定义的历史状态：

1. **查询操作日志**：
   ```sql
   SELECT * FROM op_log WHERE id = {op_id};
   ```

2. **解析操作日志**：
   - 从 `src` 字段获取操作前的状态（JSON格式）
   - 从 `mod` 字段获取变更内容

3. **递归追溯**：
   - 如果 `src` 中包含 `op_id`，继续追溯该 `op_id` 的操作日志
   - 递归构建完整的变更历史链

4. **状态复原**：
   - 从最新的 `op_id` 开始，逐步向前追溯
   - 通过 `src` 字段恢复到任意历史状态

#### 5.6.2 复原示例

假设指标 `risk_score` 的变更历史：

```
op_id=12345 (更新计算逻辑) 
  -> src.op_id=12344 (创建版本v2)
    -> src.op_id=12343 (创建指标)
```

复原到 `op_id=12344` 的状态：
1. 查询 `op_log WHERE id=12344`
2. 从 `src` 字段获取该版本的定义
3. 如果需要，继续追溯 `src.op_id=12343`

### 5.7 性能考虑

#### 5.7.1 存储优化

- **op_log 表索引**：
  - `idx_table_record`：优化按表和记录ID查询
  - `idx_event`：优化按操作类型查询
  - `idx_ct`：优化按时间范围查询

- **数据归档**：
  - 定期归档历史操作日志（超过1年的数据）
  - 归档数据保留在冷存储（ClickHouse）中

#### 5.7.2 查询优化

- **缓存策略**：
  - 缓存常用的指标定义（包含 `op_id`）
  - 缓存操作日志的查询结果

- **批量查询**：
  - 支持批量查询多个指标的 `op_id`
  - 支持批量查询操作日志

### 5.8 实施建议

#### 5.8.1 分阶段实施

**第一阶段（MVP）**：
- 实现 `op_log` 表的基础功能
- 在指标修改时记录操作日志
- 在快照写入时记录 `op_id`
- 支持基本的可解释性查询

**第二阶段（Beta）**：
- 完善变更历史追溯功能
- 支持递归追溯和状态复原
- 优化查询性能

**第三阶段（GA）**：
- 完善可解释性可视化
- 支持变更历史分析
- 支持操作日志归档和恢复

---

## 6. 关键流程逻辑

### 6.1 写入流程

#### 6.1.1 参数校验

1. **必填项检查**：
   - `biz_id` 是否为空
   - `trace_id` 是否为空
   - `project` 是否为空
   - `entity_type` 是否为空
   - `entity_id` 是否为空
   - `metrics` 是否为空或为空对象（server模式时，如果指标未提供，需要检查指标定义）

2. **数量限制**：
   - `metrics` 数量是否超限（例如 < 50个）
   - `context` 数量是否超限（包括约定字段和动态维度，建议 < 20个）

3. **大小限制**：
   - JSON Payload 大小是否超过 10KB
   - 单个指标值是否在合理范围内
   - 动态维度键名长度是否超过 64 个字符
   - 动态维度值长度是否超过 256 个字符

4. **指标写入约束检查**：
   - 如果 `write_mode = "client"` 或未指定，检查所有指标是否都已提供值
   - 如果 `write_mode = "server"`，对于未提供的指标：
     - 检查指标定义中的 `clientWriteOnly` 字段
     - 如果 `clientWriteOnly = 1`，则不允许服务端查询，必须由客户端提供值
     - 如果 `clientWriteOnly = 0`，允许服务端查询实时值

#### 6.1.2 构建数据模型

将请求转换为内部 DTO：

```java
SnapshotWriteDTO {
    String project;
    String entityType;
    String entityId;
    String bizId;
    String traceId;
    String writeMode;  // 0 | 10
    Long writeTime;
    Integer ttlSeconds;
    Map<String, Number> metrics;
    Map<String, Long> metricsOpId;  // 每个指标对应的op_id
    Map<String, String> context;  // 包含约定字段和用户自定义动态维度
}
```

**op_id 获取流程**：
1. 对于每个指标（`metrics` 中的 key），查询指标定义表获取 `op_id`
2. 如果指定了版本，查询 `metric_version` 表获取该版本的 `op_id`
3. 将 `op_id` 存储到 `metricsOpId` Map 中，key 为指标名称
4. 如果指标不存在或查询失败，`op_id` 设置为 0（表示无法追溯）

#### 6.1.3 写 Redis (同步)

1. **构建 Redis Key**: `snap:{project}:{biz_id}`
2. **执行 HSET**：写入所有字段
   - 元数据字段：`_ts`, `_trace_id`, `_entity`
   - 指标值：`{metric_name}` = `{value}`
   - 指标op_id：`op_{metric_name}` = `{op_id}`
   - 上下文：`ctx_{context_key}` = `{value}`
3. **执行 EXPIRE**：设置 TTL
4. **异常处理**：
   - 若 Redis 异常，捕获异常并返回 500 给客户端（强一致性要求）
   - 记录错误日志，包含 biz_id、trace_id 和异常信息

**注意**：对于客户端写入快照数据（client-driven模式），如果需要对快照数据进行"读取-修改-写入"操作（例如更新字段 a、ts、h，将旧的 a、ts 值插入历史列表 h，并保持列表长度不超过预设值），**必须使用 Lua 脚本实现原子操作**，避免多线程/多客户端并发写入导致的数据不一致问题。

#### 6.1.4 写 Kafka (异步)

1. **序列化 DTO**：将 DTO 序列化为 JSON
2. **发送至 Topic**：`topic_metric_snapshot`
3. **异常处理**：
   - 若 Kafka 发送失败，记录本地 Error Log 或通过备用队列重试
   - **不阻断 API 返回成功**（保证 API 低延迟）
   - 异步重试机制：最多重试 3 次，指数退避

---

### 6.2 消费流程 (Flink/Consumer)

#### 6.2.1 Kafka 消费

- 消费 Topic: `topic_metric_snapshot`
- 消费组: `snapshot_consumer_group`
- 消费模式: 至少一次（At-least-once）

#### 6.2.2 数据清洗

进行简单的格式清洗（Flatten Map）：

- 将 `metrics` Map 展开为键值对
- 将 `context` Map 展开为键值对
- 时间戳规范化（统一为 DateTime64(3)）

#### 6.2.3 批量写入 ClickHouse

- 使用 JDBC Connector 或 ClickHouse Http Sink
- **批次大小**：5000条 或 1秒（满足任一条件即触发写入）
- **写入模式**：批量 INSERT，提升写入性能
- **容错机制**：
  - 写入失败时记录失败日志
  - 支持死信队列（DLQ）存储失败消息
  - 定期重试失败消息

---

## 7. 重点保障措施

### 5.1 Read-your-writes 保证

支持两种写入模式，以满足不同业务场景的需求：

**客户端主导模式 (Client-driven)**：

为了避免由服务端重新查询最新值导致的时间差（Time Travel）问题，推荐采用**"客户端传值"**模式。

**标准流程**：

```
1. 业务方 GET /latest -> 获取最新指标值
2. 业务逻辑处理 -> 基于获取的值进行业务决策
3. POST /snapshot (write_mode="client", 携带刚才 GET 到的值) -> 固化快照
```

**设计原则**：

- 系统只负责存储，不负责重新查询实时值
- 客户端必须传入完整的指标值，不能依赖服务端查询
- 这样可以避免时间窗口导致的数据不一致问题
- 对于 `clientWriteOnly=1` 的指标，必须使用客户端主导模式

**服务端主导模式 (Server-driven)**：

适用于可以接受服务端查询的场景，由服务端查询实时值并存储。

**标准流程**：

```
1. 业务方 POST /snapshot (write_mode="server", 部分指标值可选) -> 服务端查询并固化快照
2. 服务端对于未提供的指标，调用 GMS 实时查询接口获取当前值
3. 服务端将完整的指标快照存储
```

**设计原则**：

- 服务端负责查询未提供的指标值
- 对于 `clientWriteOnly=1` 的指标，不允许服务端查询，必须由客户端提供
- 服务端查询会增加延迟，适用于对实时性要求不高的场景

### 5.2 内存保护

#### 5.2.1 API 层限流熔断

- **Rate Limiting**：
  - 单用户写入频率限制：100 QPS
  - 单项目写入频率限制：1000 QPS
  - 全局写入频率限制：5000 QPS

- **熔断机制**：
  - 当 Redis 写入失败率 > 10% 时，触发熔断
  - 熔断期间返回 503 Service Unavailable
  - 熔断恢复后逐步放量

#### 5.2.2 Redis 内存监控

- **内存阈值监控**：
  - 达到 80% 内存使用率时触发告警
  - 达到 90% 内存使用率时触发紧急告警
  - 达到 95% 内存使用率时触发熔断

- **内存保护策略**：
  - LRU 淘汰策略（针对无 TTL 的 Key，但本功能所有 Key 都有 TTL）
  - 监控快照 Key 的内存占用
  - 定期清理过期 Key（虽然会自动过期，但可以主动清理）

### 5.3 数据一致性保障

#### 5.3.1 热存储一致性

- Redis 写入失败则接口返回错误，保证强一致性
- 使用事务（MULTI/EXEC）确保 HSET 和 EXPIRE 的原子性
- **并发写入原子性保障**：
  - 对于客户端写入快照数据，如果需要对快照数据进行"读取-修改-写入"操作（例如更新字段 a、ts、h），必须使用 **Lua 脚本**实现原子操作
  - **问题背景**：在多线程/多客户端并发写入场景下，如果采用"先读取 Redis → 修改字段 → 再写入 Redis"的方式，会导致数据不一致：
    - 线程A读取快照数据
    - 线程B也读取快照数据（读取到相同的数据）
    - 线程A修改并写入
    - 线程B修改并写入（覆盖了线程A的修改，导致数据丢失）
  - **解决方案**：使用 Lua 脚本在 Redis 服务器端原子执行"读取-修改-写入"操作：
    - Lua 脚本在 Redis 中是单线程执行的，保证原子性
    - 脚本逻辑：读取现有快照数据 → 更新字段 a、ts → 将旧的 a、ts 值插入历史列表 h → 保持列表长度不超过 MAX_HISTORY_DEPTH → 写入新的快照数据
    - 通过 Lua 脚本确保多客户端并发写入时数据的一致性

#### 5.3.2 冷存储最终一致性

- Kafka 异步写入，保证最终一致性
- 支持数据校验机制，定期对比 Redis 和 ClickHouse 的数据一致性
- 提供数据修复工具，支持手动补偿

### 5.4 性能保障

#### 5.4.1 写入性能

- **目标 SLA**：
  - P99 延迟 < 20ms
  - P95 延迟 < 10ms
  - 支持 5000+ QPS

- **优化措施**：
  - Redis 使用 Pipeline 批量操作（如果支持批量写入）
  - Kafka 使用异步发送，不阻塞主流程
  - 连接池优化，减少连接建立开销

#### 5.4.2 查询性能

- **热数据查询**（Redis）：
  - P99 延迟 < 10ms
  - 支持 10000+ QPS

- **冷数据查询**（ClickHouse）：
  - P95 延迟 < 1s
  - 支持复杂聚合查询

---

## 8. 监控与告警

### 6.1 关键指标监控

| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| 写入 QPS | 快照写入请求量 | > 6000 QPS |
| 写入延迟 P99 | 接口响应时间 | > 30ms |
| Redis 写入失败率 | Redis 写入失败比例 | > 1% |
| Kafka 发送失败率 | Kafka 发送失败比例 | > 5% |
| ClickHouse 写入延迟 | 冷存储写入延迟 | > 5s |
| Redis 内存使用率 | 内存占用比例 | > 80% |

### 6.2 告警策略

- **P0 告警**（立即处理）：
  - Redis 写入失败率 > 5%
  - Redis 内存使用率 > 90%
  - 接口可用性 < 99%

- **P1 告警**（1小时内处理）：
  - 写入延迟 P99 > 50ms
  - Kafka 发送失败率 > 10%
  - ClickHouse 写入延迟 > 10s

---

## 9. 实施计划

### 7.1 开发阶段

- **Week 1**: 接口设计与数据模型设计
  - 完成 API 接口详细设计
  - 完成 Redis 和 ClickHouse 数据模型设计
  - 完成 DTO 和 POJO 类设计

- **Week 2**: 热存储（Redis）写入实现
  - 实现 Redis Hash 写入逻辑
  - 实现 TTL 设置和过期策略
  - 实现参数校验和异常处理
  - 完成单元测试

- **Week 3**: 冷存储（ClickHouse）异步写入实现
  - 实现 Kafka 生产者逻辑
  - 实现 Flink/Consumer 消费逻辑
  - 实现 ClickHouse 批量写入
  - 实现容错和重试机制

- **Week 4**: 查询接口实现与性能优化
  - 实现快照查询接口（Redis + ClickHouse）
  - 实现查询降级逻辑
  - 性能优化和压测
  - 完成集成测试

- **Week 5**: 测试与文档完善
  - 完成端到端测试
  - 完成压测验证
  - 完善 API 文档
  - 完善运维文档

### 7.2 验收标准

- [ ] 写入接口 P99 延迟 < 20ms
- [ ] 写入接口支持 5000+ QPS
- [ ] 查询接口 P99 延迟 < 10ms（热数据）
- [ ] 查询接口 P95 延迟 < 1s（冷数据）
- [ ] 数据一致性验证通过（Redis 和 ClickHouse 数据一致）
- [ ] 单元测试覆盖率 > 80%
- [ ] 压测验证通过（5000 QPS 持续 10 分钟）
- [ ] 接口文档完善
- [ ] 监控告警配置完成

### 7.3 风险与应对

| 风险项 | 影响 | 概率 | 应对措施 | 风险等级 |
|--------|------|------|----------|----------|
| Redis 写入性能不达标 | 高 | 中 | 1) 使用 Pipeline 批量操作 2) 连接池优化 3) 考虑使用 Redis Cluster | 🟡 中 |
| Kafka 发送失败导致数据丢失 | 高 | 低 | 1) 本地日志记录 2) 死信队列 3) 定期补偿机制 | 🟡 中 |
| ClickHouse 写入延迟 | 中 | 中 | 1) 批量写入优化 2) 异步写入不阻塞主流程 3) 监控告警 | 🟢 低 |
| 接口设计变更 | 中 | 中 | 1) 提前评审接口设计 2) 保持向后兼容 3) 版本管理 | 🟡 中 |

---

## 10. 附录

### 8.1 术语表

| 术语 | 定义 |
|------|------|
| **快照 (Snapshot)** | 某一时刻指标状态的完整记录 |
| **biz_id** | 业务唯一流水号，用于关联业务事务 |
| **trace_id** | 分布式追踪ID，用于链路追踪和审计溯源 |
| **Client-driven** | 客户端主导模式，由业务方提供指标值 |
| **Server-driven** | 服务端主导模式，由服务端查询实时值 |
| **clientWriteOnly** | 指标写入约束字段，标识指标是否只能通过客户端写入 |
| **用户自定义动态维度** | 用户可以约定或定义每一次请求的上下文信息，支持动态键值对 |
| **op_id** | 操作日志ID，用于追溯指标定义的变更历史 |
| **op_log** | 操作日志表，记录所有对metric表及相关表的修改操作 |
| **可解释性** | 通过op_id追溯指标定义的变更历史，理解指标值的构成逻辑 |
| **Read-your-writes** | 写入后立即读取能获取到刚写入的数据 |
| **Time Travel** | 时间穿越问题，指查询时获取的数据与写入时不一致 |
| **热存储** | Redis，用于快速查询最近24小时的数据 |
| **冷存储** | ClickHouse，用于长期存储和复杂分析 |
| **Training-Serving Skew** | 训练数据与线上推理数据不一致的问题 |

### 8.2 更新记录

| 版本 | 日期 | 更新内容 | 作者 |
|------|------|----------|------|
| v1.0 | 2025-12-17 | 初始版本，完成快照写入功能详细设计 | 架构组 |
| v1.1 | 2025-12-17 | 合并需求文档第7、8、9章内容，补充查询接口、实施计划和术语表 | 架构组 |
| v1.2 | 2025-12-17 | 支持client-driven和server-driven两种模式，增加trace_id和用户自定义动态维度支持 | 架构组 |
| v1.3 | 2025-12-17 | 新增第4章"用户自定义动态维度设计"，详细说明动态维度的使用、存储、查询和管理 | 架构组 |
| v1.4 | 2025-12-17 | 更新动态维度实现策略：分三个阶段演进（短期JSON+物化列混合，中期Schema版本化，长期智能动态注册），更新存储设计和查询设计 | 架构组 |
| v1.5 | 2025-12-17 | 新增第5章"可解释性设计（基于op_id）"，增加op_log表设计，支持通过op_id追溯指标定义的变更历史，实现历史交易原始指标状态的可解释性 | 架构组 |
| v1.6 | 2025-12-17 | 更新数据一致性保障设计：对于客户端写入快照数据的"读取-修改-写入"操作，必须使用Lua脚本实现原子性，避免多线程/多客户端并发写入导致的数据不一致问题 | 架构组 |

---

**文档维护**：
- 本文档应随开发进展和技术方案调整定期更新
- 关键设计决策变更需记录在更新记录中
- 建议每两周回顾一次，确保设计与实现一致

