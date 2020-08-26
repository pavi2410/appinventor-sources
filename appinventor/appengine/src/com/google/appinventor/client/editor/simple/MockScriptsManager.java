package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.editor.simple.components.MockVisibleExtension;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MockScriptsManager implements ComponentDatabaseChangeListener {

    public static MockScriptsManager INSTANCE;

    private final long projectId;
    private final YaProjectEditor projectEditor;

    private final List<String> loadedMocks = new ArrayList<>(); // list of component types

    private MockScriptsManager(long projectId, YaProjectEditor projectEditor) {
        this.projectId = projectId;
        this.projectEditor = projectEditor;

        projectEditor.addComponentDatbaseListener(this);

        initIframeListener();
    }

    /**
     * Initialises the [MockScriptsManager] on project load and registers the necessary listeners.
     *
     * @param projectId     Project ID of the currently "visible" project
     * @param projectEditor The [ProjectEditor] associated with the currently opened project
     */
    public static void init(long projectId, YaProjectEditor projectEditor) {
        if (INSTANCE != null) {
            OdeLog.log("<MSM:init:40> INSTANCE != null; destroying...");
            destroy();
        }
        INSTANCE = new MockScriptsManager(projectId, projectEditor);

        OdeLog.log("<MSM:init:46> inited! projectId = " + projectId);
    }

    public static void destroy() {
        OdeLog.log("<MSM:destroy:50> destroying project = " + INSTANCE.projectId);
        INSTANCE.removeIframeListener();
        INSTANCE.projectEditor.removeComponentDatbaseListener(INSTANCE);
        INSTANCE.unloadAll();
        INSTANCE = null;

        MockComponentRegistry.reset();
    }

    /**
     * Loads a Mock<Component> file for the given component type.
     *
     * @param type The FQCN of the component
     */
    public void load(final String type) {
        if (loadedMocks.contains(type)) {
            return; // script already loaded; don't load again!
        }

        String pkgName = type.substring(0, type.lastIndexOf('.'));
        String simpleName = type.substring(type.lastIndexOf('.') + 1);

        SimpleComponentDatabase scd = SimpleComponentDatabase.getInstance(this.projectId);
        if (!scd.getComponentExternal(simpleName)
                || scd.getNonVisible(simpleName)
                || !scd.hasCustomMock(simpleName)) {
            return; // Component doesn't have its own custom Mock, so don't try to load it
        }

        OdeLog.log("<MSM:load:79> loading for type = " + type + " pkgName = " + pkgName + " simpleName = " + simpleName + " package = " + projectId);

        IFrameElement iFrameElement = Document.get().createIFrameElement();
        iFrameElement.setId("Mock_for_" + type);
        iFrameElement.setAttribute("sandbox", "allow-scripts");
        iFrameElement.setSrc(GWT.getModuleBaseURL() + ServerLayout.GET_MOCK_SCRIPTS_SERVLET + "?projectId=" + projectId + "&type=" + type);
        Document.get().getBody().appendChild(iFrameElement);

        loadedMocks.add(type);
    }

    public void upgrade(String type) {
        if (loadedMocks.contains(type)) {
            unload(type);
            load(type);
        }
    }

    public void unload(String type) {
        OdeLog.log("<MSM:unload:98> unloading type = " + type + " project = " + projectId);
        loadedMocks.remove(type);

        Element iframeElement = Document.get().getElementById("Mock_for_" + type);
        Document.get().getBody().removeChild(iframeElement);

        String simpleName = type.substring(type.lastIndexOf('.') + 1); // todo: See MCR#register
        MockComponentRegistry.unregister(simpleName);
    }

    private void unloadAll() {
        OdeLog.log("<MSM:unloadAll:109> unloading all... project = " + projectId);
        for (String type : loadedMocks) {
            unload(type);
        }
    }

    private native void initIframeListener() /*-{
        $wnd.iframeListener = function iframeListener(event) {
//            console.log("<MSM:iframeListener:117>", event.source.id);
//            var sourceIframeId = event.source.id;

            var msg = JSON.parse(event.data);
            var action = msg["action"];
            var args = msg["args"];
            var type = msg["type"];
            var uuid = msg["uuid"];
            @MockScriptsManager::INSTANCE.@MockScriptsManager::messageInterpreter(*)(action, args, type, uuid);
        }

        $wnd.addEventListener('message', iframeListener, false);
    }-*/;

    private native void removeIframeListener() /*-{
        $wnd.removeEventListener('message', $wnd['iframeListener'], false);
    }-*/;

    /**
     * Posts message to the iframe which is related by the type.
     *
     * @param action action to perform in the iframe
     * @param args   arguments to accompany the action
     * @param type   Used to find the iframe
     * @param uuid   UUID of the MVCE
     */
    public native void postMessage(String action, String[] args, String type, String uuid) /*-{
        var iframeEl = $doc.getElementById('Mock_for_' + type);
        var iframeWindow = iframeEl.contentWindow;
        var msg = JSON.stringify({
            action: action,
            args: args,
            type: type,
            uuid: uuid
        });
        console.log("<MSM:postMessage:151> sending msg to iframe for type = " + type + ", msg = " + msg);
        iframeWindow.postMessage(msg, '*');
    }-*/;

    public void messageInterpreter(String action, String[] args, String type, String uuid) {
        OdeLog.log("<MSM:messageInterpreter:157> action = " + action + " type = " + type + " uuid = " + uuid);
        switch (action) { // TODO Look into creating this an enum
            case "registerMockComponent": {
                MockVceManager.createMVE(type, uuid);
                break;
            }
            case "initializeComponent": {
                OdeLog.log("<MSM:messageInterpreter:167> initializeComponent html = " + args[0]);
                MockVisibleExtension mve = MockVceManager.getMveFromUuid(uuid);
                mve.initComponent(SafeHtmlUtils.fromString(args[0]));
                break;
            }
            case "updateMockComponent": {
                MockVisibleExtension mve = MockVceManager.getMveFromUuid(uuid);
                mve.getElement().setInnerSafeHtml(SafeHtmlUtils.fromString(args[0]));
                break;
            }
            case "getName": {
                MockVisibleExtension mve = MockVceManager.getMveFromUuid(uuid);
                final String name = mve.getName();

                postMessage("getName.callback", new String[]{name}, type, uuid);
                break;
            }
            case "getPropertyValue": {
                MockVisibleExtension mve = MockVceManager.getMveFromUuid(uuid);
                final String propertyValue = mve.getPropertyValue(args[0]);

                postMessage("getPropertyValue.callback", new String[]{propertyValue}, type, uuid);
                break;
            }
            case "changeProperty": {
                MockVisibleExtension mve = MockVceManager.getMveFromUuid(uuid);
                mve.changeProperty(args[0], args[1]);
                break;
            }
        }
    }

    //// ComponentDatabaseChangeListener

    @Override
    public void onComponentTypeAdded(List<String> componentTypes) {
        SimpleComponentDatabase scd = SimpleComponentDatabase.getInstance(this.projectId);
        OdeLog.log("<MSM:onCompAdded:194> components added: project = " + projectId);
        for (String componentType : componentTypes) {
            String fqcn = scd.getComponentType(componentType); // Ensure [componentType] is a FQCN
            OdeLog.log("<MSM:onCompAdded:197> fqcn = " + fqcn);
            if (loadedMocks.contains(fqcn)) {
                upgrade(fqcn);
            } else {
                load(fqcn);
            }
        }
    }

    @Override
    public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
        return true;
    }

    @Override
    public void onComponentTypeRemoved(Map<String, String> componentTypes) {
        // returns map??
        OdeLog.log("<MSM:onCompRemoved:214> components removed: project = " + projectId);
        for (String fqcn : componentTypes.values()) {
            OdeLog.log("<MSM:onCompRemoved:216> fqcn = " + fqcn);
            unload(fqcn);
        }
    }

    @Override
    public void onResetDatabase() {
        unloadAll();
    }
}
