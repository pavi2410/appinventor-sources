// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime

import com.google.appinventor.components.annotations.DesignerComponent
import com.google.appinventor.components.annotations.DesignerProperty
import com.google.appinventor.components.annotations.PropertyCategory
import com.google.appinventor.components.annotations.SimpleObject
import com.google.appinventor.components.annotations.SimpleProperty
import com.google.appinventor.components.common.ComponentCategory
import com.google.appinventor.components.common.PropertyTypeConstants
import com.google.appinventor.components.common.YaVersion
import com.google.appinventor.components.runtime.util.TextViewUtil

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Label containing a text string.
 *
 */
@DesignerComponent(version = YaVersion.LABEL_COMPONENT_VERSION,
				description = "A Label displays a piece of text, which is "
							+ "specified through the <code>Text</code> property. Other properties, "
							+ "all of which can be set in the Designer or Blocks Editor, control "
							+ "the appearance and placement of the text.",
				category = ComponentCategory.USERINTERFACE)
@SimpleObject
class Label (container: ComponentContainer) : AndroidViewComponent(container) {

	// default margin in density-independent pixels. This must be
	// computed using the view
	private val defaultLabelMarginInDp = 0
	
	private val view:TextView
	
	private val linearLayoutParams:LinearLayout.LayoutParams
	
	// Backing for text alignment
	private val textAlignment:Int = 0
	
	// Backing for background color
	private val backgroundColor:Int = 0
	
	// Backing for font typeface
	private val fontTypeface:Int = 0
	
	// Backing for font bold
	private val bold:Boolean = false
	
	// Backing for font italic
	private val italic:Boolean = false
	
	// Whether or not the label should have a margin
	private val hasMargins:Boolean = false
	
	// Backing for text color
	private val textColor:Int = 0
	
	// Label Format
	private val htmlFormat:Boolean = false
	
