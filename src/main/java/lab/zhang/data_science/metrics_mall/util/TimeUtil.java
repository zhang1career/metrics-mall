package lab.zhang.data_science.metrics_mall.util;

import java.time.LocalDateTime;


public class TimeUtil {
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public static long dateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
    }

    /**
     * Parse seconds from expression like "5m", "2h", "1d".
     * Supported units:
     *   1s -> 1
     *   5m -> 300
     *   2h -> 7200
     *   1d -> 86400
     *   1w -> 604800
     *   1M -> 2592000 (30 days)
     *   1y -> 31536000 (365 days)
     * @param expression time expression
     * @return seconds
     */
    public static long parseSecondsFromExpression(String expression) {
        if (expression == null || expression.length() < 2) {
            throw new IllegalArgumentException("Invalid time expression: " + expression);
        }
        char unit = expression.charAt(expression.length() - 1);
        String numberStr = expression.substring(0, expression.length() - 1);
        long number;
        try {
            number = Long.parseLong(numberStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in time expression: " + expression);
        }
        switch (unit) {
            case 's':
                return number;
            case 'm':
                return number * 60;
            case 'h':
                return number * 3600;
            case 'd':
                return number * 86400;
            case 'w':
                return number * 604800;
            case 'M':
                return number * 2592000;
            case 'y':
                return number * 31536000;
            default:
                throw new IllegalArgumentException("Invalid unit in time expression: " + expression);
        }
    }

}
