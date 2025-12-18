CREATE TABLE `entity_meta` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(128) UNIQUE NOT NULL DEFAULT '' COMMENT 'user, device, trans',
    `name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT '用户, 设备, 交易单',
    `description` TEXT,
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Update time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_entity_code` (`code`)
) COMMENT 'entity meta';

CREATE TABLE `metric_meta` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(128) UNIQUE NOT NULL DEFAULT '' COMMENT 'metric name in english',
    `name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT 'metric name in chinese',
    `description` TEXT,
    `metric_type` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'metric type, 0=ATOMIC, 1=DERIVED, 2=COMPOSITE',
    `value_type`  INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'value type, 0=STR, 1=INT, 2=LONG, 3=FLOAT, 4=BOOL, 5=DATE, 6=OBJ',
    `precision`   INT UNSIGNED NOT NULL DEFAULT '2' COMMENT 'decimal precision for FLOAT type',
    `agg_type`    INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'aggregation type, 0=sum, 1=avg, 2=max, 3=min, 4=count',
    `unit`        VARCHAR(32)  NOT NULL DEFAULT '' COMMENT 'unit of the metric',
    `validation`  VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'valid range of metric values, json format',
    `card_max`    INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'max value of cardinality',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Update time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_metric_code` (`code`)
) COMMENT 'metric meta';

CREATE TABLE `metric_version` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `mid`         BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'refer to metric_meta.id',
    `version`     INT    UNSIGNED NOT NULL DEFAULT 1 COMMENT 'version number',
    `is_main`     TINYINT         NOT NULL DEFAULT 0 COMMENT 'whether this version is the main version, 0=no, 1=yes',
    `metric_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'metric status, 0=OFFLINE, 1=DEV, 2=TEST, 3=GRAY, 4=ONLINE, 5=DEPRECATED',
    `begin_status_t` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'timestamp when metric went online, unix timestamp in milliseconds',
    `calc_logic`  TEXT COMMENT 'calculation logic',
    `is_explainable` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'whether this metric supports explainability, 0=no, 1=yes',
    `explain_type` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'explainability type, 0=formula, 1=rule, 2=feature_importance, 3=lineage',
    `explain_config` TEXT COMMENT 'explainability configuration, JSON format',
    `change_log`  TEXT COMMENT 'change log',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Update time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_metric_version` (`mid`, `version`)
) COMMENT 'metric version';

CREATE TABLE IF NOT EXISTS `metric_lineage` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `src_id`      BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'source metric id, refer to metric_meta.id',
    `dest_id`     BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'destination metric id, refer to metric_meta.id',
    `depend_type` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'aggregation type, 0=derived, 1=aggregated, 2=joined',
    `trans_logic` TEXT COMMENT 'SQL or Flink code to derive target metric from source metric(s)',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Update time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_depend` (`src_id`, `dest_id`),
    INDEX `idx_dest` (`dest_id`)
) COMMENT 'metric lineage';

CREATE TABLE IF NOT EXISTS `dim` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(128) UNIQUE NOT NULL DEFAULT '' COMMENT 'dimension name in english',
    `name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT 'dimension name in chinese',
    `validation`  VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'valid range of dimension values, json format',
    `ct`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Create time, UNIX timestamp in seconds',
    `ut`          INT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'Update time, UNIX timestamp in seconds',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_dim_code` (`code`)
) COMMENT 'dimension meta';

CREATE TABLE `x` (
    `eid`         BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '关联的entity',
    `mid`         BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '关联的metric_meta',
    `alias`       VARCHAR(256)    NOT NULL DEFAULT '' COMMENT 'dimension alias',
    `data_uri`    VARCHAR(1024)   NOT NULL DEFAULT '' COMMENT '数据来源的地址',
    PRIMARY KEY (`eid`, `mid`),
    UNIQUE INDEX `uni_entity_dim_alias` (`eid`, `alias`)
) COMMENT 'entity and metric relationship';

CREATE TABLE `y` (
    `mid`         BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '关联的metric_meta',
    `did`         BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '关联的dim',
    `is_hot`      TINYINT         NOT NULL DEFAULT 0 COMMENT 'dim code will suffix to redis key, so the metric value can be lookuped in real-time, 0=no, 1=yes',
    `validation`  VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'valid range of dimension values, json format',
    PRIMARY KEY (`mid`, `did`)
) COMMENT 'metric and dimension relationship';