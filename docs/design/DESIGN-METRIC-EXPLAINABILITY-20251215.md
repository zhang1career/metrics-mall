# 指标计算结果可解释性设计

**文档版本**: v1.0  
**创建日期**: 2025-01-15  
**作者**: Rongjin Zhang  
**文档类型**: 产品设计文档

---

## 1. 功能价值

### 1.1 业务场景

#### 1.1.1 信贷风控场景
**场景描述**：
- 用户申请贷款，系统返回风险评分 0.85（高风险）
- 用户质疑："为什么我的评分这么低？"
- 业务人员需要向用户解释评分依据

**核心需求**：
- 能够解释风险评分的计算过程
- 展示各个影响因子及其贡献度
- 提供改进建议（如何提升评分）

**业务价值**：
- **合规要求**：满足监管对算法可解释性的要求（如欧盟GDPR、中国《个人信息保护法》）
- **用户信任**：透明的评分机制提升用户信任度
- **业务优化**：帮助用户理解如何改善评分，提升业务转化率

#### 1.1.2 推荐系统场景
**场景描述**：
- 推荐系统给用户推荐了商品A，用户想知道"为什么推荐这个？"
- 需要解释推荐理由：用户历史偏好、协同过滤、热门商品等

**核心需求**：
- 解释推荐商品的得分构成
- 展示各个推荐因子的权重
- 支持推荐理由的可视化展示

**业务价值**：
- **用户体验**：提升推荐系统的透明度和用户接受度
- **算法优化**：通过解释分析，优化推荐算法效果
- **A/B测试**：对比不同推荐策略的解释效果

#### 1.1.3 交易风控场景
**场景描述**：
- 交易被风控系统拦截，需要向用户解释原因
- 需要展示触发风控规则的具体因子

**核心需求**：
- 解释风控决策的依据
- 展示触发的风控规则
- 提供申诉和改善建议

**业务价值**：
- **用户满意度**：清晰的解释减少用户投诉
- **运营效率**：减少人工客服解释成本
- **风控优化**：通过解释分析，优化风控规则

#### 1.1.4 数据分析场景
**场景描述**：
- 数据分析师需要理解指标异常的原因
- 需要追溯指标计算过程，定位问题

**核心需求**：
- 展示指标的计算公式
- 展示各个输入因子的值
- 支持指标溯源和影响分析

**业务价值**：
- **问题定位**：快速定位指标异常的根本原因
- **数据质量**：通过解释发现数据质量问题
- **业务理解**：帮助分析师理解业务逻辑

### 1.2 技术价值

- **算法透明度**：提升算法决策的透明度，满足合规要求
- **问题诊断**：通过解释快速定位问题，提升运维效率
- **算法优化**：通过解释分析，发现算法改进点
- **用户信任**：透明的机制提升用户对系统的信任

---

## 2. 功能设计

### 2.1 可解释性类型

#### 2.1.1 基于公式的解释（Formula-based Explanation）
**适用场景**：指标有明确的数学公式

**示例**：
```
risk_score = 0.3 * overdue_count + 0.4 * debt_ratio - 0.3 * income_stability
```

**解释内容**：
- 公式本身
- 各个因子的值
- 各个因子的权重
- 各个因子的贡献度（对最终结果的贡献）

#### 2.1.2 基于规则的解释（Rule-based Explanation）
**适用场景**：指标基于规则引擎计算

**示例**：
```
IF overdue_count > 3 THEN risk_level = "high"
IF debt_ratio > 0.8 THEN risk_level = "high"
IF income_stability < 0.5 THEN risk_level = "medium"
```

**解释内容**：
- 触发的规则列表
- 规则的优先级
- 规则的匹配结果

#### 2.1.3 基于特征重要性的解释（Feature Importance Explanation）
**适用场景**：指标基于机器学习模型计算

**示例**：
```
risk_score = ML_Model(user_features)
```

**解释内容**：
- 特征重要性排序
- 各个特征的贡献度
- 特征值的分布情况

#### 2.1.4 基于血缘的解释（Lineage-based Explanation）
**适用场景**：派生指标（依赖其他指标）

**示例**：
```
available_balance = balance - frozen_amount
```

