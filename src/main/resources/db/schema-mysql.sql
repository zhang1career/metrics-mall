CREATE TABLE `metric` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(128) UNIQUE NOT NULL DEFAULT '' COMMENT 'metric name in english',
    `name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT 'metric name in chinese',
    `description` TEXT,
    `metric_type` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'metric type, 0=ATOMIC, 1=DERIVED, 2=COMPOSITE',
    `value_type`  INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'value type, 0=BOOL, 1=INT, 2=FLOAT, 3=STRING',
    `agg_type`    INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'aggregation type, 0=sum, 1=avg, 2=max, 3=min, 4=count',
    `unit`        VARCHAR(32)  NOT NULL DEFAULT '' COMMENT 'unit of the metric',
    `validation`  VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'valid range of metric values, json format',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Update time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_metric_code` (`code`)
) COMMENT 'metric definition';

CREATE TABLE IF NOT EXISTS `metric_lineage` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `src_id`      BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'source metric id, refer to metric.id',
    `dest_id`     BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'destination metric id, refer to metric.id',
    `depend_type` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'aggregation type, 0=derived, 1=aggregated, 2=joined',
    `trans_logic` TEXT COMMENT 'SQL or Flink code to derive target metric from source metric(s)',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Update time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_depend` (`src_id`, `dest_id`),
    INDEX `idx_dest` (`dest_id`)
) COMMENT 'metric lineage';

CREATE TABLE `entity_meta` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(128) UNIQUE NOT NULL DEFAULT '' COMMENT 'user, device, trans',
    `name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT '用户, 设备, 交易单',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_entity_code` (`code`)
) COMMENT 'entity meta information';

CREATE TABLE IF NOT EXISTS `dim` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(128) UNIQUE NOT NULL DEFAULT '' COMMENT 'dimension code',
    `name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT 'dimension name in chinese',
    `value_type`  INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'dimension type',
    `validation`  VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'valid range of dimension values, json format',
    `card_limit`  INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'max value of cardinality',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Update time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_dim_code` (`code`)
) COMMENT 'dimension definition';

CREATE TABLE `x` (
    `eid`         BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '关联的entity',
    `did`         BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '关联的dim',
    `da`          VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'dimension alias',
    `src_path`    VARCHAR(1024)   NOT NULL DEFAULT '' COMMENT '数据来源的地址',
    `is_hot`      TINYINT         NOT NULL DEFAULT 0 COMMENT 'dim code will surfix to redis key, so the metric value can be lookuped in real-time, 0=no, 1=yes',
    PRIMARY KEY (`eid`, `did`),
    UNIQUE INDEX `uni_entity_dim_alias` (`eid`, `da`)
) COMMENT 'entity meta and dimension relationship';