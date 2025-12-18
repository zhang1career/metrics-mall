# 指标的多版本功能与版本管理

**文档版本**: v1.0  
**创建日期**: 2025-12-14  
**作者**: Rongjin Zhang  
**文档类型**: 产品设计文档

---

## 1. 功能价值

### 业务场景
1. **算法迭代场景**
   - 场景：风险评分算法从v1升级到v2，需要同时支持两个版本
   - 需求：业务系统可以指定使用哪个版本的指标
   - 价值：支持灰度发布，降低算法变更风险

2. **A/B测试场景**
   - 场景：推荐系统需要对比v1和v2版本的用户兴趣标签效果
   - 需求：能够同时查询多个版本的指标值
   - 价值：支持科学的A/B测试，优化算法效果

3. **数据修复场景**
   - 场景：发现v1版本的指标计算有bug，需要回滚到v0版本
   - 需求：能够快速切换到历史版本
   - 价值：快速恢复业务，减少损失

4. **合规审计场景**
   - 场景：监管要求保留历史版本的指标定义和计算结果
   - 需求：能够查询任意历史版本的指标值
   - 价值：满足合规要求，支持审计追溯

### 技术价值
- **版本隔离**：不同版本的指标互不影响，保证系统稳定性
- **灰度发布**：支持逐步切换版本，降低发布风险
- **回滚能力**：快速回滚到历史版本，提升系统可靠性


## 2. 功能设计

### 2.1 数据库设计

**扩展metric表**:
```sql
ALTER TABLE `metric` 
ADD COLUMN `main_version` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'metric main version',
ADD COLUMN `op_log_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '达成当前数值的操作日志ID';

-- 修改唯一索引：code + version 唯一
ALTER TABLE `metric` DROP INDEX `uni_metric_code`;
ALTER TABLE `metric` ADD UNIQUE INDEX `uni_metric_code_version` (`code`, `version`);
```

**新增metric_version表（可选，用于版本历史管理）**:
```sql
CREATE TABLE `metric_version` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `mid`         BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'refer to metric_meta.id',
    `version`     INT UNSIGNED NOT NULL DEFAULT 1 COMMENT 'version number',
    `calc_logic`  TEXT COMMENT 'calculation logic',
    `change_log`  TEXT COMMENT 'change log',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Update time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_metric_version` (`mid`, `version`),
) COMMENT 'metric version';
```

**新增op_log表（可选，用于版本历史管理）**:
```sql
CREATE TABLE `op_log` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `event`.      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=create_metric, 1=create_metric_verion, 2=set_main, 3=rollback',
    `src`         TEXT COMMENT 'snapshot of source before operation',
    `mod`         TEXT COMMENT 'modification details',
    `operator`    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'operator user id',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    INDEX KEY `uni_event` (`event`),
) COMMENT 'molog';
```

### 2.2 API设计

**1. 查询接口扩展**

**接口**: `POST /api/v1/m_snap`

**请求参数扩展**:
```json
{
  "ec": "user",
  "eid": 12345678,
  "metrics": [
    {
      "code": "risk_score",
      "v": 2,
      "dims": {
        "city": "Beijing"
      }
    },
    {
      "code": "interest_tags",
      "v": 1
    }
  ]
}
```

**参数说明**:
- `metrics.v`: 指定版本号（可选）
   - 不传：使用主版本


**响应示例**:
```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "ec": "user",
    "eid": 12345678,
    "values": {
      "risk_score": 0.85,
      "interest_tags": {"sports": 0.8, "tech": 0.6}
    },
    "_v": {
      "risk_score": 2,
      "interest_tags": 1
    }
  }
}
```

**2. 版本管理接口**

**2.1 创建新版本**
```
POST /api/v1/m/{metric_code}/versions
```

**请求体**:
```json
{
  "calc_logic": "risk_score = 0.3 * overdue_count + 0.4 * debt_ratio - 0.3 * income_stability",
  "change_log": "优化风险评分算法，增加收入稳定性权重"
}
```

**参数说明**:
- 新建的版本，不允许是主版本

**响应**:
```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "metric_code": "risk_score",
    "version": 2,
    "is_main": false,
    "release_ts": 1701234567
  }
}
```

**参数说明**:
- `v`: 版本号
- `is_main`: 是否主版本

**2.2 查询版本列表**
```
GET /api/v1/m/{metric_code}/versions
```

**响应**:

```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "metric_code": "risk_score",
    "versions": [
      {
        "version": 2,
        "is_active": true,
        "is_main": true,
        "ut": 1701234567,
        "change_log": "优化风险评分算法"
      },
      {
        "version": 1,
        "is_active": true,
        "is_main": false,
        "ut": 1700000000,
        "change_log": "初始版本"
      }
    ]
  }
}
```

**参数说明**:
- `ut`: 更新时间（UNIX时间戳，毫秒）
- `change_log`: 版本变更说明，以供后续审计

