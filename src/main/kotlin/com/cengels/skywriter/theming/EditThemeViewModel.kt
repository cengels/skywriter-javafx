package com.cengels.skywriter.theming

import com.cengels.skywriter.util.ColorConverter
import com.cengels.skywriter.util.ConverterProperty
import tornadofx.*

class EditThemeViewModel(theme: Theme) : ItemViewModel<Theme>(theme) {
    val nameProperty = bind(Theme::name)
    var name by nameProperty

    val fontSizeProperty = bind(Theme::fontSize)
    var fontSize: Double by fontSizeProperty
    val fontFamilyProperty = bind(Theme::fontFamily)
    var fontFamily by fontFamilyProperty
    private val rawFontColorProperty = bind(Theme::fontColor)
    val fontColorProperty = ConverterProperty(rawFontColorProperty, ColorConverter)
    var fontColor by fontColorProperty
    val firstLineIndentProperty = bind(Theme::firstLineIndent)
    var firstLineIndent by firstLineIndentProperty
    val lineHeightProperty = bind(Theme::lineHeight)
    var lineHeight by lineHeightProperty
    val textAlignmentProperty = bind(Theme::textAlignment)
    var textAlignment by textAlignmentProperty

    private val rawWindowBackgroundProperty = bind(Theme::windowBackground)
    val windowBackgroundProperty = ConverterProperty(rawWindowBackgroundProperty, ColorConverter)
    var windowBackground by windowBackgroundProperty
    val backgroundImageProperty = bind(Theme::backgroundImage)
    var backgroundImage by backgroundImageProperty
    val backgroundImageSizingTypeProperty = bind(Theme::backgroundImageSizingType)
    var backgroundImageSizingType by backgroundImageSizingTypeProperty

    val documentWidthProperty = bind(Theme::documentWidth)
    var documentWidth by documentWidthProperty
    val documentHeightProperty = bind(Theme::documentHeight)
    var documentHeight by documentHeightProperty
    private val rawDocumentBackgroundProperty = bind(Theme::documentBackground)
    val documentBackgroundProperty = ConverterProperty(rawDocumentBackgroundProperty, ColorConverter)
    var documentBackground by documentBackgroundProperty
    val paddingHorizontalProperty = bind(Theme::paddingHorizontal)
    var paddingHorizontal by paddingHorizontalProperty
    val paddingVerticalProperty = bind(Theme::paddingVertical)
    var paddingVertical by paddingVerticalProperty
}