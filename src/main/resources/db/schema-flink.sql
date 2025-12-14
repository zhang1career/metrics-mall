CREATE TABLE metrics_detail (
    `tid`    UInt32 COMMENT 'tenant id',
    `uid`    String COMMENT 'user id',
    `dims`   Map(String, String) COMMENT 'dynamic dimensions',
    `code`   String COMMENT 'metric code',
    `value`  Float64,
    `count`  UInt32 DEFAULT 1,
    `ts`     DateTime64(3) COMMENT 'event timestamp',
    `date`   Date   DEFAULT toDate(event_ts)
) ENGINE = MergeTree(timestamp)
PARTITION BY toYYYYMM(date)
ORDER BY (`tid`, `date`, `code`, cityHash64(toString(uid)))
TTL event_date + INTERVAL 6 MONTH;

