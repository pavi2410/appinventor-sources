package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockVisibleExtension;

import java.util.HashMap;
import java.util.Map;

public class MockVceManager {

    /*
        Each type has a MockComponentFactory
        Each type can have multiple UUIDs
        Each a MockVisibleExtension has a UUID
    */

    // map of type to MCF
    private static final Map<String, MockComponentFactory> factoryMap = new HashMap<>();

    // map of uuid to type
    private static final Map<String, String> uuidToTypeMap = new HashMap<>();

    // map of uuid to MVE
    private static final Map<String, MockVisibleExtension> vceInstancesMap = new HashMap<>();

    public static void create(String type, String uuid) {
//        final String uuid = genUuid();

        MockComponentFactory mcf = createFactoryForMVE(type, uuid);
        MockComponentRegistry.register(type, mcf);

        factoryMap.put(type, mcf);
        uuidToTypeMap.put(uuid, type);
    }

    public static MockComponentFactory createFactoryForMVE(final String type, final String uuid) {
        return new MockComponentFactory() {
            @Override
            public MockComponent create(SimpleEditor editor) {
                MockVisibleExtension mve = new MockVisibleExtension(editor, type);
                vceInstancesMap.put(uuid, mve);
                return mve;
            }
        };
    }

    public static MockVisibleExtension getMveFromUuid(String uuid) {
        return vceInstancesMap.get(uuid);
    }

    public static void remove(String iframeId) {
        factoryMap.remove(iframeId);
    }

    public static void reset() {
        factoryMap.clear();
        vceInstancesMap.clear();
    }

    private static native String genUuid() /*-{
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }-*/;
}