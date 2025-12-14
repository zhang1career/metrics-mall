# Docker Compose 一键部署方案

## 方案概述

本文档提供 Metrics Mall 系统的 Docker Compose 一键部署方案，包含**简化版**（适合中小企业）和**生产版**（可扩展集群）两种配置。

## 架构设计

### 简化版架构（单节点）

```
┌─────────────────────────────────────────┐
│         Docker Compose Network          │
│                                         │
│  ┌──────────┐  ┌──────────┐            │
│  │  MySQL   │  │  Redis   │            │
│  │  :3306   │  │  :6379   │            │
│  └──────────┘  └──────────┘            │
│                                         │
│  ┌──────────┐  ┌──────────┐            │
│  │ClickHouse│  │  Kafka   │            │
│  │  :8123   │  │  :9092   │            │
│  └──────────┘  └──────────┘            │
│                                         │
│  ┌──────────┐  ┌──────────┐            │
│  │  Flink   │  │   App    │            │
│  │  :8081   │  │  :8080   │            │
│  └──────────┘  └──────────┘            │
│                                         │
└─────────────────────────────────────────┘
```

### 生产版架构（集群模式）

- **Kafka**: 3 节点集群（KRaft 模式）
- **Redis**: 3 主 3 从集群
- **Flink**: 1 JobManager + 3 TaskManager
- **MySQL**: 主从复制（可选）
- **ClickHouse**: 单节点（可扩展为集群）

## 组件说明

### 1. MySQL (元数据存储)

- **镜像**: `mysql:8.0`
- **端口**: 3306
- **数据持久化**: Docker volume `metrics-mall-mysql-data`
- **初始化**: 自动执行 `schema-mysql.sql`
- **配置优化**:
  - 字符集: utf8mb4
  - 最大连接数: 1000
  - InnoDB 缓冲池: 512MB

### 2. Redis (热存储/GMS)

- **镜像**: `redis:7.2-alpine`
- **端口**: 6379
- **数据持久化**: AOF + RDB
- **内存策略**: LRU 淘汰，最大 2GB
- **配置**:
  - `appendonly yes`: 启用 AOF
  - `maxmemory-policy allkeys-lru`: LRU 淘汰策略

### 3. ClickHouse (冷存储/GMA)

- **镜像**: `clickhouse/clickhouse-server:24.1`
- **端口**: 8123 (HTTP), 9000 (Native)
- **数据持久化**: Docker volume `metrics-mall-clickhouse-data`
- **配置**: 自定义 `config.xml` 和 `users.xml`

### 4. Kafka (数据缓冲)

- **镜像**: `apache/kafka:3.7.0`
- **端口**: 9092 (Broker), 9093 (Controller)
- **模式**: KRaft（无需 Zookeeper）
- **配置**:
  - 分区数: 3
  - 保留时间: 7 天
  - 自动创建 Topic: 启用

### 5. Flink (实时计算)

- **镜像**: `flink:1.19-scala_2.12-java17`
- **端口**: 8081 (JobManager Web UI)
- **组件**:
  - JobManager: 1 个
  - TaskManager: 1 个（可扩展）
- **资源配置**:
  - JobManager 内存: 1600MB
  - TaskManager 内存: 1728MB
  - Task Slots: 2

### 6. Metrics Mall Application

- **构建**: 基于 `Dockerfile` 多阶段构建
- **端口**: 8080
- **健康检查**: `/actuator/health`
- **日志**: 挂载到 `./logs` 目录

## 部署步骤

### 前置条件

1. **系统要求**:
   - Docker Engine >= 20.10
   - Docker Compose >= 2.0
   - 内存 >= 8GB（推荐 16GB）
   - 磁盘 >= 50GB

2. **环境准备**:
   ```bash
   # 克隆项目
   git clone <repository>
   cd metrics-mall
   
   # 复制环境变量
   cp .env.example .env
   ```

### 快速启动

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 验证部署

```bash
# 检查 MySQL
docker-compose exec mysql mysql -u metrics_user -pmetrics_pass metrics_mall -e "SHOW TABLES;"

# 检查 Redis
docker-compose exec redis redis-cli ping

# 检查 ClickHouse
curl http://localhost:8123/ping

# 检查 Kafka
docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# 检查 Flink
curl http://localhost:8081

# 检查应用
curl http://localhost:8080/actuator/health
```

## 配置说明

### 环境变量 (.env)

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `MYSQL_ROOT_PASSWORD` | `metrics_mall_root` | MySQL root 密码 |
| `MYSQL_DATABASE` | `metrics_mall` | 数据库名 |
| `MYSQL_USER` | `metrics_user` | 数据库用户 |
| `MYSQL_PASSWORD` | `metrics_pass` | 数据库密码 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `CLICKHOUSE_HTTP_PORT` | `8123` | ClickHouse HTTP 端口 |
| `KAFKA_PORT` | `9092` | Kafka 端口 |
| `FLINK_JOBMANAGER_PORT` | `8081` | Flink JobManager 端口 |
| `APP_PORT` | `8080` | 应用端口 |

