/**
 * ©Kodular 2017 - 2019 | FOR MAKEROID ONLY AND NOT PRIVATE USE!
 * @author mika@kodular.io (Mika)
 */

package com.google.appinventor.components.runtime;

import android.content.Context;
import android.content.res.ColorStateList;
import androidx.core.widget.CompoundButtonCompat;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.widget.CompoundButton;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;

@DesignerComponent(
        version = YaVersion.RADIOBUTTON_COMPONENT_VERSION,
        category = ComponentCategory.USERINTERFACE,
        description = "",
        helpUrl = "https://docs.kodular.io/components/user-interface/radio-button/")
@SimpleObject
public final class RadioButton extends AndroidViewComponent implements CompoundButton.OnCheckedChangeListener {

    private Context context;
    private final AppCompatRadioButton rButton;
    private int buttonColor = Component.COLOR_BLACK;
    private String rText;
    private int textColor = Component.COLOR_BLACK;
    private boolean bold = false;
    private boolean italic = false;
    private int fontTypeface = Component.TYPEFACE_DEFAULT;

    public RadioButton(ComponentContainer container) {
        super(container.$form());
        context = container.$context();
        rButton = new AppCompatRadioButton(context);
        rButton.setOnCheckedChangeListener(this);
        ViewUtil.setPaddingInDp(rButton, 4, 4, 4, 4);

        container.$add(this);

        TextViewUtil.setContext(context);
        TextViewUtil.setFontTypeface(rButton, fontTypeface, bold, italic);
        Checked(false);
        Enabled(true);
        RadioButtonColor(Component.COLOR_BLACK);
        TextColor(Component.COLOR_BLACK);
        TextSize(Component.FONT_DEFAULT_SIZE);
        Text("Radio Button Text");
    }

    @Override
    public AppCompatRadioButton getView() {
        return rButton;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Changed(isChecked);
    }

    @SimpleFunction(description = "Change the checked state of the view to the inverse of its current state")
    public void Toggle() {
        if (rButton.isChecked()) {
            rButton.setChecked(false);
        } else {
            rButton.setChecked(true);
        }
    }

    @SimpleEvent(description = "Event invoked when the radio button state has been changed.")
    public void Changed(boolean checked) {
        EventDispatcher.dispatchEvent(this, "Changed",checked);
    }

    @SimpleProperty(description = "Returns true if the radio button is checked, else false.")
    public boolean isChecked() {
        return rButton.isChecked();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "False")
    @SimpleProperty(description = "Set the radio button to checked or unchecked")
    public void Checked(boolean checked) {
        rButton.setChecked(checked);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "True")
    @SimpleProperty(description = "If set, user can touch the radio button.")
    public void Enabled(boolean enabled) {
        rButton.setEnabled(enabled);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
    @SimpleProperty(description = "Change the radio button component color.")
    public void RadioButtonColor(int radioButtonColor) {
        buttonColor = radioButtonColor;
        CompoundButtonCompat.setButtonTintList(rButton, ColorStateList.valueOf(radioButtonColor));
    }

    @SimpleProperty(description = "Return the radio button component color.")
    public int RadioButtonColor() {
        return buttonColor;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA,
            defaultValue = "Radio Button Text")
    @SimpleProperty(description = "Set here the radio button text.")
    public void Text(String text) {
        rText = text;
        rButton.setText(TextViewUtil.fromHtml(rText+"  "));
    }

    @SimpleProperty(description = "Return the radio button text.")
    public String Text() {
        return rText;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
            defaultValue = Component.FONT_DEFAULT_SIZE + "")
    @SimpleProperty(description = "The text size of the radio button.")
    public void TextSize(float sizeText) {
        TextViewUtil.setFontSize(rButton, sizeText);
    }

    @SimpleProperty(description = "Return the text size of the radio button.")
    public float TextSize() {
        return TextViewUtil.getFontSize(rButton, context);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
    @SimpleProperty(description = "Set the text color for the radio button.")
    public void TextColor(int color){
        textColor = color;
        rButton.setTextColor(color);
    }

    @SimpleProperty(description = "Returns the text color for the radio button.")
    public int TextColor() {
        return textColor;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE,
            userVisible = false)
    public boolean FontBold() {
        return bold;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "False")
    @SimpleProperty(userVisible = false)
    public void FontBold(boolean bold) {
        this.bold = bold;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE,
            userVisible = false)
    public boolean FontItalic() {
        return italic;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "False")
    @SimpleProperty(userVisible = false)
    public void FontItalic(boolean italic) {
        this.italic = italic;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE,
            userVisible = false)
    public int FontTypeface() {
        return fontTypeface;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE,
            defaultValue = Component.TYPEFACE_DEFAULT + "")
    @SimpleProperty(userVisible = false)
    public void FontTypeface(int typeface) {
        fontTypeface = typeface;
        TextViewUtil.setFontTypeface(rButton, fontTypeface, bold, italic);
    }

    @DesignerProperty(defaultValue = "",
            editorType = PropertyTypeConstants.PROPERTY_TYPE_FONT_ASSET,
            propertyType = PropertyPriorityConstants.PROPERTY_TYPE_ADVANCED)
    @SimpleProperty(description = "Set a custom font.")
    public void FontTypefaceImport(String path) {
        if (path != null) {
            TextViewUtil.setCustomFontTypeface(container.$form(), rButton, path, bold, italic);
        }
    }

}