**解释内容**：
- 依赖的源指标
- 源指标的值
- 计算逻辑
- 数据来源

### 2.2 数据模型设计

#### 2.2.1 扩展metric_version表

**新增字段**：
```sql
ALTER TABLE `metric_version` 
ADD COLUMN `is_explainable` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'whether this metric supports explainability, 0=no, 1=yes',
ADD COLUMN `explain_type` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'explainability type, 0=formula, 1=rule, 2=feature_importance, 3=lineage',
ADD COLUMN `explain_config` TEXT COMMENT 'explainability configuration, JSON format';
```

**explain_config JSON结构**：
```json
{
  "formula": "risk_score = 0.3 * overdue_count + 0.4 * debt_ratio - 0.3 * income_stability",
  "factors": [
    {
      "name": "overdue_count",
      "code": "overdue_count",
      "weight": 0.3,
      "impact": "negative",
      "description": "逾期次数"
    },
    {
      "name": "debt_ratio",
      "code": "debt_ratio",
      "weight": 0.4,
      "impact": "negative",
      "description": "负债率"
    },
    {
      "name": "income_stability",
      "code": "income_stability",
      "weight": 0.3,
      "impact": "positive",
      "description": "收入稳定性"
    }
  ],
  "rules": [
    {
      "name": "high_overdue_risk",
      "condition": "overdue_count > 3",
      "action": "risk_score += 0.2",
      "priority": 1
    }
  ]
}
```

#### 2.2.2 新增metric_explanation表

**表结构**：
```sql
CREATE TABLE `metric_explanation` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `metric_id`   BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'refer to metric.id',
    `entity_code` VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'entity code',
    `entity_id`   BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'entity id',
    `metric_value` DECIMAL(20, 4) COMMENT 'metric value',
    `explanation` TEXT COMMENT 'explanation JSON',
    `snapshot_ts` BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'snapshot timestamp, milliseconds',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    INDEX `idx_metric_entity` (`metric_id`, `entity_code`, `entity_id`),
    INDEX `idx_snapshot_ts` (`snapshot_ts`)
) COMMENT 'metric explanation cache';
```

**explanation JSON结构**：
```json
{
  "metric_code": "risk_score",
  "metric_value": 0.85,
  "explain_type": "formula",
  "formula": "risk_score = 0.3 * overdue_count + 0.4 * debt_ratio - 0.3 * income_stability",
  "factors": [
    {
      "name": "overdue_count",
      "code": "overdue_count",
      "value": 3,
      "weight": 0.3,
      "impact": "negative",
      "contribution": 0.09,
      "description": "逾期次数",
      "source": "internal_db",
      "timestamp": 1701234567890
    },
    {
      "name": "debt_ratio",
      "code": "debt_ratio",
      "value": 0.8,
      "weight": 0.4,
      "impact": "negative",
      "contribution": 0.32,
      "description": "负债率",
      "source": "credit_bureau",
      "timestamp": 1701234500000
    },
    {
      "name": "income_stability",
      "code": "income_stability",
      "value": 0.9,
      "weight": 0.3,
      "impact": "positive",
      "contribution": -0.27,
      "description": "收入稳定性",
      "source": "internal_db",
      "timestamp": 1701234567000
    }
  ],
  "calculation": {
    "step1": "0.3 * 3 = 0.09",
    "step2": "0.4 * 0.8 = 0.32",
    "step3": "-0.3 * 0.9 = -0.27",
    "step4": "0.09 + 0.32 - 0.27 = 0.14",
    "final": "risk_score = 0.14"
  },
  "summary": {
    "positive_factors": ["income_stability"],
    "negative_factors": ["overdue_count", "debt_ratio"],
    "top_contributor": "debt_ratio",
    "improvement_suggestions": [
      "降低负债率可显著提升风险评分",
      "减少逾期次数可提升风险评分"
    ]
  }
}
```

#### 2.2.3 扩展metric_lineage表