**需要注意**:
- 维护多版本的指标，需要一个“用户-指标快照”的注册机制，维护有一个依赖注册表和一个依赖注册详情表：
  - 用户使用任何一个特定版本的指标快照，都要留有记录
  - 当想要deactive（`is_active=false`）一个版本的指标快照时，需要检查是否还有用户在使用这个版本
    - 如果有用户在使用，则不允许deactive该版本
  - 当用户查询一个指标的快照时，首先检查该用户是否有指定版本的使用权限
    - 如果用户没有指定版本，则返回主版本的快照，不需要进一步做版本权限的检查
    - 如果用户指定版本，并且没有权限，则返回错误提示

**2.3 更新特定版本的指标（功能停留在service层，不暴露api，URL预留）**
```
PUT /api/v1/m/{metric_code}/versions/{version}
```

**请求体**:
```json
{
  "is_active": true,
  "is_main": true,
}
```

**2.4 开启版本**
```
POST /api/v1/m/{metric_code}/versions/{version}/active
```

调用2.3的接口的service层

**请求体**:
```json
{
  "is_active": true
}
```

**2.5 设为主版本**
```
POST /api/v1/m/{metric_code}/versions/{version}/main
```

调用2.3的接口的service层

**请求体**:
```json
{
  "is_main": true
}
```

**需要注意**:
- 当一个指定版本的指标快照被设置为主版本时，系统需要自动将其他版本的is_main设置为false，确保同一时间只有一个主版本。
- 操作日志表（`op_log`）插入记录，其中`event=set_main`，`src`存放metric记录的当前值（json编码）；
- 指标表（`metric`）更新记录，`main_version`字段存放新版本的值，`op_log_id`字段存放新插入的操作日志ID。

**2.6 关闭版本**
```
POST /api/v1/m/{metric_code}/versions/{version}/deactive
```

调用2.3的接口的service层

**请求体**:
```json
{
  "is_active": false
}
```
**需要注意**:
- 当一个指定版本的指标快照被设置为deactive（`is_active=false`）时，该指标快照不允许有用户在使用。
- 当一个指定版本的指标快照被设置为deactive时，该指标快照不能是主版本。

**2.7 版本回滚到上一个版本**
```
POST /api/v1/m/{metric_code}/rollback
```

根据指标表`metric`的`op_log_id`，查询操作日志表`op_log`；
从记录中取查到记录的`src`字段，解析得到指标在操作前的快照；
通过快照中的字段`op_log_id`，恢复到指标表`metric`中该记录的`op_log_id`值；
通过旧记录中的字段`metric_version`,更新指标表`metric_version`中的记录。

### 2.3 存储设计

**Redis Key设计**:
```
// 当前设计（无版本）
{tenant_id}:mx:{metric_code}:{dim_suffix}

// 扩展设计（支持版本）
{tenant_id}:mx:{metric_code}:{dim_suffix}:v{version}
```

**ClickHouse表设计**:
```sql
-- 扩展metrics_detail表，增加version字段
ALTER TABLE metrics_detail 
ADD COLUMN `version` UInt32 DEFAULT 1 COMMENT 'metric version';

-- 修改ORDER BY，包含version
ALTER TABLE metrics_detail 
MODIFY ORDER BY (`tid`, `date`, `code`, `version`, cityHash64(toString(uid)));
```

### 2.4 版本切换流程

**1. 创建新版本**
```
1. 管理员在后台创建新版本（v2）
2. 系统自动生成新的Flink计算任务
3. 新版本开始计算，但is_main=false
4. 数据同时写入Redis和ClickHouse，Key包含版本号
```

**2. 灰度发布**
```
1. 设置v2为默认版本（is_main=true）
2. 业务系统逐步切换（通过API指定version=2）
3. 监控v2版本的数据质量和性能
4. 如果发现问题，快速回滚到v1
```

**3. 全量切换**
```
1. 确认v2版本稳定
2. 所有业务系统切换到v2
3. 下线v1版本（is_active=false）
4. 清理v1版本的历史数据（可选，根据合规要求）
```

### 2.5 影响面评估

**接口**: `GET /api/v1/m/{metric_code}/versions/{version}/impact`

**响应**:
```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "metric_code": "risk_score",
    "version": 2,
    "api_calls": {
      "total_qps": 500,
      "calling_services": [
        {"service": "risk_control", "qps": 300},
        {"service": "credit_approval", "qps": 200}
      ]
    },
    "downstream_metrics": [
      {"code": "credit_limit", "dependency": "derived"}
    ],
    "risk_level": "high"
  }
}
```

**需要注意**:
- 后端查询负责“用户-指标快照”的注册机制的数据表，结合指标系统的运维指标（定时更新）获得API统计数据
- 关于指标系统的运维指标的产生机制，可以由后端查询API调用日志，通过统计出当前版本的指标快照被哪些服务调用，以及调用频率，保存成运维指标。


## 3. 实施优先级

- 基础版（支持版本创建和切换）：中优先级，Alpha实现
- 高级版（支持灰度发布、A/B测试）：低优先级，Beta阶段实现


## 4. 技术实现要点

## 4.1 数据库迁移
- 需要为现有指标设置默认状态（ONLINE）和默认版本（1）
- 需要迁移历史数据，确保兼容性

## 4.2 Flink任务改造
- 支持读取指标版本和状态配置
- 根据版本号生成不同的Redis Key

## 4.3 API改造
- GMS接口增加版本检查
- 增加版本管理接口

## 4.4 监控告警
- 监控版本切换
