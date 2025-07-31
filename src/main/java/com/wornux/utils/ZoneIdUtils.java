package com.wornux.utils;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public class ZoneIdUtils {

    private ZoneIdUtils() {

    }

    public static List<String> zonesWithOffset() {

        return ZoneId.getAvailableZoneIds().stream().sorted().map(id -> {
            ZoneId zoneId = ZoneId.of(id);
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            ZoneOffset offset = now.getOffset();
            return String.format("%s (UTC%s)", id, offset);
        }).toList();
    }
}