**新增字段**：
```sql
ALTER TABLE `metric_lineage` 
ADD COLUMN `factor_weight` DECIMAL(10, 4) NOT NULL DEFAULT 0 COMMENT 'factor weight in formula',
ADD COLUMN `factor_impact` VARCHAR(32) NOT NULL DEFAULT '' COMMENT 'factor impact, positive/negative/neutral',
ADD COLUMN `factor_description` VARCHAR(256) NOT NULL DEFAULT '' COMMENT 'factor description';
```

### 2.3 API设计

#### 2.3.1 查询接口扩展

**接口**: `POST /api/v1/m/explain`

**请求参数扩展**：
```json
{
  "metrics": [
    {
      "metric_code": "risk_score",
      "version": 1002
    }
  ],
  "require_explanation": true,
  "explain_level": 1
}
```

**参数说明**：
- `require_explanation`: 是否返回解释信息（可选，默认false）
  - `false`: 只返回指标值（性能最优）
  - `true`: 返回指标值和解释信息
- `explain_level`: 解释程度（可选，默认0），枚举值：0=simple，1=full
  - `simple`: 只返回关键因子和公式
  - `full`: 返回详细的解释信息，包括计算步骤、改进建议等

**响应示例（require_explanation=true）**：
```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "ec": "user",
    "eid": 12345678,
    "values": {
      "risk_score": 0.85
    },
    "explanations": {
      "risk_score": {
        "explain_type": "formula",
        "formula": "risk_score = 0.3 * overdue_count + 0.4 * debt_ratio - 0.3 * income_stability",
        "factors": [
          {
            "name": "overdue_count",
            "code": "overdue_count",
            "value": 3,
            "weight": 0.3,
            "impact": "negative",
            "contribution": 0.09,
            "description": "逾期次数"
          },
          {
            "name": "debt_ratio",
            "code": "debt_ratio",
            "value": 0.8,
            "weight": 0.4,
            "impact": "negative",
            "contribution": 0.32,
            "description": "负债率"
          },
          {
            "name": "income_stability",
            "code": "income_stability",
            "value": 0.9,
            "weight": 0.3,
            "impact": "positive",
            "contribution": -0.27,
            "description": "收入稳定性"
          }
        ],
        "summary": {
          "positive_factors": ["income_stability"],
          "negative_factors": ["overdue_count", "debt_ratio"],
          "top_contributor": "debt_ratio",
          "improvement_suggestions": [
            "降低负债率可显著提升风险评分",
            "减少逾期次数可提升风险评分"
          ]
        }
      }
    }
  }
}
```

#### 2.3.2 独立解释查询接口

**接口**: 
```
GET /api/v1/m/{metric_code}/versions/{version}/explain
```

**响应示例**：
```json
{
  "code": 0,
  "errmsg": "",
  "data": {
    "metric_code": "risk_score",
    "metric_value": 0.85,
    "entity_code": "user",
    "entity_id": 12345678,
    "snapshot_ts": 1701234567890,
    "explain_type": "formula",
    "formula": "risk_score = 0.3 * overdue_count + 0.4 * debt_ratio - 0.3 * income_stability",
    "factors": [
      {
        "name": "overdue_count",
        "code": "overdue_count",
        "value": 3,
        "weight": 0.3,
        "impact": "negative",
        "contribution": 0.09,
        "description": "逾期次数",
        "source": "internal_db",
        "timestamp": 1701234567890
      },
      {
        "name": "debt_ratio",
        "code": "debt_ratio",
        "value": 0.8,
        "weight": 0.4,
        "impact": "negative",
        "contribution": 0.32,
        "description": "负债率",
        "source": "credit_bureau",
        "timestamp": 1701234500000
      },
      {
        "name": "income_stability",
        "code": "income_stability",
        "value": 0.9,
        "weight": 0.3,
        "impact": "positive",
        "contribution": -0.27,
        "description": "收入稳定性",
        "source": "internal_db",
        "timestamp": 1701234567000
      }
    ],
    "calculation": {
      "step1": "0.3 * 3 = 0.09",
      "step2": "0.4 * 0.8 = 0.32",
      "step3": "-0.3 * 0.9 = -0.27",
      "step4": "0.09 + 0.32 - 0.27 = 0.14",
      "final": "risk_score = 0.14"
    },
    "summary": {
      "positive_factors": ["income_stability"],
      "negative_factors": ["overdue_count", "debt_ratio"],
      "top_contributor": "debt_ratio",
      "improvement_suggestions": [
        "降低负债率可显著提升风险评分",
        "减少逾期次数可提升风险评分"
      ]
    }
  }
}
```

