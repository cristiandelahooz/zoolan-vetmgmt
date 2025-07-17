package com.wornux.utils;

/**
 * @author me@fredpena.dev
 * @created 28/02/2025  - 12:31
 */
public final class FileSizeUtils {
    private static final String[] UNITS = { "bytes", "KB", "MB", "GB", "TB", "PB", "EB" };
    private static final double UNIT_STEP = 1024.0;

    private FileSizeUtils() {
    }

    public static String formatFileSize(long contentLength) {
        if (contentLength <= 0)
            return "0 bytes";

        int unitIndex = 0;
        double size = contentLength;

        while (size >= UNIT_STEP && unitIndex < UNITS.length - 1) {
            size /= UNIT_STEP;
            unitIndex++;
        }

        return String.format("%.2f %s", size, UNITS[unitIndex]);
    }

}