	init {
		view = TextView(container.$context())
		
		// Adds the component to its designated container
		container.$add(this)
		
		// Get the layout parameters to use in setting margins (and potentially
		// other things.
		// There will be a bug if the label view does not have linear layout params.
		// TODO(hal): Generalize this for other types of layouts
		val lp = view.getLayoutParams()
		// The following instanceof check will fail if we have not previously
		// added the label to the container (Why?)
		if (lp is LinearLayout.LayoutParams) {
			linearLayoutParams = lp as LinearLayout.LayoutParams
			defaultLabelMarginInDp = dpToPx(view, DEFAULT_LABEL_MARGIN)
		} else {
			defaultLabelMarginInDp = 0
			linearLayoutParams = null
			Log.e("Label", "Error: The label's view does not have linear layout parameters")
			throw RuntimeException().printStackTrace()
		}
		
		// Default property values
		TextAlignment(Component.ALIGNMENT_NORMAL)
		BackgroundColor(Component.COLOR_NONE)
		fontTypeface = Component.TYPEFACE_DEFAULT
		TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic)
		FontSize(Component.FONT_DEFAULT_SIZE)
		Text("")
		TextColor(Component.COLOR_DEFAULT)
		HTMLFormat(false)
		HasMargins(true)
	}
	
	override fun getView() : View {
		return view
	}
	
	/**
	* Returns the alignment of the label's text: center, normal
	* (e.g., left-justified if text is written left to right), or
	* opposite (e.g., right-justified if text is written left to right).
	*
	* @return one of {@link Component#ALIGNMENT_NORMAL},
	* {@link Component#ALIGNMENT_CENTER} or
	* {@link Component#ALIGNMENT_OPPOSITE}
	*/
	@SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = false)
	fun TextAlignment() : Int {
		return textAlignment
	}
	
	/**
	* Specifies the alignment of the label's text: center, normal
	* (e.g., left-justified if text is written left to right), or
	* opposite (e.g., right-justified if text is written left to right).
	*
	* @param alignment one of {@link Component#ALIGNMENT_NORMAL},
	* {@link Component#ALIGNMENT_CENTER} or
	* {@link Component#ALIGNMENT_OPPOSITE}
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTALIGNMENT, defaultValue = Component.ALIGNMENT_NORMAL + "")
	@SimpleProperty(userVisible = false)
	fun TextAlignment(alignment: Int) {
		this.textAlignment = alignment
		TextViewUtil.setAlignment(view, alignment, false)
	}
	
	/**
	* Returns the label's background color as an alpha-red-green-blue
	* integer.
	*
	* @return background RGB color with alpha
	*/
	@SimpleProperty(category = PropertyCategory.APPEARANCE)
		fun BackgroundColor() : Int {
		return backgroundColor
	}
	
	/**
	* Specifies the label's background color as an alpha-red-green-blue
	* integer.
	*
	* @param argb background RGB color with alpha
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_NONE)
	@SimpleProperty
	fun BackgroundColor(argb: Int) {
		backgroundColor = argb
		if (argb != Component.COLOR_DEFAULT) {
			TextViewUtil.setBackgroundColor(view, argb)
		} else {
			TextViewUtil.setBackgroundColor(view, Component.COLOR_NONE)
		}
	}
	
	/**
	* Returns true if the label's text should be bold.
	* If bold has been requested, this property will return true, even if the
	* font does not support bold.
	*
	* @return {@code true} indicates bold, {@code false} normal
	*/
	@SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = false)
	fun FontBold() : Boolean {
		return bold
	}
	
	/**
	* Specifies whether the label's text should be bold.
	* Some fonts do not support bold.
	*
	* @param bold {@code true} indicates bold, {@code false} normal
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
	@SimpleProperty(userVisible = false)
	fun FontBold(bold: Boolean) {
		this.bold = bold
		TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic)
	}
	
	/**
	* Returns true if the label's text should be italic.
	* If italic has been requested, this property will return true, even if the
	* font does not support italic.
	*
	* @return {@code true} indicates italic, {@code false} normal
	*/
	@SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = false)
		fun FontItalic() : Boolean {
		return italic
	}
	
	/**
	* Specifies whether the label's text should be italic.
	* Some fonts do not support italic.
	*
	* @param italic {@code true} indicates italic, {@code false} normal
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
	@SimpleProperty(userVisible = false)
	fun FontItalic(italic: Boolean) {
		this.italic = italic
		TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic)
	}
	
	/**
	* Returns true if the label should have margins.
	*
	* @return {@code true} indicates margins, {@code false} no margins
	*/
	@SimpleProperty(category = PropertyCategory.APPEARANCE,
					description = "Reports whether or not the label appears with margins. All four "
								+ "margins (left, right, top, bottom) are the same. This property has no effect "
								+ "in the designer, where labels are always shown with margins.",
					userVisible = true)
	fun HasMargins() : Boolean {
		return hasMargins
	}
	
	/**
	* Specifies whether the label should have margins.
	* This margin value is not well coordinated with the
	* designer, where the margins are defined for the arrangement, not just for individual
	* labels.
	*
	* @param hasMargins {@code true} indicates that there are margins, {@code false} no margins
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
	@SimpleProperty(userVisible = true)
	fun HasMargins(hasMargins: Boolean) {
		this.hasMargins = hasMargins
		setLabelMargins(hasMargins)
	}
	
	private fun setLabelMargins(hasMargins: Boolean) {
		val m = if (hasMargins) defaultLabelMarginInDp else 0
		linearLayoutParams.setMargins(m, m, m, m)
		view.invalidate()
	
	
	/**
	* Returns the label's text's font size, measured in sp(scale-independent pixels).
	*
	* @return font size in sp(scale-independent pixels).
	*/
	@SimpleProperty(category = PropertyCategory.APPEARANCE)
	fun FontSize() : Float {
		return TextViewUtil.getFontSize(view, container.$context())
	}
	
	/**
	* Specifies the label's text's font size, measured in sp(scale-independent pixels).
	*
	* @param size font size in sp (scale-independent pixels)
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT, defaultValue = Component.FONT_DEFAULT_SIZE + "")
	@SimpleProperty
	fun FontSize(size: Float) {
		TextViewUtil.setFontSize(view, size)
	}
	
	/**
	* Returns the label's text's font face as default, serif, sans
	* serif, or monospace.
	*
	* @return one of {@link Component#TYPEFACE_DEFAULT},
	* {@link Component#TYPEFACE_SERIF},
	* {@link Component#TYPEFACE_SANSSERIF} or
	* {@link Component#TYPEFACE_MONOSPACE}
	*/
	@SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = false)
	fun FontTypeface(): Int {
		return fontTypeface
	}
	
	/**
	* Specifies the label's text's font face as default, serif, sans
	* serif, or monospace.
	*
	* @param typeface one of {@link Component#TYPEFACE_DEFAULT},
	* {@link Component#TYPEFACE_SERIF},
	* {@link Component#TYPEFACE_SANSSERIF} or
	* {@link Component#TYPEFACE_MONOSPACE}
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE, defaultValue = Component.TYPEFACE_DEFAULT + "")
	@SimpleProperty(userVisible = false)
	fun FontTypeface(typeface: Int) {
		fontTypeface = typeface
		TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic)
	}
	
	/**
	* Returns the text displayed by the label.
	*
	* @return label caption
	*/
	@SimpleProperty(category = PropertyCategory.APPEARANCE)
	fun Text() : String {
		return TextViewUtil.getText(view)
	}
	
	/**
	* Specifies the text displayed by the label.
	*
	* @param text new caption for label
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
					defaultValue = "")
	@SimpleProperty
	fun Text(text: String) {
		if (htmlFormat) {
			TextViewUtil.setTextHTML(view, text)
		} else {
			TextViewUtil.setText(view, text)
		}
	}
	
	/**
	* Returns the label's text's format
	*
	* @return {@code true} indicates that the label format is html text
	* {@code false} lines that the label format is plain text
	*/
	@SimpleProperty(category = PropertyCategory.APPEARANCE,
					description = "If true, then this label will show html text else it "
								+ "will show plain text. Note: Not all HTML is supported.")
	fun HTMLFormat() : Boolean {
		return htmlFormat
	}
	
	/**
	* Specifies the label's text's format
	*
	* @return {@code true} indicates that the label format is html text
	* {@code false} lines that the label format is plain text
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
					defaultValue = "False")
	@SimpleProperty(userVisible = false)
	fun HTMLFormat(fmt: Boolean) {
		htmlFormat = fmt
		if (htmlFormat) {
			val txt = TextViewUtil.getText(view)
			TextViewUtil.setTextHTML(view, txt)
		} else {
			val txt = TextViewUtil.getText(view)
			TextViewUtil.setText(view, txt)
		}
	}
	
	/**
	* Returns the label's text color as an alpha-red-green-blue
	* integer.
	*
	* @return text RGB color with alpha
	*/
	@SimpleProperty(category = PropertyCategory.APPEARANCE)
	fun TextColor() : Int {
		return textColor
	}
	
	/**
	* Specifies the label's text color as an alpha-red-green-blue
	* integer.
	*
	* @param argb text RGB color with alpha
	*/
	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
					defaultValue = Component.DEFAULT_VALUE_COLOR_BLACK)
	@SimpleProperty
	fun TextColor(argb: Int) {
		textColor = argb
		if (argb != Component.COLOR_DEFAULT) {
			TextViewUtil.setTextColor(view, argb)
		} else {
			TextViewUtil.setTextColor(view, if (container.$form().isDarkTheme()) Component.COLOR_WHITE else Component.COLOR_BLACK)
		}
	}
	
	companion object {
		
		// default margin around a label in DPs
		// note that the spacing between adjacent labels will be twice this value
		// because each label has a margin
		private val DEFAULT_LABEL_MARGIN = 2
		
		// put this in the right file
		private fun dpToPx(view: View, dp: Int) : Int {
			val density = view.getContext().getResources().getDisplayMetrics().density
			return Math.round(dp.toFloat() * density)
		}
	}
}