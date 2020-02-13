package com.cengels.skywriter.theming

import com.cengels.skywriter.util.ColorConverter
import com.cengels.skywriter.util.ConverterProperty
import tornadofx.*

class EditThemeViewModel(theme: Theme) : ItemViewModel<Theme>(theme) {
    val nameProperty = bind(Theme::name)

    val fontSizeProperty = bind(Theme::fontSize)
    val fontSize: Double by fontSizeProperty
    val fontFamilyProperty = bind(Theme::fontFamily)
    val fontFamily by fontFamilyProperty
    private val rawFontColorProperty = bind(Theme::fontColor)
    val fontColorProperty = ConverterProperty(rawFontColorProperty, ColorConverter)
    var fontColor by fontColorProperty
    val firstLineIndentProperty = bind(Theme::firstLineIndent)
    val firstLineIndent by firstLineIndentProperty
    val lineHeightProperty = bind(Theme::lineHeight)
    val lineHeight by lineHeightProperty
    val textAlignmentProperty = bind(Theme::textAlignment)
    val textAlignment by textAlignmentProperty

    val backgroundImageProperty = bind(Theme::backgroundImage)
    val backgroundImage by backgroundImageProperty
    val backgroundImageSizingTypeProperty = bind(Theme::backgroundImageSizingType)
    val backgroundImageSizingType by backgroundImageSizingTypeProperty
    private val rawBackgroundFillProperty = bind(Theme::backgroundFill)
    var backgroundFillProperty = ConverterProperty(rawBackgroundFillProperty, ColorConverter)
    var backgroundFill by fontColorProperty
    private val rawBackgroundDocumentProperty = bind(Theme::backgroundDocument)
    var backgroundDocumentProperty = ConverterProperty(rawBackgroundDocumentProperty, ColorConverter)
    var backgroundDocument by fontColorProperty

    val documentWidthProperty = bind(Theme::documentWidth)
    val documentWidth by documentWidthProperty
    val documentHeightProperty = bind(Theme::documentHeight)
    val documentHeight by documentHeightProperty
    val paddingHorizontalProperty = bind(Theme::paddingHorizontal)
    val paddingHorizontal by paddingHorizontalProperty
    val paddingVerticalProperty = bind(Theme::paddingVertical)
    val paddingVertical by paddingVerticalProperty
}