### 2.4 实现方案

#### 2.4.1 公式型指标解释

**实现逻辑**：
```
1. 查询metric表，获取explain_config（包含公式和因子列表）
2. 对于每个因子，查询其当前值（通过GMS接口或直接查Redis）
3. 根据公式计算各个因子的贡献度
4. 生成解释JSON
5. 可选：缓存解释结果到metric_explanation表
```

**代码示例（伪代码）**：
```java
public Explanation explainFormulaMetric(String metricCode, String entityCode, Long entityId) {
    // 1. 查询指标配置
    Metric metric = metricDao.getByCode(metricCode);
    ExplainConfig config = JSON.parseObject(metric.getExplainConfig(), ExplainConfig.class);
    
    // 2. 查询各个因子的值
    List<Factor> factors = new ArrayList<>();
    for (FactorConfig factorConfig : config.getFactors()) {
        // 查询因子值
        Double factorValue = getMetricValue(factorConfig.getCode(), entityCode, entityId);
        
        // 计算贡献度
        Double contribution = factorValue * factorConfig.getWeight();
        if ("negative".equals(factorConfig.getImpact())) {
            contribution = -contribution;
        }
        
        Factor factor = Factor.builder()
            .code(factorConfig.getCode())
            .value(factorValue)
            .weight(factorConfig.getWeight())
            .impact(factorConfig.getImpact())
            .contribution(contribution)
            .build();
        factors.add(factor);
    }
    
    // 3. 生成计算步骤
    CalculationSteps steps = generateCalculationSteps(config.getFormula(), factors);
    
    // 4. 生成总结
    Summary summary = generateSummary(factors);
    
    // 5. 构建解释
    return Explanation.builder()
        .explainType("formula")
        .formula(config.getFormula())
        .factors(factors)
        .calculation(steps)
        .summary(summary)
        .build();
}
```

#### 2.4.2 血缘型指标解释

**实现逻辑**：
```
1. 查询metric_lineage表，获取依赖关系
2. 递归查询所有依赖指标的当前值
3. 根据计算逻辑生成解释
4. 展示完整的依赖链
```

**代码示例（伪代码）**：
```java
public Explanation explainLineageMetric(String metricCode, String entityCode, Long entityId) {
    // 1. 查询依赖关系
    List<MetricLineage> lineages = lineageDao.getByDestMetricCode(metricCode);
    
    // 2. 查询依赖指标的值
    List<Factor> factors = new ArrayList<>();
    for (MetricLineage lineage : lineages) {
        Metric srcMetric = metricDao.getById(lineage.getSrcId());
        Double srcValue = getMetricValue(srcMetric.getCode(), entityCode, entityId);
        
        Factor factor = Factor.builder()
            .code(srcMetric.getCode())
            .value(srcValue)
            .weight(lineage.getFactorWeight())
            .impact(lineage.getFactorImpact())
            .dependency(true)
            .build();
        factors.add(factor);
    }
    
    // 3. 获取计算逻辑
    String calcLogic = lineages.get(0).getTransLogic();
    
    // 4. 生成解释
    return Explanation.builder()
        .explainType("lineage")
        .formula(calcLogic)
        .factors(factors)
        .build();
}
```

#### 2.4.3 规则型指标解释

**实现逻辑**：
```
1. 查询metric表的explain_config，获取规则列表
2. 对每个规则进行匹配
3. 记录触发的规则和匹配结果
4. 生成解释
```

**代码示例（伪代码）**：
```java
public Explanation explainRuleMetric(String metricCode, String entityCode, Long entityId) {
    // 1. 查询规则配置
    Metric metric = metricDao.getByCode(metricCode);
    ExplainConfig config = JSON.parseObject(metric.getExplainConfig(), ExplainConfig.class);
    
    // 2. 匹配规则
    List<RuleMatch> matchedRules = new ArrayList<>();
    for (RuleConfig rule : config.getRules()) {
        boolean matched = evaluateRule(rule.getCondition(), entityCode, entityId);
        if (matched) {
            matchedRules.add(RuleMatch.builder()
                .ruleName(rule.getName())
                .condition(rule.getCondition())
                .action(rule.getAction())
                .priority(rule.getPriority())
                .build());
        }
    }
    
    // 3. 生成解释
    return Explanation.builder()
        .explainType("rule")
        .matchedRules(matchedRules)
        .build();
}
```

