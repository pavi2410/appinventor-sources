package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.ui.HTMLPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.appinventor.client.Ode.MESSAGES;

public final class MockScriptsManager implements ComponentDatabaseChangeListener, ProjectChangeListener {

    public static MockScriptsManager INSTANCE;

    private final long projectId;
    private final YaProjectEditor projectEditor;

    private final List<String> loadedMocks = new ArrayList<>(); // list of component types

    private static final String SDK_CODE_START = "<html><body><script>'use strict';let mockInstances={};let MockComponentRegistry={register:(type,clazz,uuid)=>{$wnd.postMessage(type);mockInstances[uuid]=new clazz()}};window.addEventListener('message',event=>{let{action,args,type,uuid}=event.data;messageInterpreter(action,args,type,uuid)});function messageInterpreter(action,args,type,uuid){switch(action){case 'onPropertyChanged':{mockInstances[uuid].onPropertyChanged(...args);break}}}class MockVisibleExtension{initComponent(element){this.refresh(element)}refresh(element){window.top.postMessage(element.outerHTML)}}";
    private static final String SDK_CODE_END = "</script></body></html>";

    private MockScriptsManager(long projectId, YaProjectEditor projectEditor) {
        this.projectId = projectId;
        this.projectEditor = projectEditor;

        projectEditor.addComponentDatbaseListener(this);
        Ode.getInstance().getProjectManager().getProject(projectId).addProjectChangeListener(this);
    }

    /**
     * Initialises the [MockScriptsManager] on project load and registers the necessary listeners.
     *
     * @param projectId     Project ID of the currently "visible" project
     * @param projectEditor The [ProjectEditor] associated with the currently opened project
     */
    public static void init(long projectId, YaProjectEditor projectEditor) {
        if (INSTANCE != null) {
            OdeLog.log("<MSM:init:45> INSTANCE != null; destroying...");
            destroy();
        }
        INSTANCE = new MockScriptsManager(projectId, projectEditor);

        INSTANCE.initIframeListener();
        OdeLog.log("<MSM:init:57> inited! projectId = " + projectId);
    }

