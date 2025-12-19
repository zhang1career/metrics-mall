# 指标的上线状态功能与上线管理

**文档版本**: v1.0  
**创建日期**: 2025-12-14  
**作者**: Rongjin Zhang  
**文档类型**: 产品设计文档

---

## 1. 功能价值

### 业务场景
1. **指标生命周期管理**
   - 场景：指标的状态包括：开发、测试、灰度、上线到下线，需要明确的状态管理
   - 需求：能够清晰看到指标当前状态，避免误用
   - 价值：规范指标管理流程，降低运营风险

2. **上线前验证**
   - 场景：新指标上线前，需要验证计算逻辑是否正确
   - 需求：支持"测试"状态，只有测试通过才能上线
   - 需求：支持"灰度"状态，具有和线上相同的数据源，但是不允许被业务调用，用于验证
   - 价值：避免错误指标上线影响业务

3. **下线保护**
   - 场景：某个指标还在被业务系统使用，不能直接下线
   - 需求：下线前检查是否有调用，有调用则禁止下线
   - 价值：避免误下线导致业务故障

4. **状态监控**
   - 场景：运营需要知道哪些指标已上线、哪些在测试、哪些已下线
   - 需求：提供指标状态看板
   - 价值：提升运营效率，快速定位问题

### 技术价值
- **状态机管理**：规范指标状态流转，避免状态混乱
- **上线保护**：防止误操作导致业务故障
- **可追溯性**：记录状态变更历史，支持审计

## 2 功能设计

### 2.1 状态定义

**指标状态枚举**:

```java
public enum LifeStatus {
   OFFLINE(0, "下线"),    // 下线，不计算，不保留历史数据，不可被业务系统调用
   DEV(1, "开发中"),      // 研发环境，指标开发中，只能被开发环境的业务系统调用
   TEST(2, "测试中"),     // 测试环境，验证中，只能被测试环境的业务系统调用
   GRAY(3, "灰度"),       // 灰度，验证中，只能被灰度环境的业务系统调用
   ONLINE(4, "上线"),     // 上线，可被灰度/生产环境的业务系统调用
   DEPRECATED(5, "废弃"), // 废弃，不再计算，但保留历史数据
}
```

**状态流转规则**:
```
   ┌────────┐  forward   ┌────────┐  forward   ┌────────┐            ┌────────┐            ┌────────────┐ 
   │        ├───────────►│        ├───────────►│        │  forward   │        │   no use   │            │ 
   │  DEV   │ push back  │  TEST  │ push back  │  GRAY  ├───────────►│ ONLINE ├-----------►│ DEPRECATED │ 
   │        │◄-----------┤        │◄-----------┤        │            │        │            │            │ 
   └───┬────┘            └──┬─────┘            └─────┬──┘            └────┬───┘            └──────┬─────┘ 
       |                    | discard        discard |                    |                       | 
       |                    └-------┐        ┌-------┘                    |                       | 
       |                            ▼        ▼                            |                       | 
       |                         ┌──────────────┐                         |                       | 
       |       discard           │              │          discard        ▼                       | 
       └------------------------►│   OFFLINE    │◄─-----------------------------------------------┘ 
                                 │              │                           
                                 └──────────────┘                           
```

### 2.2 数据库设计

