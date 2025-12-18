# Metrics Mall 项目实施计划

**文档版本**: v1.0  
**创建日期**: 2025-01-16  
**作者**: Metrics Mall Team

---

## 目录

1. [总计划概览](#1-总计划概览)
2. [MVP阶段详细计划](#2-mvp阶段详细计划-week-1-4)
3. [Alpha阶段详细计划](#3-alpha阶段详细计划-week-5-8)
4. [Beta阶段详细计划](#4-beta阶段详细计划-week-9-12)
5. [部署方案](#5-部署方案)
6. [测试策略](#6-测试策略)

---

## 1. 总计划概览

### 1.1 阶段划分

| 阶段 | 时间 | 目标 | 关键交付物 | 验收标准 |
|------|------|------|-----------|----------|
| **MVP** | Week 1-4 | 核心链路打通，单机部署，支持1个指标 | 可运行的端到端系统 | GMS延迟<10ms，GMA延迟<1s，系统可用性>99% |
| **Alpha** | Week 5-8 | 完整功能，管理后台上线，1个客户试用 | 管理后台、多指标支持 | 支持10+指标，1个客户成功接入 |
| **Beta** | Week 9-12 | 商业化准备，性能达标，文档完善，3个付费客户 | 计费系统、完整文档 | 性能达标，3个付费客户，NPS>50 |

### 1.2 技术架构概览

```
┌─────────────┐
│   Client    │ (SDK)
└──────┬──────┘
       │ JSON Events
       │
┌──────▼──────────┐
│     Kafka      │ (消息队列)
└──────┬──────────┘
       │
┌──────▼──────────┐
│     Flink       │ (流处理)
└──────┬──────────┘
       │
   ┌───┴───┐
   │       │
┌──▼──┐ ┌──▼────────┐
│Redis│ │ClickHouse │
│(GMS)│ │  (GMA)    │
└──┬──┘ └──┬────────┘
   │       │
┌──▼───────▼──┐
│   API Service│ (SpringBoot)
└──────────────┘
       │
┌──────▼──────┐
│    MySQL    │ (元数据)
└─────────────┘
```

---

## 2. MVP阶段详细计划 (Week 1-4)

### 2.1 Week 1-2: 基础设施搭建与快照写入功能设计

#### 2.1.1 任务清单

- [ ] Kafka集群部署（3节点）
- [ ] Redis Cluster部署（3主3从）
- [ ] ClickHouse单节点部署
- [ ] MySQL数据库初始化
- [ ] 基础监控配置（Prometheus + Grafana）
- [ ] **快照写入功能接口设计与数据模型设计**
  - 完成快照写入API接口详细设计（POST /api/v1/m_snap/write）
  - 完成快照查询API接口详细设计（POST /api/v1/m_snap）
  - 完成Redis Hash数据模型设计（热存储）
  - 完成ClickHouse表结构设计（冷存储，MVP阶段使用JSON+物化列混合方案）
  - 完成DTO和POJO类设计（MetricSnapshotWriteQO、MetricSnapshotWriteDTO等）
  - 完成op_log表设计（用于可解释性追溯）

#### 2.1.2 数据库Schema

**MySQL Schema (元数据)**

```sql
-- 指标定义表
CREATE TABLE `metric` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(128) UNIQUE NOT NULL DEFAULT '' COMMENT 'metric name in english',
    `name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT 'metric name in chinese',
    `description` TEXT,
    `metric_type` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '0=ATOMIC, 1=DERIVED, 2=COMPOSITE',
    `value_type`  INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '0=STR, 1=INT, 2=LONG, 3=FLOAT, 4=BOOL, 5=DATE, 6=OBJ',
    `agg_type`    INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '0=sum, 1=avg, 2=max, 3=min, 4=count',
    `unit`        VARCHAR(32)  NOT NULL DEFAULT '' COMMENT 'unit of the metric',
    `validation`  VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'valid range, json format',
    `op_id`       BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '操作日志ID，用于可解释性追溯',
    `client_write_only` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否仅允许客户端写入：0=否，1=是',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Update time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_metric_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='metric meta';

-- 操作日志表（用于可解释性追溯）
CREATE TABLE `op_log` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `event`       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '操作类型：0=create_metric, 1=create_metric_version, 2=update_metric, 3=set_main, 4=rollback',
    `table_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '操作的表名：metric, metric_version等',
    `record_id`  BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '操作的记录ID',
    `src`         TEXT COMMENT '操作前的快照（JSON格式），用于恢复',
    `mod`         TEXT COMMENT '修改详情（JSON格式），记录变更内容',
    `operator`    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '操作者用户ID',
    `ct`          INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间，UNIX时间戳（秒）',
    PRIMARY KEY (`id`),
    INDEX `idx_table_record` (`table_name`, `record_id`),
    INDEX `idx_event` (`event`),
    INDEX `idx_operator` (`operator`),
    INDEX `idx_ct` (`ct`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表，用于记录所有对metric表及相关表的修改操作';

-- 实体元数据表
CREATE TABLE `entity_meta` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(128) UNIQUE NOT NULL DEFAULT '' COMMENT 'user, device, trans',
    `name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT '用户, 设备, 交易单',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_entity_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='entity meta';

-- 维度定义表
CREATE TABLE `dim` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(128) UNIQUE NOT NULL DEFAULT '' COMMENT 'dimension code',
    `name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT 'dimension name in chinese',
    `value_type`  INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'dimension type',
    `validation`  VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'valid range, json format',
    `card_limit`  INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'max value of cardinality',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_dim_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='dimension definition';

-- 实体与维度关联表
CREATE TABLE `x` (
    `eid`         BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '关联的entity',
    `mid`         BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '关联的metric_meta',
    `alias`       VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'dimension alias',
    `data_uri`    VARCHAR(1024)   NOT NULL DEFAULT '' COMMENT '数据来源的地址',
    `is_hot`      TINYINT         NOT NULL DEFAULT 0 COMMENT '是否热维度，0=no, 1=yes',
    PRIMARY KEY (`eid`, `mid`),
    UNIQUE INDEX `uni_entity_dim_alias` (`eid`, `alias`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='entity and metric relationship';
```

**ClickHouse Schema (冷存储)**

```sql
-- 指标明细表
CREATE TABLE metrics_detail (
    `metric_name` String COMMENT '指标代码',
    `timestamp`   UInt64 COMMENT '事件时间戳（毫秒）',
    `dims`        Map(String, String) COMMENT '动态维度',
    `value`       Float64 COMMENT '指标值',
    `date`        Date DEFAULT toDate(toDateTime(timestamp / 1000)) COMMENT '日期分区'
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (metric_name, date, timestamp)
SETTINGS index_granularity = 8192;

-- 快照日志表（MVP阶段：JSON + 物化列混合方案）
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
PARTITION BY toYYYYMMDD(event_time)
TTL event_time + INTERVAL 6 MONTH;
```

#### 2.1.3 部署方案

**Kafka部署**

```yaml
# docker-compose.yml (Kafka部分)
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
  
  kafka-1:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
    ports:
      - "9092:9092"
  
  kafka-2:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
    ports:
      - "9093:9092"
  
  kafka-3:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 3
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9094
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3
    ports:
      - "9094:9092"
```

**Redis Cluster部署**

```yaml
# redis-cluster.yml
version: '3.8'
services:
  redis-1:
    image: redis:7-alpine
    command: redis-server --cluster-enabled yes --cluster-config-file nodes.conf --cluster-node-timeout 5000 --appendonly yes
    ports:
      - "7001:6379"
  
  redis-2:
    image: redis:7-alpine
    command: redis-server --cluster-enabled yes --cluster-config-file nodes.conf --cluster-node-timeout 5000 --appendonly yes
    ports:
      - "7002:6379"
  
  redis-3:
    image: redis:7-alpine
    command: redis-server --cluster-enabled yes --cluster-config-file nodes.conf --cluster-node-timeout 5000 --appendonly yes
    ports:
      - "7003:6379"
```

**ClickHouse部署**

```yaml
# clickhouse.yml
version: '3.8'
services:
  clickhouse:
    image: clickhouse/clickhouse-server:latest
    ports:
      - "8123:8123"  # HTTP
      - "9000:9000"  # Native
    volumes:
      - clickhouse-data:/var/lib/clickhouse
    environment:
      CLICKHOUSE_DB: metrics_mall
      CLICKHOUSE_USER: default
      CLICKHOUSE_PASSWORD: ""

volumes:
  clickhouse-data:
```

#### 2.1.4 功能测试大纲

**基础设施测试**

| 测试项 | 测试内容 | 预期结果 |
|--------|----------|----------|
| Kafka可用性 | 创建Topic，发送/消费消息 | 消息正常收发 |
| Redis Cluster | 写入/读取数据，故障转移 | 数据一致性，自动故障转移 |
| ClickHouse | 创建表，插入/查询数据 | 查询延迟<100ms |
| MySQL | 创建表，插入/查询数据 | 查询正常 |

---

### 2.2 Week 3: 数据链路开发与快照写入热存储实现

#### 2.2.1 任务清单

- [ ] Flink任务开发（数据清洗+双路分流）
- [ ] SDK开发（Java版，支持JSON上报）
- [ ] GMS接口实现（Redis查询）
- [ ] 基础API框架搭建
- [ ] **快照写入功能热存储（Redis）实现**
  - 实现快照写入接口（POST /api/v1/m_snap/write）
  - 实现Redis Hash写入逻辑（Key格式：snap:{project}:{biz_id}）
  - 实现TTL设置和过期策略（默认24小时，最长7天）
  - 实现参数校验（必填项、数量限制、大小限制、指标写入约束检查）
  - 实现op_id获取和存储逻辑（从metric表查询op_id并存储）
  - 实现Lua脚本原子操作（用于客户端写入快照数据的"读取-修改-写入"场景）
  - 实现Kafka异步发送逻辑（异步写入，不阻塞主流程）
  - 实现异常处理和错误返回
  - 完成单元测试

#### 2.2.2 API设计

**SDK上报接口（内部）**

```http
POST /api/v1/events
Content-Type: application/json
X-API-Key: {api_key}

Request Body:
[
  {
    "metric_name": "user_credit_score",
    "timestamp": 1701234567890,
    "dim": {
      "user_id": "12345",
      "region": "us-west",
      "device": "ios"
    },
    "value": 850
  }
]

Response:
{
  "code": 0,
  "errmsg": "",
  "data": {
    "accepted": 1,
    "rejected": 0
  }
}
```

**GMS接口（实时快照查询）**

```http
POST /api/v1/m_snap
Content-Type: application/json
X-API-Key: {api_key}

Request Body:
{
  "ec": "user",
  "eid": 12345678,
  "metrics": [
    {
      "code": "coin_balance",
      "dims": {
        "city": "Beijing"
      }
    }
  ],
  "snapshot_ts": 0
}

Response:
{
  "code": 0,
  "errmsg": "",
  "data": {
    "ec": "user",
    "eid": 12345678,
    "values": {
      "coin_balance": 1050.5
    },
    "_ts": {
      "coin_balance": 1715000001000
    }
  }
}
```

**快照写入接口**

```http
POST /api/v1/m_snap/write
Content-Type: application/json
X-API-Key: {api_key}

Request Body:
{
  "project": "credit_loan",
  "entity_type": "user",
  "entity_id": "u88888",
  "biz_id": "txn_20231212_001",
  "trace_id": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
  "write_mode": 0,
  "ttl_seconds": 86400,
  "metrics": {
    "risk_score": 85.5,
    "ctr_rate": 0.12
  },
  "context": {
    "client_ip": "10.0.0.1",
    "device_model": "iPhone 15",
    "campaign_id": "camp_20231212"
  }
}

Response:
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

**快照查询接口**

```http
POST /api/v1/m_snap
Content-Type: application/json
X-API-Key: {api_key}

Request Body:
{
  "biz_id": "txn_20231212_001"
}

或

{
  "trace_id": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "biz_id": "txn_20231212_001",
    "trace_id": "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
    "timestamp": 1701234567890,
    "entity": {
      "type": "user",
      "id": "u88888"
    },
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
      "device_model": "iPhone 15",
      "campaign_id": "camp_20231212"
    }
  }
}
```

#### 2.2.3 Flink任务设计

**Flink任务流程**

```java
// 伪代码
public class MetricsProcessingJob {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // 1. 从Kafka读取原始数据
        DataStream<MetricEvent> events = env
            .addSource(new FlinkKafkaConsumer<>("metrics.raw", new MetricEventSchema(), props));
        
        // 2. 数据清洗
        DataStream<MetricEvent> cleaned = events
            .filter(event -> validateEvent(event))
            .map(event -> normalizeTimestamp(event));
        
        // 3. 双路分流
        // 3.1 写入Redis (GMS)
        cleaned.addSink(new RedisSink<>(redisConfig, new MetricRedisMapper()));
        
        // 3.2 写入ClickHouse (GMA)
        cleaned.addSink(new ClickHouseSink<>(clickHouseConfig, new MetricClickHouseMapper()));
        
        env.execute("Metrics Processing Job");
    }
}
```

**Redis Key设计**

```
Key格式: mx:{entity_code}:{entity_id}:{metric_code}:{dim_suffix}
示例: mx:user:12345:coin_balance:city_Beijing

Value格式: JSON
{
  "a": 1050.5,
  "ts": 1715000001000
}

TTL: 24小时
```

#### 2.2.4 功能测试大纲

**数据链路测试**

| 测试项 | 测试内容 | 预期结果 |
|--------|----------|----------|
| SDK上报 | 通过SDK发送事件 | 事件成功写入Kafka |
| Flink处理 | 验证数据清洗和分流 | 数据正确写入Redis和ClickHouse |
| GMS查询 | 查询实时指标 | 延迟<10ms，数据准确 |
| 数据一致性 | 验证Redis和ClickHouse数据 | 数据一致 |

---

### 2.3 Week 4: 快照写入冷存储实现与查询接口

#### 2.3.1 任务清单

- [ ] GMA接口实现（ClickHouse聚合查询）
- [ ] **快照写入功能冷存储（ClickHouse）异步写入实现**
  - 实现Flink/Consumer消费逻辑（消费topic_metric_snapshot）
  - 实现数据清洗和格式转换（Flatten Map，时间戳规范化）
  - 实现ClickHouse批量写入（批次大小5000条或1秒）
  - 实现容错和重试机制（死信队列、定期重试）
- [ ] **快照查询接口实现**
  - 实现快照查询接口（POST /api/v1/m_snap）
  - 实现热存储查询逻辑（优先查询Redis，通过biz_id查询）
  - 实现冷存储查询逻辑（降级查询ClickHouse，支持biz_id和trace_id查询）
  - 实现查询降级逻辑（Redis未命中时自动降级到ClickHouse）
  - 实现op_id返回逻辑（查询结果中包含每个指标的op_id）
- [ ] 性能优化和压测
- [ ] 监控告警配置

#### 2.3.2 API设计

**GMA接口（历史聚合查询）**

```http
POST /api/v1/m_agg
Content-Type: application/json
X-API-Key: {api_key}

Request Body:
{
  "metric_codes": ["pay_amount", "pay_user_cnt"],
  "time_range": {
    "start": "2024-05-20 00:00:00",
    "stop": "2024-05-21 00:00:00"
  },
  "interval": "1h",
  "group_by": ["city", "os"],
  "filters": [
    {"field": "channel", "op": "=", "value": "tiktok"}
  ],
  "order_bys": [{"field": "pay_amount", "sort": "desc"}],
  "limit": 100
}

Response:
{
  "code": 0,
  "errmsg": "",
  "data": {
    "meta": {
      "pay_amount": {"name": "支付金额", "unit": "CNY", "precision": 2},
      "pay_user_cnt": {"name": "支付人数", "unit": "人", "precision": 0}
    },
    "rows": [
      {
        "bucket_time": "2024-05-20 10:00:00",
        "city": "Beijing",
        "os": "iOS",
        "pay_amount": 5000.00,
        "pay_user_cnt": 200
      }
    ]
  }
}
```

#### 2.3.3 性能测试大纲

**压测场景**

| 场景 | 指标 | 目标值 | 测试方法 |
|------|------|--------|----------|
| GMS查询 | P99延迟 | <10ms | JMeter压测，1000 QPS |
| GMA查询 | P95延迟 | <1s | JMeter压测，10 QPS |
| 数据写入 | 吞吐量 | 10万行/天 | 持续写入24小时 |
| 系统可用性 | 可用率 | >99% | 7x24小时监控 |

#### 2.3.4 监控告警配置

**Prometheus监控指标**

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'metrics-mall-api'
    static_configs:
      - targets: ['localhost:8080']
  
  - job_name: 'kafka'
    static_configs:
      - targets: ['localhost:9092']
  
  - job_name: 'redis'
    static_configs:
      - targets: ['localhost:6379']
```

**告警规则**

```yaml
# alert-rules.yml
groups:
  - name: metrics_mall
    rules:
      - alert: HighGMSLatency
        expr: histogram_quantile(0.99, gms_latency_seconds) > 0.01
        for: 5m
        annotations:
          summary: "GMS延迟过高"
      
      - alert: HighGMALatency
        expr: histogram_quantile(0.95, gma_latency_seconds) > 1
        for: 5m
        annotations:
          summary: "GMA延迟过高"
```

---

## 3. Alpha阶段详细计划 (Week 5-8)

### 3.1 Week 5-6: 快照写入功能测试完善与多指标支持

#### 3.1.1 任务清单

- [ ] **快照写入功能测试与文档完善**
  - 完成端到端测试（写入→查询全流程验证）
  - 完成压测验证（5000 QPS持续10分钟）
  - 完成数据一致性验证（Redis和ClickHouse数据一致性）
  - 完善API文档（Swagger文档）
  - 完善运维文档（监控告警配置说明）
- [ ] 支持10+指标
- [ ] 管理后台开发（Dashboard、指标管理、用户管理）
- [ ] API Key认证机制
- [ ] 多租户隔离（基础版）

#### 3.1.2 数据库Schema扩展

**多租户支持（基础版）**

```sql
-- 租户表
CREATE TABLE `tenant` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `tenant_id`  VARCHAR(64) UNIQUE NOT NULL COMMENT '租户唯一标识',
    `name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT '租户名称',
    `plan_type`   ENUM('free', 'standard', 'pro', 'enterprise') NOT NULL DEFAULT 'free',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1=active, 0=suspended',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- API Key表
CREATE TABLE `api_key` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `tenant_id`  VARCHAR(64) NOT NULL,
    `key_hash`    VARCHAR(255) NOT NULL COMMENT 'API Key哈希值',
    `key_prefix`  VARCHAR(16) NOT NULL COMMENT 'API Key前缀',
    `name`        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '密钥名称',
    `key_type`    ENUM('live', 'test') NOT NULL DEFAULT 'live',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1=active, 0=revoked',
    `last_used_at` INT UNSIGNED NOT NULL DEFAULT 0,
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key_hash` (`key_hash`),
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API密钥表';

-- 扩展metric表，添加tenant_id
ALTER TABLE `metric` ADD COLUMN `tenant_id` VARCHAR(64) NOT NULL DEFAULT '' AFTER `id`;
ALTER TABLE `metric` ADD INDEX `idx_tenant_id` (`tenant_id`);
```

#### 3.1.3 API设计

**管理后台API**

```http
# 用户注册
POST /api/v1/auth/register
{
  "email": "user@example.com",
  "password": "password123",
  "tenant_name": "My Company"
}

# 用户登录
POST /api/v1/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}

# 创建指标
POST /api/v1/metrics
Authorization: Bearer {jwt_token}
{
  "code": "coin_balance",
  "name": "金币余额",
  "metric_type": 0,
  "value_type": 2,
  "agg_type": 0,
  "unit": "CNY"
}

# 创建API Key
POST /api/v1/api-keys
Authorization: Bearer {jwt_token}
{
  "name": "Production Key",
  "key_type": "live"
}

# Dashboard统计
GET /api/v1/dashboard/stats
Authorization: Bearer {jwt_token}
```

#### 3.1.4 功能测试大纲

**管理后台测试**

| 测试项 | 测试内容 | 预期结果 |
|--------|----------|----------|
| 用户注册/登录 | 注册新用户，登录系统 | 注册成功，JWT Token返回 |
| 指标管理 | 创建/查询/更新/删除指标 | CRUD操作正常 |
| API Key管理 | 创建/查询/删除API Key | API Key生成正确，可正常使用 |
| 多租户隔离 | 不同租户数据隔离 | 租户A无法访问租户B数据 |

---

### 3.2 Week 7-8: 客户试用与迭代

#### 3.2.1 任务清单

- [ ] 选择1个种子客户进行试用
- [ ] 收集反馈并迭代
- [ ] 性能优化
- [ ] 文档编写（API文档、SDK文档）

#### 3.2.2 客户试用流程

1. **客户接入**
   - 提供API Key
   - SDK集成指导
   - 指标配置协助

2. **数据验证**
   - 验证数据上报正确性
   - 验证GMS/GMA查询准确性
   - 性能测试

3. **反馈收集**
   - 使用体验问卷
   - 功能需求收集
   - Bug报告

#### 3.2.3 功能测试大纲

**端到端测试**

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 完整流程 | 注册→创建指标→SDK上报→GMS查询→GMA查询 | 全流程正常 |
| 异常处理 | 无效API Key、超限请求、数据格式错误 | 错误提示清晰 |
| 性能测试 | 高并发查询、大数据量写入 | 性能达标 |

---

## 4. Beta阶段详细计划 (Week 9-12)

### 4.1 Week 9-10: 性能优化与计费系统

#### 4.1.1 任务清单

- [ ] 性能优化（达到目标指标）
- [ ] 计费系统开发
- [ ] 支付集成（支付宝/微信）
- [ ] 配额管理

#### 4.1.2 数据库Schema扩展

**计费相关表**

```sql
-- 租户配额表
CREATE TABLE `tenant_quota` (
    `tenant_id`  VARCHAR(64) PRIMARY KEY,
    `plan_type`   ENUM('free', 'standard', 'pro', 'enterprise') NOT NULL,
    `event_limit_monthly` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `storage_limit_gb` INT UNSIGNED NOT NULL DEFAULT 0,
    `gms_qps_limit` INT UNSIGNED NOT NULL DEFAULT 0,
    `gma_qps_limit` INT UNSIGNED NOT NULL DEFAULT 0,
    `retention_days` INT UNSIGNED NOT NULL DEFAULT 7,
    `current_usage` JSON COMMENT '当前使用量',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户配额表';

-- 使用量统计表
CREATE TABLE `tenant_usage` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `tenant_id`  VARCHAR(64) NOT NULL,
    `date`        DATE NOT NULL COMMENT '统计日期',
    `event_count` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `gms_requests` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `gma_requests` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `storage_bytes` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_date` (`tenant_id`, `date`),
    INDEX `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户使用量统计表';

-- 订单表
CREATE TABLE `order` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `tenant_id`  VARCHAR(64) NOT NULL,
    `order_no`   VARCHAR(64) UNIQUE NOT NULL,
    `plan_type`   ENUM('free', 'standard', 'pro', 'enterprise') NOT NULL,
    `amount`      DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `status`      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=pending, 1=paid, 2=cancelled',
    `payment_method` VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'alipay, wechat',
    `paid_at`     INT UNSIGNED NOT NULL DEFAULT 0,
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

#### 4.1.3 API设计

**计费相关API**

```http
# 查询配额
GET /api/v1/billing/quota
Authorization: Bearer {jwt_token}

Response:
{
  "code": 0,
  "data": {
    "plan_type": "standard",
    "event_limit_monthly": 1000000,
    "event_used_this_month": 500000,
    "gms_qps_limit": 1000,
    "gma_qps_limit": 10
  }
}

# 创建订单
POST /api/v1/billing/orders
Authorization: Bearer {jwt_token}
{
  "plan_type": "pro",
  "payment_method": "alipay"
}

Response:
{
  "code": 0,
  "data": {
    "order_no": "ORD20250116001",
    "amount": 299.00,
    "payment_url": "https://..."
  }
}

# 支付回调
POST /api/v1/billing/payment/callback
{
  "order_no": "ORD20250116001",
  "status": "paid",
  "sign": "..."
}
```

#### 4.1.4 功能测试大纲

**计费系统测试**

| 测试项 | 测试内容 | 预期结果 |
|--------|----------|----------|
| 配额检查 | 超限请求被拒绝 | 返回429错误 |
| 订单创建 | 创建订单，生成支付链接 | 订单创建成功 |
| 支付回调 | 模拟支付成功回调 | 订单状态更新，配额生效 |
| 使用量统计 | 验证使用量统计准确性 | 统计数据准确 |

---

### 4.2 Week 11-12: 文档完善与客户获取

#### 4.2.1 任务清单

- [ ] API文档完善（Swagger）
- [ ] SDK文档编写
- [ ] 教程和案例编写
- [ ] 获取3个付费客户
- [ ] 客户成功体系建立

#### 4.2.2 文档清单

**技术文档**

- [ ] API文档（Swagger UI）
- [ ] SDK集成文档（Java/Python/Go）
- [ ] 部署文档
- [ ] 架构设计文档

**用户文档**

- [ ] 快速开始指南
- [ ] 指标配置指南
- [ ] 常见问题FAQ
- [ ] 最佳实践案例

#### 4.2.3 功能测试大纲

**文档与客户测试**

| 测试项 | 测试内容 | 预期结果 |
|--------|----------|----------|
| 文档完整性 | 检查所有文档是否齐全 | 文档完整，易于理解 |
| 客户接入 | 新客户按照文档接入 | 30分钟内完成接入 |
| 客户满意度 | NPS调研 | NPS>50 |

---

## 5. 部署方案

### 5.1 MVP阶段部署（单机）

**部署架构**

```
┌─────────────────────────────────┐
│      Single Server              │
│                                 │
│  ┌──────────┐  ┌──────────┐   │
│  │  Kafka   │  │  Redis   │   │
│  │ (3节点)   │  │ (Cluster)│   │
│  └──────────┘  └──────────┘   │
│                                 │
│  ┌──────────┐  ┌──────────┐   │
│  │ClickHouse│  │  MySQL   │   │
│  │ (单节点)  │  │ (单节点)  │   │
│  └──────────┘  └──────────┘   │
│                                 │
│  ┌──────────┐                  │
│  │   API    │                  │
│  │ Service  │                  │
│  └──────────┘                  │
│                                 │
│  ┌──────────┐                  │
│  │  Flink   │                  │
│  │  Job     │                  │
│  └──────────┘                  │
└─────────────────────────────────┘
```

**服务器配置**

- CPU: 8核
- 内存: 16GB
- 磁盘: 500GB SSD
- 操作系统: Ubuntu 22.04 LTS

**部署步骤**

```bash
# 1. 安装Docker和Docker Compose
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# 2. 启动基础设施
docker-compose -f kafka.yml up -d
docker-compose -f redis-cluster.yml up -d
docker-compose -f clickhouse.yml up -d

# 3. 初始化MySQL
mysql -u root -p < schema-mysql.sql

# 4. 部署API服务
java -jar metrics-mall-api.jar --spring.profiles.active=prod

# 5. 提交Flink任务
flink run -c lab.zhang.data_science.metrics_mall.flink.MetricsProcessingJob metrics-mall-flink.jar
```

### 5.2 Alpha/Beta阶段部署（集群）

**部署架构**

```
┌─────────────────────────────────────────┐
│         Load Balancer                   │
└──────────────┬──────────────────────────┘
               │
    ┌──────────┼──────────┐
    │          │          │
┌───▼───┐  ┌───▼───┐  ┌───▼───┐
│ API 1 │  │ API 2 │  │ API 3 │
└───┬───┘  └───┬───┘  └───┬───┘
    │          │          │
    └──────────┼──────────┘
               │
    ┌──────────┼──────────┐
    │          │          │
┌───▼───┐  ┌───▼───┐  ┌───▼───┐
│MySQL │  │ Redis │  │ClickHouse│
│Master│  │Cluster│  │ Cluster │
└───┬───┘  └───────┘  └────────┘
    │
┌───▼───┐
│MySQL │
│Slave │
└───────┘
```

**Kubernetes部署配置**

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: metrics-mall-api
  namespace: metrics-mall
spec:
  replicas: 3
  selector:
    matchLabels:
      app: metrics-mall-api
  template:
    metadata:
      labels:
        app: metrics-mall-api
    spec:
      containers:
      - name: metrics-mall
        image: metrics-mall:latest
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: MYSQL_HOST
          value: "mysql-cluster"
        - name: REDIS_HOST
          value: "redis-cluster"
        - name: CLICKHOUSE_HOST
          value: "clickhouse-cluster"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
---
apiVersion: v1
kind: Service
metadata:
  name: metrics-mall-api
  namespace: metrics-mall
spec:
  selector:
    app: metrics-mall-api
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

---

## 6. 测试策略

### 6.1 单元测试

**覆盖率要求**: >80%

**测试范围**:
- Service层业务逻辑
- Util工具类
- 数据转换逻辑

### 6.2 集成测试

**测试场景**:
- API接口测试（GMS/GMA）
- 数据链路测试（SDK→Kafka→Flink→Redis/ClickHouse）
- 数据库操作测试
- **快照写入功能集成测试**
  - 快照写入接口测试（同步写入Redis，异步发送Kafka）
  - 快照查询接口测试（热存储查询、冷存储查询、降级逻辑）
  - 数据一致性测试（Redis和ClickHouse数据一致性验证）
  - op_id追溯测试（验证op_id正确存储和查询）
  - 并发写入测试（Lua脚本原子性验证）
  - 异常场景测试（Redis失败、Kafka失败、参数校验失败）

### 6.3 性能测试

**压测工具**: JMeter

**测试指标**:
- GMS: P99延迟<10ms，支持1000 QPS
- GMA: P95延迟<1s，支持10 QPS
- 数据写入: 支持10万行/天
- **快照写入功能性能测试**:
  - 快照写入接口: P99延迟<20ms，支持5000+ QPS
  - 快照查询接口（热数据）: P99延迟<10ms
  - 快照查询接口（冷数据）: P95延迟<1s
  - 压测场景: 5000 QPS持续10分钟

### 6.4 端到端测试

**测试场景**:
- 用户注册→创建指标→SDK上报→查询指标
- 多租户数据隔离
- 配额限制与超限处理
- **快照写入功能端到端测试**:
  - 完整流程: 写入快照→查询快照（热存储）→查询快照（冷存储）→op_id追溯
  - 客户端主导模式: 客户端提供指标值→写入快照→验证数据一致性
  - 服务端主导模式: 服务端查询指标值→写入快照→验证数据一致性
  - 动态维度测试: 写入自定义维度→查询验证→ClickHouse物化列查询
  - 可解释性测试: 查询快照→获取op_id→追溯指标定义变更历史

---

## 7. 风险与应对

| 风险项 | 影响 | 概率 | 应对措施 | 风险等级 |
|--------|------|------|----------|----------|
| 性能不达标 | 高 | 中 | 提前进行性能测试，及时优化 | 🟡 中 |
| 数据丢失 | 高 | 低 | 配置Kafka副本，定期备份 | 🟡 中 |
| 客户获取困难 | 高 | 中 | 提前准备营销材料，建立案例库 | 🟡 中 |
| 技术难点 | 中 | 中 | 预留缓冲时间，寻求外部支持 | 🟢 低 |
| **Redis写入性能不达标（快照写入）** | 高 | 中 | 1) 使用Pipeline批量操作 2) 连接池优化 3) 考虑使用Redis Cluster | 🟡 中 |
| **Kafka发送失败导致数据丢失（快照写入）** | 高 | 低 | 1) 本地日志记录 2) 死信队列 3) 定期补偿机制 | 🟡 中 |
| **ClickHouse写入延迟（快照写入）** | 中 | 中 | 1) 批量写入优化 2) 异步写入不阻塞主流程 3) 监控告警 | 🟢 低 |
| **接口设计变更（快照写入）** | 中 | 中 | 1) 提前评审接口设计 2) 保持向后兼容 3) 版本管理 | 🟡 中 |

---

## 8. 里程碑检查点

### MVP阶段检查点（Week 4结束）

- [ ] GMS延迟<10ms（P99）
- [ ] GMA延迟<1s（P95）
- [ ] 系统可用性>99%
- [ ] 支持1个指标端到端流程
- [ ] **快照写入功能验收标准**
  - [ ] 快照写入接口P99延迟<20ms
  - [ ] 快照写入接口支持5000+ QPS
  - [ ] 快照查询接口P99延迟<10ms（热数据）
  - [ ] 快照查询接口P95延迟<1s（冷数据）
  - [ ] 数据一致性验证通过（Redis和ClickHouse数据一致）
  - [ ] 单元测试覆盖率>80%
  - [ ] 压测验证通过（5000 QPS持续10分钟）
  - [ ] 接口文档完善
  - [ ] 监控告警配置完成

### Alpha阶段检查点（Week 8结束）

- [ ] 支持10+指标
- [ ] 管理后台功能完整
- [ ] 1个客户成功接入
- [ ] API文档完善
- [ ] **快照写入功能完整验收**
  - [ ] 端到端测试通过
  - [ ] 压测验证通过
  - [ ] 数据一致性验证通过
  - [ ] API文档和运维文档完善

### Beta阶段检查点（Week 12结束）

- [ ] 性能达标
- [ ] 3个付费客户
- [ ] 客户满意度NPS>50
- [ ] 文档完善

---

**文档维护**:
- 本文档应随项目进展实时更新
- 每个阶段结束后进行回顾和调整
- 关键决策和变更应记录在本文档

