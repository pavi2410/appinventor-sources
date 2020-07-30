class MockSimpleLabel2 extends MockVisibleExtension {
  static TYPE = "SimpleLabel2"

  constructor(editor) {
    super(editor, MockSimpleLabel2.TYPE)

    this.label = document.createElement("p")

    this.initComponent(this.label)
  }

  onCreateFromPalette() {
    this.changeProperty("Text", this.getName())
  }

  onPropertyChange(propertyName, newValue) {
    super.onPropertyChange(propertyName, newValue)

    switch (propertyName) {
      case "Text":
        this.label.textContent = newValue
        break
    }
  }

  static create(editor) {
    return new MockSimpleLabel2(editor)
  }
}

MockComponentRegistry.register(MockSimpleLabel2.TYPE, MockSimpleLabel2.create)
