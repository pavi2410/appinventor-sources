package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.editor.simple.components.MockVisibleExtension;

import java.util.HashMap;
import java.util.Map;

public class MockVceManager {
    private static final Map<String, MockVisibleExtension> vceInstances = new HashMap<>();

    public static void insert(String iframeId, MockVisibleExtension mve) {
        vceInstances.put(iframeId, mve);
    }

    public static void remove(String iframeId) {
        vceInstances.remove(iframeId);
    }

    public static boolean isPresent(String iframeId) {
        return vceInstances.containsKey(iframeId);
    }

    public static MockVisibleExtension getMve(String iframeId) {
        return vceInstances.get(iframeId);
    }

    public static void reset() {
        vceInstances.clear();
    }
}