#### 2.4.4 性能优化

**1. 缓存策略**
- 解释结果缓存到`metric_explanation`表（TTL 5分钟）
- Redis缓存热点解释（TTL 1分钟）
- 缓存Key: `explain:{metric_code}:{entity_code}:{entity_id}`

**2. 异步计算**
- 对于复杂解释（如深度血缘查询），支持异步计算
- 返回任务ID，客户端轮询结果

**3. 批量查询优化**
- 批量查询因子值时，使用GMS批量接口
- 减少网络往返次数

**4. 懒加载**
- 默认只返回simple解释
- 需要full解释时再计算详细内容

### 2.5 改进建议生成

#### 2.5.1 基于因子的建议

**逻辑**：
```
1. 识别负向因子（impact=negative）
2. 计算各因子的贡献度
3. 按贡献度排序，优先改善贡献度大的因子
4. 生成具体建议
```

**示例**：
```json
{
  "improvement_suggestions": [
    {
      "factor": "debt_ratio",
      "current_value": 0.8,
      "suggested_value": 0.6,
      "impact": "降低负债率从0.8到0.6，可提升风险评分约0.08",
      "priority": "high"
    },
    {
      "factor": "overdue_count",
      "current_value": 3,
      "suggested_value": 0,
      "impact": "减少逾期次数从3到0，可提升风险评分约0.09",
      "priority": "high"
    }
  ]
}
```

#### 2.5.2 基于阈值的建议

**逻辑**：
```
1. 定义各因子的健康阈值
2. 对比当前值和阈值
3. 生成改善建议
```

**配置示例**：
```json
{
  "thresholds": {
    "debt_ratio": {
      "healthy": 0.5,
      "warning": 0.7,
      "critical": 0.8
    },
    "overdue_count": {
      "healthy": 0,
      "warning": 1,
      "critical": 3
    }
  }
}
```

### 2.6 错误处理

| 错误码 | HTTP状态码 | 错误信息 | 触发场景 | 处理建议 |
|--------|-----------|---------|---------|---------|
| 40008 | 400 | Metric Not is_explainable | 指标不支持解释 | 检查指标配置 |
| 40009 | 400 | Explanation Config Invalid | 解释配置无效 | 检查指标配置 |
| 40010 | 400 | Factor Value Not Found | 因子值查询失败 | 检查因子指标是否存在 |
| 50003 | 500 | Explanation Calculation Timeout | 解释计算超时 | 重试或使用异步接口 |

---

## 三、实施优先级

### 高优先级（MVP后立即实现）
1. **公式型指标解释（基础版）** - 支持简单的线性公式解释
2. **血缘型指标解释（基础版）** - 支持单层依赖的解释

### 中优先级（Alpha阶段）
3. **规则型指标解释** - 支持规则引擎的解释
4. **改进建议生成** - 基于因子的建议生成
5. **解释结果缓存** - 提升查询性能

### 低优先级（Beta阶段）
6. **特征重要性解释** - 支持机器学习模型的解释
7. **异步解释计算** - 支持复杂解释的异步计算
8. **解释可视化** - 提供解释的可视化展示

---

## 四、技术实现要点

### 4.1 数据库迁移
- 为现有指标设置`is_explainable`和`explain_type`字段
- 为支持解释的指标配置`explain_config`
- 创建`metric_explanation`表

### 4.2 API改造
- GMS接口增加`require_explanation`参数
- 新增独立解释查询接口
- 新增批量解释查询接口

### 4.3 计算引擎改造
- Flink任务支持记录计算过程（可选，用于复杂解释）
- 支持解释结果的实时计算

### 4.4 性能优化
- 解释结果缓存（Redis + MySQL）
- 批量查询优化
- 懒加载策略
d