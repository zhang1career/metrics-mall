# 指定时间戳的指标快照查询

**文档版本**: v1.0  
**创建日期**: 2025-12-14  
**作者**: Rongjin Zhang  
**文档类型**: 产品设计文档

---

## 1. 功能价值

### 业务场景
1. **交易场景 - 事务一致性保障**
   - 场景：支付前需要同时查询用户余额、冻结金额、风险评分等多个指标
   - 需求：必须保证所有指标是同一时刻的快照，避免在查询过程中数据变更导致的不一致
   - 价值：确保交易决策基于一致的数据状态，避免资金风险

2. **审计与合规场景**
   - 场景：需要查询历史某个时间点的指标值，用于审计或合规检查
   - 需求：能够回溯到任意历史时间点的指标快照
   - 价值：满足监管要求，支持数据追溯

3. **问题排查场景**
   - 场景：用户投诉"昨天下午3点我的余额显示错误"
   - 需求：能够查询指定时间点的指标值，验证问题
   - 价值：快速定位问题，提升问题处理效率

### 技术价值
- **数据一致性**：通过时间戳快照，保证多个指标查询的一致性
- **可追溯性**：支持历史数据查询，满足审计需求
- **性能优化**：对于历史快照查询，可以走ClickHouse，避免Redis内存压力

## 2 功能设计

### 2.1 API设计

**接口**: `POST /api/v1/m_snap`

**请求参数扩展**:
```json
{
  "ec": "user",
  "eid": 12345678,
  "metrics": [
    {
      "code": "coin_balance",
      "dims": {
        "city": "Beijing"
      }
    },
    {
      "code": "risk_score"
    }
  ],
  "snapshot_ts": 123456789000,
  "atomic": true
}
```

**参数说明**:
- `snapshot_ts`: 指定快照时间（UNIX时间戳，毫秒）（可选）
  - 不传：不返回指标的快照时间信息
  - 传0：查询最新值（走Redis，性能最优）
  - 传入非0值：查询指定时间点的快照值。当`atomic`不传或者`atomic=false`时：
    - 如果指定时间点的快照值缺失，就返回早于该时间点的最近值
    - 如果时间戳是最近24小时内：优先查Redis（如果Redis中有该时间点的数据）
    - 如果时间戳超过24小时：直接查ClickHouse
- `atomic`: 原子性快照查询（可选）
  - 当`snapshot_ts`存在且非0，且`atomic=true`时，系统保证所有指标都是该时间点的快照
    - 实现方式：对于ClickHouse查询，使用`ASOF JOIN`或时间窗口查询，确保所有指标使用相同的时间基准

**响应示例**:
```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "ec": "user",
    "eid": 12345678,
    "values": {
      "coin_balance": 1050.5,
      "risk_score": 0.1
    },
    "_ts": {
      "coin_balance": 1701234567890,
      "risk_score": 1701234567890
    },
    "_src": {
      "coin_balance": "clickhouse",
      "risk_score": "clickhouse"
    }
  }
}
```

**参数说明**:
- `_ts`: 实际的快照时间（UNIX时间戳，毫秒）
- `_src`: 数据来源

### 2.2 实现方案

**方案一：Redis + ClickHouse混合查询（推荐）**

1. **查询逻辑**:
```
IF snapshot_ts == null OR snapshot_ts == 0:
    // 查询最新值
    Redis.GET(key) -> 返回最新值
ELSE:
    current_ts = System.currentTimeMillis()
    IF snapshot_ts >= (current_ts - 24小时):
        // 最近24小时内的快照
        Redis.GET(key) -> 检查value中的timestamp字段
        IF value.timestamp >= snapshot_ts:
            RETURN value  // Redis中有该时间点的数据
        ELSE:
            ClickHouse.ASOF_QUERY(snapshot_ts) -> 查询历史快照
    ELSE:
        // 超过24小时的历史快照
        ClickHouse.ASOF_QUERY(snapshot_ts) -> 直接查ClickHouse
```

2. **Redis数据结构扩展**:
// 当前Redis Value格式
```json
{
  "a": 1050.5,
  "ts": 1701234567890
}
```

// 扩展：支持保留最近N个版本（可选，用于提升最近时间点的查询性能）
```json
{
  "a": 1050.5,
  "ts": 1701234567890,
  "h": [
    {"a": 1050.0, "ts": 1701234567000},
    {"a": 1049.5, "ts": 1701234566000}
  ]
}
```

3. **ClickHouse查询优化**:
ASOF JOIN查询，获取指定时间点之前的最新值
```sql
SELECT code,value,ts
FROM `metrics_detail`
WHERE 
  tid = {tenant_id}
  AND uid = '{entity_id}'
  AND code = '{metric_code}'
  AND ts <= {snapshot_ts}
ORDER BY ts DESC
LIMIT 1
```

**方案二：纯ClickHouse查询（简化版）**

- 优点：实现简单，不需要修改Redis结构
- 缺点：所有历史查询都走ClickHouse，性能略差
- 适用：MVP阶段，快速实现

### 2.3 性能优化

1. **缓存策略**:
   - 对于频繁查询的历史时间点，可以缓存查询结果（TTL 5分钟）
   - 缓存Key: `snapshot:{entity_code}:{entity_id}:{metric_code}:{snapshot_ts}`

2. **批量查询优化**:
   - 当`atomic=true`时，多个指标的ClickHouse查询可以合并为一个SQL
   - 使用`GROUP BY`或`ARRAY JOIN`减少查询次数

3. **时间范围限制**:
   - 限制查询时间范围：最多查询6个月前的数据（与ClickHouse TTL一致）
   - 如果`snapshot_ts`超过6个月，返回错误码`40004: Time Range Exceeded`

### 2.4 错误处理

| 错误码 | HTTP状态码 | 错误信息 | 触发场景 | 处理建议 |
|--------|-----------|---------|---------|---------|
| 40004 | 400 | Time Range Exceeded | snapshot_ts超过6个月 | 缩小时间范围 |
| 40005 | 400 | Snapshot Not Found | 指定时间点没有数据 | 检查时间戳是否正确 |
| 50002 | 500 | ClickHouse Query Timeout | ClickHouse查询超时 | 重试或联系运维 |


## 3. 实施优先级

- 简化版（支持ClickHouse查询历史快照）：高优先级，MVP后立即实现
- 完整版（支持Redis+ClickHouse混合查询）：中优先级，Alpha阶段实现


## 4. 技术实现要点

