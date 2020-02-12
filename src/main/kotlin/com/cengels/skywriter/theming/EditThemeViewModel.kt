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
    val backgroundImageProperty = bind(Theme::backgroundImage)
    val backgroundImageSizingTypeProperty = bind(Theme::backgroundImageSizingType)
    val documentOpacityProperty = bind(Theme::documentOpacity)
    val documentWidthProperty = bind(Theme::documentWidth)
    val documentHeightProperty = bind(Theme::documentHeight)
    val paddingVerticalProperty = bind(Theme::paddingVertical)
    val paddingHorizontalProperty = bind(Theme::paddingHorizontal)
    val firstLineIndentProperty = bind(Theme::firstLineIndent)
    val lineHeightProperty = bind(Theme::lineHeight)
    private val rawFontColorProperty = bind(Theme::fontColor)
    private val rawBackgroundFillProperty = bind(Theme::backgroundFill)
    private val rawBackgroundDocumentProperty = bind(Theme::backgroundDocument)
    val textAlignmentProperty = bind(Theme::textAlignment)
    val fontColorProperty = ConverterProperty(rawFontColorProperty, ColorConverter)
    var fontColor by fontColorProperty
    var backgroundFillProperty = ConverterProperty(rawBackgroundFillProperty, ColorConverter)
    var backgroundFill by fontColorProperty
    var backgroundDocumentProperty = ConverterProperty(rawBackgroundDocumentProperty, ColorConverter)
    var backgroundDocument by fontColorProperty
}