### 数据持久化

所有数据存储在 Docker volumes 中，即使容器删除，数据也不会丢失：

```bash
# 查看 volumes
docker volume ls | grep metrics-mall

# 备份 MySQL 数据
docker run --rm -v metrics-mall-mysql-data:/data -v $(pwd):/backup \
  alpine tar czf /backup/mysql-backup-$(date +%Y%m%d).tar.gz /data
```

## 生产环境部署

### 使用生产配置

```bash
# 使用生产配置（集群模式）
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### 扩展服务

```bash
# 扩展 Flink TaskManager 到 3 个
docker-compose up -d --scale flink-taskmanager=3

# 扩展应用实例（需要负载均衡）
docker-compose up -d --scale metrics-mall-app=3
```

### 资源限制

在生产环境中，建议为每个服务设置资源限制：

```yaml
services:
  mysql:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

## 性能优化

### MySQL 优化

```yaml
command:
  - --innodb_buffer_pool_size=1G
  - --max_connections=2000
  - --query_cache_size=256M
```

### Redis 优化

```yaml
command:
  - redis-server
  - --maxmemory 4gb
  - --maxmemory-policy allkeys-lru
```

### ClickHouse 优化

编辑 `docker/clickhouse/config.xml`:

```xml
<max_memory_usage>20000000000</max_memory_usage>
<max_concurrent_queries>200</max_concurrent_queries>
```

## 监控与告警

### 集成 Prometheus

```yaml
prometheus:
  image: prom/prometheus:latest
  ports:
    - "9090:9090"
  volumes:
    - ./docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
```

### 集成 Grafana

```yaml
grafana:
  image: grafana/grafana:latest
  ports:
    - "3000:3000"
  environment:
    - GF_SECURITY_ADMIN_PASSWORD=admin
```

## 故障排查

### 查看服务健康状态

```bash
docker-compose ps
```

### 查看服务日志

```bash
# 所有服务日志
docker-compose logs

# 特定服务日志
docker-compose logs -f mysql
docker-compose logs -f metrics-mall-app

# 最近 100 行日志
docker-compose logs --tail=100 metrics-mall-app
```

### 进入容器调试

```bash
# 进入 MySQL 容器
docker-compose exec mysql bash

# 进入 Redis 容器
docker-compose exec redis redis-cli

# 进入应用容器
docker-compose exec metrics-mall-app sh
```

## 维护操作

### 停止服务

```bash
# 停止所有服务（保留数据）
docker-compose stop

# 停止并删除容器（保留数据）
docker-compose down

# 停止并删除容器和数据（危险！）
docker-compose down -v
```

### 更新服务

```bash
# 拉取最新镜像
docker-compose pull

# 重新构建应用
docker-compose build metrics-mall-app

# 滚动更新
docker-compose up -d --no-deps metrics-mall-app
```

## 安全建议

1. **修改默认密码**: 在 `.env` 中设置强密码
2. **限制网络访问**: 使用防火墙限制端口访问
3. **启用 TLS**: 生产环境启用 HTTPS/TLS
4. **定期更新**: 保持镜像版本更新
5. **备份策略**: 配置自动备份

## 成本估算

### 简化版（单节点）

- **服务器**: 1 台 8C16G 云服务器
- **存储**: 100GB SSD
- **月成本**: 约 ¥300-500（国内云厂商）

### 生产版（集群）

- **服务器**: 3 台 8C16G 云服务器
- **存储**: 300GB SSD
- **月成本**: 约 ¥900-1500（国内云厂商）

## 与 PRD 的对应关系

| PRD 要求 | Docker Compose 实现 |
|----------|---------------------|
| Kafka 3 节点 | 简化版：1 节点，生产版：3 节点集群 |
| Redis Cluster 3主3从 | 简化版：单节点，生产版：3主3从集群 |
| ClickHouse 单节点 | 单节点（可扩展为集群） |
| MySQL 元数据 | 单节点（可选主从） |
| Flink 实时计算 | JobManager + TaskManager（可扩展） |

## 总结

本方案提供了：

1. ✅ **简化版部署**: 适合中小企业，单机即可运行
2. ✅ **生产版部署**: 支持集群扩展，满足高可用需求
3. ✅ **一键启动**: 通过 Docker Compose 实现一键部署
4. ✅ **数据持久化**: 所有数据存储在 volumes 中
5. ✅ **健康检查**: 所有服务配置健康检查
6. ✅ **易于维护**: 提供完整的维护文档和脚本

该方案降低了部署门槛，使中小企业能够快速部署和使用 Metrics Mall 系统。

