package com.synsenetwork.inventorybackup.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    /**
     * Formats a timestamp to a string.
     * @param timestamp The timestamp to format.
     * @return The formatted timestamp.
     */
    public static String formatTime(long timestamp) {
        // Convert timestamp to Instant
        Instant instant = Instant.ofEpochMilli(timestamp);

        // Get system default zone id
        ZoneId zoneId = ZoneId.systemDefault();

        // Convert instant to LocalDateTime
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();

        // Create formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Format time

        return localDateTime.format(formatter);
    }
}
