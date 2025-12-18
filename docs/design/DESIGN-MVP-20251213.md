# 最小产品设计

## 指标管理

### 细化维度的指标
支持对少量的高频核心维度进行预计算，提升查询性能，同时能够避免维度爆炸。
在元数据配置里，指定哪些维度允许在GMS接口查询，Flink 只预计算这些特定的 Key，将维度直接“扁平化”进 Key 的名字里。
例如原本不带维度的 Key: metrics:user:u123:pv (总PV)，带维度的 Key: metrics:user:u123:pv:page_home (首页PV)， 带更多维度的 Key: metrics:user:u123:pv:page_home:os_ios (首页且iOS的PV)。
查询逻辑：如果入参是 {"page": "home"}, 后端代码把 Key 拼成 ...:pv:page_home 去 Redis GET。


### 维度管理采用配置驱动的方式

#### 1. 统一维度代号(开发侧价值)

对于GMS接口，例如查询用户（10000001）在北京的登录次数：

`POST /api/v1/m_snap`
```json
{
  "ec": "user",
  "eid": 10000001,
  "metrics": [
    {
      "code": "login_count",
      "v": 2,
      "dims": {
        "city": "Beijing"
      }
    }
  ],
  "snapshot_ts": 123456789000,
  "is_atomic": true
}
```

**参数说明**:
- `metrics.code`: 统一维度代号（必传）
- `metrics.v`: 指定版本号（可选），详见[DESIGN-METRIC-VERSION-20251214](DESIGN-METRIC-VERSION-20251214.md)
- `metrics.dims`: 维度值（可选）
- `snapshot_ts`: 指定快照时间（UNIX时间戳，毫秒）（可选）,详见[DESIGN-SNAPSHOT-AT-SPECIFIC-TIME-20251214](DESIGN-SNAPSHOT-AT-SPECIFIC-TIME-20251214.md)
- `atomic`: 是否要求多个指标的快照时间一致（可选）,详见[DESIGN-SNAPSHOT-AT-SPECIFIC-TIME-20251214](DESIGN-SNAPSHOT-AT-SPECIFIC-TIME-20251214.md)


后端的处理逻辑是：
1. API服务接收入参`ec=user`和`eid=10000001`；解析`metrics[0].dims.city=Beijing`得到`city=Beijing`；
2. 联表查询`entity_meta`、`metric_meta`和`x`表，通过`entity_meta.code=user`和`x.alias=city`，把统一维度代号`city`替换为真实的维度代号`metric_meta.code=born_city`，同时得到数值的获取方式`x.data_uri`。
3. 拼接 Redis Key: mx:user:10000001:login_count:born_city_Beijing；

查询设备（40000001）在北京的登陆次数：

`POST /api/v1/m_snap`
```json
{
  "ec": "device",
  "eid": 40000001,
  "metrics": [
    {
      "code": "login_count",
      "dims": {
        "city": "Beijing"
      }
    }
  ]
}
```

后端的处理逻辑是：
1. API服务接收入参`ec=device`和`eid=40000001`；解析`metrics[0].dims.city=Beijing`得到`city=Beijing`；
2. 联表查询`entity_meta`、`metric_meta`和`x`表，通过`entity_meta.code=device`和`x.alias=city`，把统一维度代号`city`替换为真实的维度代号`metric_meta.code=produce_city`，同时得到数值的获取方式`x.data_uri`。
3. 拼接 Redis Key: mx:device:40000001:login_count:produce_city_Beijing；


可以看到几个特点：
1. 无论entity是什么，只要是查询city维度，在前端的参数名是一致的`city`（在dims对象中）。统一的维度代号，在结合不同的物理实体时，产生不同的指标含义，进而对应不同的取值方式。这种多态简化了前端查询的难度。
2. 底层存储的维度代号是专用的（user实体是born_city，device实体是produce_city），避免了数据混淆的风险。同样的，Clickhouse的维度代号也是专用的。
3. 每个metric可以独立配置维度条件，支持复杂查询场景（如不同metric需要不同的维度过滤）。
   总之，前端查询时使用统一代号，解耦了前端和底层存储的关系，提升了系统的灵活性和可维护性。


#### 2. 智能参数校验(运营/调用方价值)

对于GMS接口，当传入`ec=transaction`且`dims={"os_version": "14.0"}`（经过解析统一维度代号而得到）时，后端的处理逻辑是：
1. 联表查询`entity_meta`、`metric_meta`和`x`表;
2. 判断：发现“交易”实体并没有关联“操作系统版本”维度；
3. 动作：直接返回 400 Bad Request，无需去 Redis 查，节省了一次网络IO。

#### 3. Flink 自动化 Key 生成 (核心底层逻辑)

这是对于GMS接口的最大的贡献——自动化预计算。

1. Flink 启动时，扫描`x`表，发现 User 实体关联了 City 维度，且 is_hot=1。
2. 代码生成：Flink 自动生成逻辑——“每当处理 User 日志，解析 data_uri 拿到城市值，拼接 Key mx:user:{uid}:{metric_codes}:city_{val} 并写入 Redis”。
3. 发现 Transaction 实体关联了 City 维度，但 is_hot=0（也许因为交易对城市维度的实时查询需求不大，或者基数太大）。


## API

### Implementation
**GMS接口**
为了保证GMS接口的高性能，要求：
- 不要读 DB。
- Redis.mget(keys)。
- 批量接口：必须支持一次查多个指标。建立 Redis 连接很贵，尽量在一个 RTT (Round Trip Time) 里把该拿的都拿走。
- 空值处理：如果 Redis 里查不到 risk_score，返回 null。因为 0 有业务含义（比如得分为0），而 null 代表没数据。让业务方自己决定默认值。
- 直接序列化返回。
- 逻辑耗时应 < 2ms。

批量接口的复杂场景示例：查询用户（10000001）的多个指标，每个指标有不同的维度条件：

`POST /api/v1/m_snap`
```json
{
  "entity_code": "user",
  "entity_id": 10000001,
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
  ]
}
```

后端的处理逻辑是：
1. 对于每个metric，分别解析其dims（如果有的话）；
2. 为每个metric独立拼接Redis Key并查询；
3. 合并所有metric的查询结果返回。

**GMA接口**
为了保证GMA接口的灵活性，要求：
- 前端传 filters: {city: beijing} -> 转换成 SQL WHERE dims['city'] = 'beijing'。
- 前端传 metric_codes: [pay_amount] -> 查 MySQL 发现聚合类型是 SUM -> 转换成 SQL sum(value) as pay_amount (配合 WHERE metric_name='pay_amount')。
- 注意 ClickHouse 的 Map 字段查询语法。
- 支持分页查询，避免一次查太多数据压垮系统。
