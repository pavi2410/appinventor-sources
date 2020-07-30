package com.srlane;

import android.view.View;
import android.widget.TextView;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;

@DesignerComponent(version = 1,
    description = "Simple Label extension - 2",
    category = ComponentCategory.EXTENSION,
    iconName = "images/extension.png",
    hasCustomMock = true)
@SimpleObject(external = true)
public class SimpleLabel2 extends AndroidViewComponent {

  private TextView tv;

  public SimpleLabel2(ComponentContainer container) {
    super(container.$form());

    tv = new TextView(container.$context());

    container.$add(this);
  }

  @Override
  public View getView() {
    return tv;
  }

  @DesignerProperty
  @SimpleProperty
  public void Text(String text) {
    tv.setText(text);
  }
}