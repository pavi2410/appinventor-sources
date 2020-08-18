package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.MockScriptsManager;
import com.google.appinventor.client.editor.simple.MockVceManager;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(namespace = JsPackage.GLOBAL)
public class MockVisibleExtension extends MockVisibleComponent {

    public MockVisibleExtension(SimpleEditor editor, String type) {
        super(editor, type, new Image(images.extension()));
    }

    public void initComponent(Element component) {
        HTMLPanel componentElement = HTMLPanel.wrap(component);
        componentElement.setStylePrimaryName("ode-SimpleMockComponent");
        super.initComponent(componentElement);
    }

    public void initComponent(SafeHtml html) {
        HTMLPanel componentElement = new HTMLPanel(html);
        componentElement.setStylePrimaryName("ode-SimpleMockComponent");
        super.initComponent(componentElement);
    }

    @Override
    public void onPropertyChange(String propertyName, String newValue) {
        super.onPropertyChange(propertyName, newValue);


    }

    public String getName() {
        return super.getName();
    }
}