**扩展metric表**:
```sql
ALTER TABLE `metric_version` 
ADD COLUMN `life_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'metric status, 0=OFFLINE, 1=DEV, 2=TEST, 3=GRAY, 4=ONLINE, 5=DEPRECATED',
ADD COLUMN `begin_status_t` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'timestamp when metric went online, unix timestamp in milliseconds',
```

### 2.3 API设计

**1. 查询接口扩展**

**2. 状态管理接口**

**2.1 修改特定版本的指标的状态**
```
POST /api/v1/m/{metric_code}/versions/{version}/status
```

**请求体**:
```json
{
   "life_status": 4
}
```

调用[DESIGN-METRIC-VERSION-20251214.md](DESIGN-METRIC-VERSION-20251214.md)中，章节2.3的接口（更新特定版本的指标）的service层

**请求体**:
```json
{
  "life_status": 4
}
```

**响应**:
```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "metric_code": "risk_score",
    "life_status": 4,
    "begin_status_ts": 1701234567
  }
}
```

**需要注意**:
- 状态变更只能按状态流转规则进行；否则拒绝变更。
- 当状态变更到GRAY和ONLINE时，预先检查依赖指标已达到GRAY和ONLINE状态；否则拒绝变更。
- 当状态从ONLINE变更到OFFLINE时，预先检查没有下游指标依赖或API调用依赖；否则拒绝变更。
- 当状态从ONLINE变更到DEPRECATED时，预先检查全部的下游指标依赖和API调用依赖，全部依赖都要认可变更（每一项依赖，都要在依赖注册详情表中，留一条记录）；否则拒绝变更。
- 当状态从DEPRECATED变更到OFFLINE时，预先检查全部的下游指标依赖和API调用依赖，全部依赖都要认可变更（每一项依赖，都要在依赖注册详情表中，留一条记录）；否则拒绝变更。

**2.2 查询特定版本的指标的状态**
```
GET /api/v1/m/{metric_code}/versions/{version}/status
```

**响应**:
```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "metric_code": "risk_score",
    "version": 1,
    "life_status": 4,
    "begin_status_ts": 1701234567,
    "health": {
      "data_quality": 0,
      "update_before": "10 s",
      "update_ts": 1701234567890,
      "update_freq": 0,
      "api_calls_24h": 10000,
      "error_rate": 0.001,
      "alters": []
    },
    "depends": {
      "upstream_metrics": [
         {"metric_id": 10000000, "exp": "user_id IN WHITE_LIST"}
      ],
      "downstream_metrics": [
         {"metric_id": 10000001, "exp": "risk_score > 700"},
         {"metric_id": 10000002, "exp": "risk_score > 500"}
      ],
      "downstream_apis": [
        {"service": "risk_control", "qps": 300},
        {"service": "credit_approval", "qps": 200}
      ]
    }
  }
}
```

**参数说明**:
- `health.data_quality`: 健康度，枚举值：0=good, 1=warning, 2=error
- `health.update_before`: 数据新鲜度，最近一次数据更新的距今时间
- `health.update_freq`: 数据更新频率，枚举值：0=real-time, 1=hourly, 2=daily
- `health.api_calls_24h`: 最近24小时内的API调用次数
- `health.alters`: 健康度警告
- `depends.upstream_metrics`: 依赖该指标的上游指标列表及依赖表达式
- `depends.downstream_metrics`: 依赖的下游指标列表及依赖表达式
- `depends.downstream_apis`: 调用该指标的API列表及调用频率

**2.4 查询变更历史**
```
GET /api/v1/m/{metric_code}/versions/{version}/history
```

这是一个通用接口，它返回的不仅仅是状态变更的信息，而是全部字段变更的信息。

**响应**:
```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "metric_code": "risk_score",
    "history": [
      {
        "from": {
          "life_status": 1,
          "begin_status_ts": 123456789000
        },
        "to": {
          "life_status": 2,
          "begin_status_ts": 123456789000
        },
        "operator": "admin",
        "ct": 1701234567
      }
    ]
  }
}
```

**2.5 批量查询指标详情**
```
GET /api/v1/m?metric_code={metric_code}&life_status={life_status}
```

***参数说明**:
- `metric_code`: 可选，过滤指标代码，多个指标用逗号分隔；
- `life_status`: 可选，过滤指标状态，多个状态用逗号分隔。

**响应**:
```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "metrics": [
      {
        "metric_code": "risk_score",
        "version": 1001,
        "life_status": 5,
        "begin_status_ts": 1701234567
      },
      {
        "metric_code": "coin_balance",
        "version": 1002,
        "life_status": 4,
        "begin_status_ts": 1700000000
      },
      {
        "metric_code": "login_count",
        "version": 0,
        "life_status": 2,
        "begin_status_ts": 0
      }
    ]
  }
}
```

### 2.4 上线管理流程

**1. 指标上线流程**
```
1. 开发阶段（DEV）
   - 创建指标定义
   - 配置计算逻辑
   - 配置维度关系

2. 测试阶段（TEST）
   - 提交测试申请
   - 系统启动Flink任务（测试环境）
   - 验证数据正确性
   - 使用API Playground验证

