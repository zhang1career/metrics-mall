# Metrics Mall 集成测试计划

**文档版本**: v1.0  
**创建日期**: 2025-01-16  
**测试范围**: 完整的数据准备、快照写入和查询流程

---

## 1. 测试环境

### 1.1 数据库
- **数据库类型**: MySQL
- **测试数据库**: 使用独立的测试数据库，每个测试用例执行前清理数据

### 1.2 测试框架
- **测试框架**: Spock Framework (Groovy)
- **HTTP客户端**: Spring MockMvc 或 RestTemplate
- **数据库**: 使用 `@SpringBootTest` 和 `@Transactional` 或手动数据清理

### 1.3 前置条件
- 所有测试必须通过 Controller 层调用
- 需要有效的 API Key（测试用）
- 数据库连接配置正确

---

## 2. 测试数据准备阶段

### 2.1 EntityMeta 数据准备

#### 2.1.1 测试用例：新增 EntityMeta - 成功
- **接口**: `POST /api/v1/entity_metas` (假设存在，如不存在需创建)
- **请求数据**:
  ```json
  {
    "code": "customer",
    "name": "顾客"
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中存在该记录

#### 2.1.2 测试用例：新增 EntityMeta - 失败（code重复）
- **接口**: `POST /api/v1/entity_metas`
- **请求数据**:
  ```json
  {
    "code": "customer",
    "name": "顾客2"
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "code重复" 或类似提示

**注意**: 如果 EntityMeta Controller 不存在，需要先创建 `EntityController`。

---

### 2.2 MetricMeta 数据准备

#### 2.2.1 测试用例：新增 MetricMeta - 成功
- **接口**: `POST /api/v1/metrics`
- **请求数据**:
  ```json
  {
    "code": "consume_amount",
    "name": "消费金额",
    "valueType": 2,
    "precision": 2
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中存在该记录

#### 2.2.2 测试用例：新增 MetricMeta - 失败（code重复）
- **接口**: `POST /api/v1/metrics`
- **请求数据**:
  ```json
  {
    "code": "consume_amount",
    "name": "消费金额2",
    "valueType": 2,
    "precision": 2
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "code重复" 或类似提示

---

### 2.3 EntityMetricRel 关联关系准备

#### 2.3.1 测试用例：新增 EntityMetricRel - 成功
- **接口**: `POST /api/v1/entity_metric_rels` (假设存在，如不存在需创建)
- **请求数据**:
  ```json
  {
    "entityCode": "customer",
    "metricCode": "consume_amount",
    "alias": "amount",
    "dataUri": "xxx"
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中存在该关联关系

#### 2.3.2 测试用例：新增 EntityMetricRel - 失败（重复关联）
- **接口**: `POST /api/v1/entity_metric_rels`
- **请求数据**:
  ```json
  {
    "entityCode": "customer",
    "metricCode": "consume_amount",
    "alias": "amount2",
    "dataUri": "yyy"
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "1条EntityMeta数据和1条MetricMeta数据只能建立1条关联关系" 或类似提示

**注意**: 如果 EntityMetricRel Controller 不存在，需要先创建相关 Controller。

---

### 2.4 MetricVersion 数据准备

#### 2.4.1 测试用例：新增 MetricVersion - 成功（version=1）
- **接口**: `POST /api/v1/metric_versions`
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 1,
    "isMain": 0,
    "lifeStatus": 0,
    "calcLogic": "xxx"
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中存在该记录（记作 version_1）

**注意**: `MetricVersionQO` 可能需要添加 `calcLogic` 字段，或通过其他方式传递。

#### 2.4.2 测试用例：新增 MetricVersion - 失败（version重复）
- **接口**: `POST /api/v1/metric_versions`
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 1,
    "isMain": 0,
    "lifeStatus": 0,
    "calcLogic": "yyy"
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "version重复" 或类似提示

#### 2.4.3 测试用例：新增 MetricVersion - 失败（is_main不能为非0值）
- **接口**: `POST /api/v1/metric_versions`
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 2,
    "isMain": 1,
    "lifeStatus": 0,
    "calcLogic": "xxx"
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "新建数据的is_main不能为非0值" 或类似提示

#### 2.4.4 测试用例：新增 MetricVersion - 失败（life_status不能为非0值）
- **接口**: `POST /api/v1/metric_versions`
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 2,
    "isMain": 0,
    "lifeStatus": 1,
    "calcLogic": "xxx"
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "新建数据的life_status不能为非0值" 或类似提示

#### 2.4.5 测试用例：新增 MetricVersion - 失败（calc_logic不能为空）
- **接口**: `POST /api/v1/metric_versions`
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 2,
    "isMain": 0,
    "lifeStatus": 0,
    "calcLogic": ""
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "calc_logic不能为空" 或类似提示

#### 2.4.6 测试用例：新增 MetricVersion - 成功（version=2）
- **接口**: `POST /api/v1/metric_versions`
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 2,
    "isMain": 0,
    "lifeStatus": 0,
    "calcLogic": "xxx"
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中存在该记录（记作 version_2）

---

### 2.5 Dimension 数据准备

#### 2.5.1 测试用例：新增 Dimension - 成功（location）
- **接口**: `POST /api/v1/dims`
- **请求数据**:
  ```json
  {
    "code": "location",
    "name": "地点"
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中存在该记录（记作 dim_location）

#### 2.5.2 测试用例：新增 Dimension - 失败（code重复）
- **接口**: `POST /api/v1/dims`
- **请求数据**:
  ```json
  {
    "code": "location",
    "name": "地点2"
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "code重复" 或类似提示

#### 2.5.3 测试用例：新增 Dimension - 成功（venues）
- **接口**: `POST /api/v1/dims`
- **请求数据**:
  ```json
  {
    "code": "venues",
    "name": "场所"
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中存在该记录（记作 dim_venues）

#### 2.5.4 测试用例：新增 Dimension - 成功（category）
- **接口**: `POST /api/v1/dims`
- **请求数据**:
  ```json
  {
    "code": "category",
    "name": "种类"
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中存在该记录（记作 dim_category）

---

### 2.6 MetricDimensionRel 关联关系准备

#### 2.6.1 测试用例：新增 MetricDimensionRel - 成功（location）
- **接口**: `POST /api/v1/metric_dim_rels` (假设存在，如不存在需创建)
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "dimensionCode": "location",
    "isHot": 1,
    "validation": "xxx"
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中存在该关联关系

#### 2.6.2 测试用例：新增 MetricDimensionRel - 成功（venues）
- **接口**: `POST /api/v1/metric_dim_rels`
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "dimensionCode": "venues",
    "isHot": 1,
    "validation": "xxx"
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中存在该关联关系

#### 2.6.3 测试用例：新增 MetricDimensionRel - 成功（category）
- **接口**: `POST /api/v1/metric_dim_rels`
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "dimensionCode": "category",
    "isHot": 1,
    "validation": "xxx"
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中存在该关联关系

**注意**: 如果 MetricDimensionRel Controller 不存在，需要先创建相关 Controller。

---

### 2.7 MetricVersion 状态更新

#### 2.7.1 测试用例：更新 version_1 的 life_status 为 TEST
- **接口**: `PUT /api/v1/metric_versions`
- **路径参数**: `id` = version_1 的 ID
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 1,
    "lifeStatus": 2
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中 life_status = 2 (TEST)

**注意**: 需要确认 life_status 的枚举值，TEST 可能对应 2（根据 OpEventEnum）。

#### 2.7.2 测试用例：更新 version_1 的 life_status 为 GRAY
- **接口**: `PUT /api/v1/metric_versions`
- **路径参数**: `id` = version_1 的 ID
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 1,
    "lifeStatus": 3
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中 life_status = 3 (GRAY)

#### 2.7.3 测试用例：更新 version_1 的 life_status 为 ONLINE
- **接口**: `PUT /api/v1/metric_versions`
- **路径参数**: `id` = version_1 的 ID
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 1,
    "lifeStatus": 4
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中 life_status = 4 (ONLINE)

#### 2.7.4 测试用例：更新 version_2 的 life_status 为 TEST
- **接口**: `PUT /api/v1/metric_versions`
- **路径参数**: `id` = version_2 的 ID
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 2,
    "lifeStatus": 2
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中 life_status = 2 (TEST)

#### 2.7.5 测试用例：更新 version_2 的 life_status 为 GRAY
- **接口**: `PUT /api/v1/metric_versions`
- **路径参数**: `id` = version_2 的 ID
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 2,
    "lifeStatus": 3
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中 life_status = 3 (GRAY)

#### 2.7.6 测试用例：更新 version_2 的 life_status 为 ONLINE
- **接口**: `PUT /api/v1/metric_versions`
- **路径参数**: `id` = version_2 的 ID
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 2,
    "lifeStatus": 4
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中 life_status = 4 (ONLINE)

#### 2.7.7 测试用例：更新 version_2 的 is_main 为 1
- **接口**: `PUT /api/v1/metric_versions`
- **路径参数**: `id` = version_2 的 ID
- **请求数据**:
  ```json
  {
    "metricCode": "consume_amount",
    "version": 2,
    "isMain": 1
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data = true`
  - 数据库中 is_main = 1

---

## 3. 快照写入测试阶段

### 3.1 测试用例：写入快照 - 成功（基础数据）
- **接口**: `POST /api/v1/m_snap/write`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "metrics": [
      {
        "alias": "amount",
        "v": 1,
        "value": 500.01
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data` 为 receiptId (BigInteger)
  - 数据已写入缓存/数据库

### 3.2 测试用例：写入快照 - 失败（不支持的别名）
- **接口**: `POST /api/v1/m_snap/write`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "metrics": [
      {
        "alias": "foo",
        "v": 1,
        "value": 100.0
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "不支持的别名：foo" 或类似提示

### 3.3 测试用例：写入快照 - 失败（不存在的版本）
- **接口**: `POST /api/v1/m_snap/write`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "metrics": [
      {
        "alias": "amount",
        "v": 3,
        "value": 100.0
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "不存在的版本：3" 或类似提示

### 3.4 测试用例：写入快照 - 失败（不存在的维度）
- **接口**: `POST /api/v1/m_snap/write`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "metrics": [
      {
        "alias": "amount",
        "v": 1,
        "dims": {
          "time": "8000"
        },
        "value": 100.0
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "不存在的维度：time" 或类似提示

### 3.5 测试用例：写入快照 - 成功（带单个维度）
- **接口**: `POST /api/v1/m_snap/write`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "metrics": [
      {
        "alias": "amount",
        "v": 1,
        "dims": {
          "location": "beijing"
        },
        "value": 99.99
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data` 为 receiptId
  - 数据已写入

### 3.6 测试用例：写入快照 - 成功（带两个维度）
- **接口**: `POST /api/v1/m_snap/write`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "metrics": [
      {
        "alias": "amount",
        "v": 1,
        "dims": {
          "location": "shanghai",
          "venues": "hotel"
        },
        "value": 400.02
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data` 为 receiptId
  - 数据已写入

### 3.7 测试用例：写入快照 - 失败（不支持的维度组合）
- **接口**: `POST /api/v1/m_snap/write`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "metrics": [
      {
        "alias": "amount",
        "v": 1,
        "dims": {
          "location": "shanghai",
          "venues": "hotel",
          "category": "food"
        },
        "value": 100.0
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200 或 400
  - `code != 0` 或抛出异常
  - 错误信息包含 "不支持的维度组合：location, venues, category" 或类似提示

### 3.8 测试用例：写入快照 - 成功（带时间戳1）
- **接口**: `POST /api/v1/m_snap/write`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "snapshotTs": 1000,
    "metrics": [
      {
        "alias": "amount",
        "dims": {
          "category": "food"
        },
        "value": 111.111
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data` 为 receiptId
  - 数据已写入，时间戳为 1000

### 3.9 测试用例：写入快照 - 成功（带时间戳2）
- **接口**: `POST /api/v1/m_snap/write`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "snapshotTs": 2000,
    "metrics": [
      {
        "alias": "amount",
        "dims": {
          "category": "food"
        },
        "value": 222.222
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data` 为 receiptId
  - 数据已写入，时间戳为 2000

---

## 4. 快照查询测试阶段

### 4.1 测试用例：查询快照 - 成功（指定版本）
- **接口**: `POST /api/v1/m_snap`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "metrics": [
      {
        "alias": "amount",
        "v": 1
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data.values.consume_amount = 500.01`
  - 返回最新的快照值

### 4.2 测试用例：查询快照 - 成功（指定时间戳，返回较新的值）
- **接口**: `POST /api/v1/m_snap`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "snapshotTs": 2500,
    "metrics": [
      {
        "alias": "amount"
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data.values.consume_amount = 222.22` (精度为2，四舍五入)
  - `data._ts.consume_amount = 2000` (返回时间戳字段)

### 4.3 测试用例：查询快照 - 成功（指定时间戳，精确匹配）
- **接口**: `POST /api/v1/m_snap`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "snapshotTs": 2000,
    "metrics": [
      {
        "alias": "amount"
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data.values.consume_amount = 222.22`
  - `data._ts.consume_amount = 2000`

### 4.4 测试用例：查询快照 - 成功（指定时间戳，返回较旧的值）
- **接口**: `POST /api/v1/m_snap`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "snapshotTs": 1500,
    "metrics": [
      {
        "alias": "amount"
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200
  - `code = 0`
  - `data.values.consume_amount = 111.11`
  - `data._ts.consume_amount = 1000`

### 4.5 测试用例：查询快照 - 成功（指定时间戳，查询结果为空）
- **接口**: `POST /api/v1/m_snap`
- **请求头**: `X-API-Key: {test_api_key}`
- **请求数据**:
  ```json
  {
    "ec": "customer",
    "eid": 10000001,
    "snapshotTs": 500,
    "metrics": [
      {
        "alias": "amount"
      }
    ]
  }
  ```
- **预期结果**: 
  - HTTP 200 或 404
  - `code != 0` 或 `data` 为空/null
  - 错误信息可能包含 "metric not found" 或类似提示

---

## 5. 测试执行顺序

### 5.1 数据准备阶段（顺序执行）
1. EntityMeta 数据准备（2.1）
2. MetricMeta 数据准备（2.2）
3. EntityMetricRel 关联关系准备（2.3）
4. MetricVersion 数据准备（2.4）
5. Dimension 数据准备（2.5）
6. MetricDimensionRel 关联关系准备（2.6）
7. MetricVersion 状态更新（2.7）

### 5.2 快照写入测试阶段（顺序执行）
1. 写入基础数据（3.1）
2. 写入失败场景测试（3.2, 3.3, 3.4, 3.7）
3. 写入成功场景测试（3.5, 3.6, 3.8, 3.9）

### 5.3 快照查询测试阶段（顺序执行）
1. 查询基础场景（4.1）
2. 查询时间戳场景（4.2, 4.3, 4.4, 4.5）

---

## 6. 注意事项

### 6.1 缺失的 Controller
根据代码分析，以下 Controller 可能不存在，需要创建：
1. **EntityController**: 用于 EntityMeta 的 CRUD 操作
2. **EntityMetricRelController**: 用于 EntityMetricRel 的创建操作
3. **MetricDimensionRelController**: 用于 MetricDimensionRel 的创建操作

### 6.2 数据清理
每个测试用例执行前需要：
- 清理相关测试数据
- 或使用 `@Transactional` 和 `@Rollback` 自动回滚

### 6.3 API Key
需要准备测试用的 API Key，并在所有需要认证的请求中使用。

---

## 7. 测试数据总结

### 7.1 准备的数据
- **EntityMeta**: customer (id 待定)
- **MetricMeta**: consume_amount (id 待定)
- **EntityMetricRel**: customer <-> consume_amount (alias=amount)
- **MetricVersion**: 
  - version_1 (version=1, id 待定)
  - version_2 (version=2, is_main=1, id 待定)
- **Dimension**: 
  - dim_location (code=location)
  - dim_venues (code=venues)
  - dim_category (code=category)
- **MetricDimensionRel**: 
  - consume_amount <-> location
  - consume_amount <-> venues
  - consume_amount <-> category

### 7.2 写入的快照数据
- entityCode=customer, entityId=10000001, alias=amount, v=1, value=500.01
- entityCode=customer, entityId=10000001, alias=amount, v=1, dims={location: beijing}, value=99.99
- entityCode=customer, entityId=10000001, alias=amount, v=1, dims={location: shanghai, venues: hotel}, value=400.02
- entityCode=customer, entityId=10000001, alias=amount, dims={category: food}, value=111.111, snapshotTs=1000
- entityCode=customer, entityId=10000001, alias=amount, dims={category: food}, value=222.222, snapshotTs=2000

---

## 8. 更新记录

| 版本 | 日期 | 更新内容 | 作者 |
|------|------|----------|------|
| v1.0 | 2025-01-16 | 初始版本，完成集成测试计划 | Metrics Mall Team |

---

**文档维护**：
- 本文档应随测试实现和需求变化定期更新
- 测试用例应基于实际 Controller 接口进行调整
- 建议在实现测试前确认所有 Controller 和字段的可用性

