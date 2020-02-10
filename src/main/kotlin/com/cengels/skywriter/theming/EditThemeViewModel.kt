package com.cengels.skywriter.theming

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Paint
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
    val fontColorProperty = bind(Theme::fontColor)
    val backgroundFillProperty = bind(Theme::backgroundFill)
    val backgroundDocumentProperty = bind(Theme::backgroundDocument)
    val textAlignmentProperty = bind(Theme::textAlignment)
}