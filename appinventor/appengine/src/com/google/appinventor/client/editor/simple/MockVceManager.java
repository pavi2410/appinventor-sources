package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockVisibleExtension;

import java.util.HashMap;
import java.util.Map;

public class MockVceManager {

    /*
        Each type has a MockComponentFactory
        Each type can have multiple UUIDs
        Each MockVisibleExtension has a UUID
    */

    // map of uuid to type
    private static final Map<String, String> uuidToTypeMap = new HashMap<>();

    // map of uuid to MVE
    private static final Map<String, MockVisibleExtension> vceInstancesMap = new HashMap<>();

    public static void createMVE(final String type, final String uuid) {
//        final String uuid = genUuid();

        MockComponentRegistry.register(type, new MockComponentFactory() {
            @Override
            public MockComponent create(SimpleEditor editor) {
                MockVisibleExtension mve = new MockVisibleExtension(editor, type, uuid);
                vceInstancesMap.put(uuid, mve);
                return mve;
            }
        });

        uuidToTypeMap.put(uuid, type);
    }

    public static MockVisibleExtension getMveFromUuid(String uuid) {
        return vceInstancesMap.get(uuid);
    }

    public static String getTypeFromUuid(String uuid) {
        return uuidToTypeMap.get(uuid);
    }

    public static void reset() {
        uuidToTypeMap.clear();
        vceInstancesMap.clear();
    }

    // TODO: remove
    private static native String genUuid() /*-{
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }-*/;
}