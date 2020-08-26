package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.MockScriptsManager;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(namespace = JsPackage.GLOBAL)
public class MockVisibleExtension extends MockVisibleComponent {

    private final String mockUuid; // this is different from getUuid()

    public MockVisibleExtension(SimpleEditor editor, String type, String mockUuid) {
        super(editor, type, new Image(images.extension()));
        this.mockUuid = mockUuid;
    }

    public void initComponent(SafeHtml html) {
        HTMLPanel componentElement = new HTMLPanel(html);
        componentElement.setStylePrimaryName("ode-SimpleMockComponent");
        super.initComponent(componentElement);
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);

        MockScriptsManager.INSTANCE.postMessage("onPropertyChange", new String[]{propertyName, newValue}, getType(), mockUuid);
    }

    public String getName() {
        return super.getName();
    }
}
