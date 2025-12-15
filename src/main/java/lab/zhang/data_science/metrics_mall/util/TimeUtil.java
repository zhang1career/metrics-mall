package lab.zhang.data_science.metrics_mall.util;

import java.time.LocalDateTime;


public class TimeUtil {
    public static long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

    public static long dateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
    }
}
