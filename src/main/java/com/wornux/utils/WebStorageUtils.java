package com.wornux.utils;

import com.vaadin.flow.component.page.WebStorage;

/**
 * @author me@fredpena.dev
 * @created 09/02/2025 - 12:45
 */
public final class WebStorageUtils {

    private WebStorageUtils() {
    }

    public static void setItem(StorageKey key, String value) {
        WebStorage.setItem(key.name(), value);
    }

    public static void getItem(StorageKey key, WebStorage.Callback callback) {
        WebStorage.getItem(key.name(), callback);
    }

    public enum StorageKey {
        CUSTOMER_RECENT
    }
}
