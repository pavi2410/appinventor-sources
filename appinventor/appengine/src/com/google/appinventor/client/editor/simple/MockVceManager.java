package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockVisibleExtension;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel;
import com.google.appinventor.client.output.OdeLog;

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

    public static void createMVE(final String type) {
        final String uuid = genUuid();

        final String simpleName = type.substring(type.lastIndexOf('.') + 1);

        // TODO: Make MCR accept FQCN
        MockComponentRegistry.register(simpleName, new MockComponentFactory() {
            @Override
            public MockComponent create(SimpleEditor editor) {
                OdeLog.log("creating MockComponent for type = " + type + " uuid = " + uuid);
                MockScriptsManager.INSTANCE.postMessage("instantiateComponent", new String[]{}, type, uuid);
                MockVisibleExtension mve = new MockVisibleExtension(editor, simpleName, uuid);
                vceInstancesMap.put(uuid, mve);
                return mve;
            }
        });

        for (FileEditor openFileEditor : Ode.getInstance().getCurrentFileEditor().getProjectEditor().getOpenFileEditors()) {
            if (openFileEditor instanceof YaFormEditor) {
                OdeLog.log("<MVM> got form editor");
                YaFormEditor yaFormEditor = (YaFormEditor) openFileEditor;
                YoungAndroidPalettePanel componentPalettePanel = (YoungAndroidPalettePanel) yaFormEditor.getComponentPalettePanel();

                componentPalettePanel.getSimplePaletteItem(simpleName).createMockComponent();
            }
        }

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

    private static native String genUuid() /*-{
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }-*/;
}