    public static void destroy() {
        OdeLog.log("<MSM:destroy:63> destroying project = " + INSTANCE.projectId);
        INSTANCE.removeIframeListener();

        INSTANCE.projectEditor.removeComponentDatbaseListener(INSTANCE);
        Ode.getInstance()
                .getProjectManager()
                .getProject(INSTANCE.projectId)
                .removeProjectChangeListener(INSTANCE);
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

        OdeLog.log("<MSM:load:91> loading for type = " + type + " pkgName = " + pkgName + " simpleName = " + simpleName + " package = " + projectId);

        YoungAndroidProjectNode youngAndroidProjectNode = (YoungAndroidProjectNode) Ode.getInstance()
                .getProjectManager()
                .getProject(projectId)
                .getRootNode();
        String componentsFolder = youngAndroidProjectNode.getComponentsFolder().getFileId();
        String fileId = componentsFolder + "/" + pkgName + "/Mock" + simpleName + ".js";

        Ode.getInstance().getProjectService().load2(projectId, fileId,
                new OdeAsyncCallback<ChecksumedLoadFile>(MESSAGES.loadError()) {
                    @Override
                    public void onSuccess(ChecksumedLoadFile result) {
                        try {
                            OdeLog.log("<MSM:load:110> loading success for type = " + type + " project = " + projectId);

                            String mockJsFile = result.getContent();
                            mockJsFile = SDK_CODE_START + mockJsFile + SDK_CODE_END;

                            IFrameElement iFrameElement = Document.get().createIFrameElement();
                            iFrameElement.setId("Mock_for_" + type);
                            iFrameElement.setAttribute("sandbox", "allow-scripts");
                            iFrameElement.setInnerHTML(mockJsFile); // append script element inside
                            Document.get().getBody().appendChild(iFrameElement);

                            loadedMocks.add(type);
                        } catch (ChecksumedFileException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    public void upgrade(String type) {
        if (loadedMocks.contains(type)) {
            unload(type);
            load(type);
        }
    }

    public void unload(String type) {
        OdeLog.log("<MSM:unload:132> unloading type = " + type + " project = " + projectId);
        loadedMocks.remove(type);

        Element iframeElement = Document.get().getElementById("Mock_for_" + type);
        Document.get().getBody().removeChild(iframeElement);

        String simpleName = type.substring(type.lastIndexOf('.') + 1); // todo: See MCR#register
        MockComponentRegistry.unregister(simpleName);
        cleanup(simpleName); // todo: check if it's required
    }

    private void unloadAll() {
        OdeLog.log("<MSM:unloadAll:140> unloading all... project = " + projectId);
        for (String type : loadedMocks) {
            unload(type);
        }
    }

    private static native void cleanup(String name)/*-{
        // fixme: it doesn't delete the Mock class!!
        console.log("<MSM:cleanup:159>", $wnd["Mock" + name]);
        delete $wnd["Mock" + name];
        console.log("<MSM:cleanup:161>", $wnd["Mock" + name]);
    }-*/;

    private native void initIframeListener() /*-{
        $wnd.addEventListener('message', function (event) {
            console.log("<MSM:iframeListener:169>", event.source.id);
            let sourceIframeId = event.source.id
            let { action, args, type, uuid } = event.data
            [@MockScriptsManager::INSTANCE].@MockScriptsManager::messageInterpreter(*)(sourceIframeId, action, args, type, uuid)
        })
    }-*/;

    private native void removeIframeListener() /*-{
        $wnd.removeEventListener('message')
    }-*/;

    public native void postMessage(String action, String[] args, String type, String uuid) /*-{
        var iframeEl = $doc.getElementById('Mock_for_' + type)
        var iframeWindow = iframeEl.contentWindow
        var msg = JSON.stringify({
            action,
            args,
            type,
            uuid
        })
        iframeWindow.postMessage(msg, '*') // @TODO: figure out target origin
    }-*/;

    public void messageInterpreter(String sourceIframeId, String action, String[] args, String type, String uuid) {
        switch (action) {
            case "MCR.register": // @TODO Look into creating this an enum
                MockVceManager.create(type, sourceIframeId);
                break;
            case "update":
                HTMLPanel $el = new HTMLPanel(args[0]);
                break;
        }
    }

    //// ComponentDatabaseChangeListener

    @Override
    public void onComponentTypeAdded(List<String> componentTypes) {
        SimpleComponentDatabase scd = SimpleComponentDatabase.getInstance(this.projectId);
        OdeLog.log("<MSM:onCompAdded:156> components added: project = " + projectId);
        for (String componentType : componentTypes) {
            String fqcn = scd.getComponentType(componentType); // Ensure [componentType] is a FQCN
            OdeLog.log("<MSM:onCompAdded:159> fqcn = " + fqcn);
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
        OdeLog.log("<MSM:onCompRemoved:176> components removed: project = " + projectId);
        for (String fqcn : componentTypes.values()) {
            OdeLog.log("<MSM:onCompRemoved:178> fqcn = " + fqcn);
            unload(fqcn);
        }
    }

    @Override
    public void onResetDatabase() {
        unloadAll();
    }

    //// ProjectChangeListener

    @Override
    public void onProjectLoaded(Project project) {
        // todo: figure out when this is called
        OdeLog.log("<MSM:onProjectLoaded:192> " + "loadedProject = " + project.getProjectId() + " project = " + projectId);
        // init only if the project is different
//        if (project.getProjectId() != projectId) {
//            YaProjectEditor projectEditor = (YaProjectEditor) Ode.getInstance().getEditorManager()
//                    .getOpenProjectEditor(project.getProjectId());
//            MockScriptsManager.init(project.getProjectId(), projectEditor, project.getRootNode());
//        }
    }

    @Override
    public void onProjectNodeAdded(Project project, ProjectNode node) {
        // called when a form/block editor (new screen) is added
        OdeLog.log("<MSM:onProjectNodeAdded:203> " + "loadedProject = " + project.getProjectId() + " node = " + node.getProjectId() + " project = " + projectId);
        // todo: handle project change
//        if (project.getProjectId() == projectId) { // ensure this is the same project
//            if (node.getProjectId() != projectId) { // res
//                destroy();
//                MockComponentRegistry.reset();
//            }
//        }
    }

    @Override
    public void onProjectNodeRemoved(Project project, ProjectNode node) {
        // called when a form/block editor (new screen) is removed
        OdeLog.log("<MSM:onProjectNodeRemoved:215> " + "loadedProject = " + project.getProjectId() + " node = " + node.getProjectId() + " project = " + projectId);
//        destroy();
//        MockComponentRegistry.reset();
    }
}