3. 灰度阶段（GRAY）
   - 测试通过后，提交灰度申请
   - 系统检查：
     a. 依赖指标是否已灰度/上线
     b. 计算逻辑是否完整
     c. 维度配置是否正确
   - 启动灰度环境Flink任务
   - 开始计算并写入Redis/ClickHouse
   - 状态变更为GRAY

4. 上线阶段（ONLINE）
   - 测试通过后，提交上线申请
   - 系统检查：
     a. 依赖指标是否已上线
     b. 计算逻辑是否完整
     c. 维度配置是否正确
   - 启动生产环境Flink任务
   - 开始计算并写入Redis/ClickHouse
   - 状态变更为ONLINE

5. 监控阶段
   - 监控数据质量
   - 监控API调用量
   - 如有问题，快速下线
```

**2. 指标废弃流程**
```
1. 废弃申请
   - 提交废弃申请，填写下线原因

2. 影响面评估
   - 系统自动检查：
     a. 是否有API调用
     b. 是否有下游指标依赖
     c. 是否有告警规则使用

3. 废弃决策
   - 如果没有调用或依赖：直接废弃
   - 如果有调用或依赖：
     a. 通知相关服务停止调用
     b. 等待全部依赖认可变更（每一项依赖，都要在依赖注册详情表中，留一条记录）

4. 执行废弃
   - 停止Flink计算任务
   - 状态变更为DEPRECATED
   - 保留历史数据
```

**3. 指标下线流程**
```
1. 下线申请
   - 提交下线申请，填写下线原因

2. 影响面评估
   - 系统自动检查：
     a. 是否有API调用
     b. 是否有下游指标依赖
     c. 是否有告警规则使用

3. 下线决策
   - 如果没有调用：直接下线
   - 如果有调用：
     a. 通知相关服务停止调用
     b. 等待依赖都要认可变更（每一项依赖，都要在依赖注册详情表中，留一条记录）

4. 执行下线
   - 停止Flink计算任务
   - 状态变更为OFFLINE
   - 标记历史数据为可释放
```

### 2.5 状态监控

**状态看板**:
```
GET /api/v1/m/dashboard
```

**响应**:
```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "summary": {
      "total": 100,
      "dev": 10,
      "test": 5,
      "gray": 80,
      "online": 80,
      "deprecated": 80,
      "offline": 3
    },
    "recent_history": [
      {
        "metric_code": "coin_balance",
        "version": 1001,
        "event": 1,
        "from": {
          "life_status": 1,
          "begin_status_ts": 123456789000
        },
        "to": {
          "life_status": 2,
          "begin_status_ts": 123456789000
        },
        "operator": "admin",
        "ct": 1701234567
      }
    ],
    "health_alerts": [
      {
        "metric_code": "coin_balance",
        "version": 1001,
        "alert": "数据更新延迟超过1分钟",
        "level": 1
      }
    ]
  }
}
```

***参数说明**:
- `health_alerts.level`: 警告级别，枚举值：0=info, 1=warning, 2=error, 3=critical

### 2.6 保护机制

**1. 上线保护**
- 依赖检查：如果依赖的指标未上线，给出警告
- 计算逻辑检查：验证计算逻辑是否完整
- 维度配置检查：验证维度关系是否正确

**2. 废弃保护**
- 调用检查：如果有API调用或者下游指标依赖，禁止废弃
- 审批流程：废弃流程需要审批

**3. 下线保护**
- 调用检查：如果有API调用或者下游指标依赖，禁止下线
- 审批流程：下线流程需要审批

**4. 状态一致性**
- 状态变更必须通过API，不允许直接修改数据库
- 状态变更记录到op_log表，支持审计
- 状态变更通知相关服务（可选）


## 3. 实施优先级

- 指标上线状态管理：高优先级，MVP后立即实现
- 状态监控看板：低优先级，Beta阶段实现


## 4. 技术实现要点

## 4.1 数据库迁移
- 需要为现有指标设置默认状态（ONLINE）和默认版本（1）
- 需要迁移历史数据，确保兼容性

## 4.2 Flink任务改造
- 支持读取指标状态配置
- 根据状态决定是否计算（DEPRECATED和OFFLINE状态不计算）

## 4.3 API改造
- GMS接口增加状态检查
- 增加状态管理接口

## 4.4 监控告警
- 监控指标状态变更
- 告警异常状态（如OFFLINE状态的指标被调用）
