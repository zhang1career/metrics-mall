# RESTful API Documentation

## Overview
This document describes the RESTful API interfaces of the metrics mall system, including management interfaces for metrics snapshot, aggregate and management.

## Basic Information
- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **Response Format**: Unified use of `ApiResponse` wrapper

### ApiResponse Format
```json
{
  "code": 0,
  "errmsg": "",
  "data": {}
}
```
- `code`: 0 indicates success, non-zero indicates failure
- `errmsg`: Response message
- `data`: Response data

错误码设计

| Code | Status | Message | 触发场景 | 处理建议 |
| ---- | ------ | ------- | -------- | -------- |
| 40001 | 400 | Invalid Metric Code | 请求了不存在的指标 | 检查拼写或元数据同步 |
| 40002 | 400 | Time Range Too Large | GMA接口查了超过3个月的小时级数据 | 缩小时间范围或增大粒度(用天级) |
| 40003 | 400 | Cardinality Exceeded | Group By 维度值过多 | 增加 Filter 或 Limit |
| 50001 | 500 | Storage Timeout | Redis/ClickHouse 响应超时 | 重试，或联系运维 |


## 1. Metrics Query Interfaces
### 1.1. Get metric snapshot (GMS)

**Interface**: `POST /api/v1/m_snap`

**Request Body**:

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
      "code": "risk_score",
      "dims": {
        "os": "ios"
      }
    },
    {
      "code": "login_count"
    }
  ],
  "snapshot_ts": 0
}
```

**Request Parameters**:
- ec: entity code (e.g. user, device, transaction) (required)
- eid: entity identifier (required)
- metrics: array of metric query objects (required)
  - code: metric code (e.g. coin_balance, risk_score, login_count) (required)
  - dims: dimension conditions map, keys are unified dimension codes without 'dim_' prefix (e.g. {"city": "Beijing", "os": "ios"}) (optional)
- snapshot_ts: 指定快照时间（UNIX时间戳，毫秒）（可选）
  - 如果不传：不需要返回指标的快照时间信息
  - 如果传0：返回指标的最新快照时间信息
  - 如果传非0：返回指定时间点的快照值；如果指定时间点的快照值缺失，就返回早于该时间点的最近值

**Response Example**:

```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "ec": "user",
    "eid": 12345678,
    "values": {
      "coin_balance": 1050.5,
      "risk_score": 0.1,
      "login_count": 5
    },
    "_ts": { 
      "coin_balance": 1715000001000, 
      "risk_score": 1715000005000 
    }
  }
}
```

Notes:
- 空值处理：如果 Redis 里查不到 risk_score，返回 null。
- 批量接口：支持一次查多个指标，每个指标可以指定不同的维度条件。
- 维度条件：每个 metric 可以独立配置 dims，如果某个 metric 不需要维度条件，可以不传 dims 字段。
- 时间戳：默认false以节省流量，如果 snapshot_ts=true，则返回各指标的更新时间戳。


### 1.2. Get metric aggregate (GMA)

**Interface**: `POST api/v1/m_agg`

**Request Body**:

```json
{
  "metric_codes": ["pay_amount", "pay_user_cnt"],
  "time_range": {
    "start": "2024-05-20 00:00:00",
    "end": "2024-05-21 00:00:00"
  },
  "interval": "1h",
  "group_by": ["city", "os"],
  "filters": [
    {"field": "channel", "op": "=", "value": "tiktok"},
    {"field": "os", "op": "in", "value": ["ios", "android"]}
  ],
  "order_by": {"field": "pay_amount", "sort": "desc"},
  "limit": 100
}
```

Notes:
- interval：时间粒度，用于画趋势图，可选: "1m", "1h", "1d", "total" (total代表不按时间分，只算总数)
- aggregation：请求参数里不传（比如 SUM 或 AVG），是为了对调用方透明。例如，pay_amount 在 MySQL 元数据里已经定义了聚合方式是 SUM，ctr (点击率) 定义的是 AVG。调用方只需要说“我要看 pay_amount”，系统自动决定用什么聚合函数。这能防止前端传错逻辑。
- group_by：维度分组，可选，不传则查大盘数据
- filters：过滤条件，支持简单的逻辑运算
- limit：基数保护。例如，如果用户 group_by user_id（按用户分组），且这天有100万用户，接口会直接超时或炸内存。后端强制限制 Limit 最大为 1000。并在 dimension 表中检查 cardinality_limit。如果请求的维度基数过大，直接拒绝并提示“分组过多，请增加筛选条件”。


**Response Example**:

```json

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
      },
      {
        "bucket_time": "2024-05-20 10:00:00",
        "city": "Shanghai",
        "os": "Android",
        "pay_amount": 3000.00,
        "pay_user_cnt": 150
      }
    ]
  }
}

```

Notes:
- meta：元数据，告诉前端怎么格式化（来自MySQL配置）
- rows：核心数据列表
