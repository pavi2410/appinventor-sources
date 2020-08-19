package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockVisibleExtension;

import java.util.HashMap;
import java.util.Map;

public class MockVceManager {
    // mapping for uuid to iframe id
    private static final Map<String, String> uuidToIframeMap = new HashMap<>();

    // maps uuid to MVE
    private static final Map<String, MockComponentFactory> vceInstances = new HashMap<>();

    public static void create(String type, String iframeId) {
        final String uuid = genUuid();

        MockComponentFactory mcf = createFactoryForMVE(type);
        MockComponentRegistry.register(type, mcf);

        uuidToIframeMap.put(uuid, iframeId);
        vceInstances.put(uuid, mcf);
    }

    public static MockComponentFactory createFactoryForMVE(String type) {
        return new MockComponentFactory() {
            @Override
            public MockComponent create(SimpleEditor editor) {
                return new MockVisibleExtension(editor, type);
            }
        };
    }

    public static void remove(String iframeId) {
        vceInstances.remove(iframeId);
    }

    public static boolean isPresent(String iframeId) {
        return vceInstances.containsKey(iframeId);
    }

    public static void reset() {
        vceInstances.clear();
    }

    private static native String genUuid() /*-{
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }-*/;
}