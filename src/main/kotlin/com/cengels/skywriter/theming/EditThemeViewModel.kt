package com.cengels.skywriter.theming

import com.cengels.skywriter.util.convert.ColorConverter
import com.cengels.skywriter.util.convert.ConverterProperty
import tornadofx.*
import java.awt.Color

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

    private val rawFontShadowColorProperty = bind(Theme::fontShadowColor)
    val fontShadowColorProperty = ConverterProperty(rawFontShadowColorProperty, ColorConverter)
    var fontShadowColor by fontShadowColorProperty
    val fontShadowRadiusProperty = bind(Theme::fontShadowRadius)
    var fontShadowRadius by fontShadowRadiusProperty
    val fontShadowSpreadProperty = bind(Theme::fontShadowSpread)
    var fontShadowSpread by fontShadowSpreadProperty
    val fontShadowOffsetXProperty = bind(Theme::fontShadowOffsetX)
    var fontShadowOffsetX by fontShadowOffsetXProperty
    val fontShadowOffsetYProperty = bind(Theme::fontShadowOffsetY)
    var fontShadowOffsetY by fontShadowOffsetYProperty
}