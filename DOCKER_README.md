# Rule Engine Docker 部署指南

本文档介绍如何使用Docker部署Metrics Mall应用，支持外部配置MySQL连接信息。

## 目录结构

```
.
├── Dockerfile                   # Docker镜像构建文件
├── docker-compose.yml           # 部署配置
├── docker-build.sh              # 构建脚本
└── DOCKER_README.md             # 本文档
```

## 快速开始

首先确保项目已构建：

### 1. 构建应用

```shell
mvn clean package -DskipTests
```

### 2. 构建Docker镜像

```shell
./docker-build.sh
```

或指定版本号

```shell
./docker-build.sh v1.0.0
```

### 3. 启动
```shell
dcup
```