package com.cengels.skywriter.theming

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Color
import tornadofx.*

class EditThemeViewModel(theme: Theme) : ItemViewModel<Theme>(theme) {
    val nameProperty = bind(Theme::name)
    var name by nameProperty

    val fontSizeProperty = bind(Theme::fontSize)
    var fontSize: Double by fontSizeProperty
    val fontFamilyProperty = bind(Theme::fontFamily)
    var fontFamily by fontFamilyProperty
    val fontColorProperty: ObjectProperty<Color> = bind(Theme::fontColor)
    var fontColor by fontColorProperty
    val firstLineIndentProperty = bind(Theme::firstLineIndent)
    var firstLineIndent by firstLineIndentProperty
    val lineHeightProperty = bind(Theme::lineHeight)
    var lineHeight by lineHeightProperty
    val textAlignmentProperty = bind(Theme::textAlignment)
    var textAlignment by textAlignmentProperty

    val windowBackgroundProperty: ObjectProperty<Color> = bind(Theme::windowBackground)
    var windowBackground by windowBackgroundProperty
    val backgroundImageProperty = bind(Theme::backgroundImage)
    var backgroundImage by backgroundImageProperty
    val backgroundImageSizingTypeProperty = bind(Theme::backgroundImageSizingType)
    var backgroundImageSizingType by backgroundImageSizingTypeProperty

    val documentWidthProperty = bind(Theme::documentWidth)
    var documentWidth by documentWidthProperty
    val documentHeightProperty = bind(Theme::documentHeight)
    var documentHeight by documentHeightProperty
    val documentBackgroundProperty: ObjectProperty<Color> = bind(Theme::documentBackground)
    var documentBackground by documentBackgroundProperty
    val paddingHorizontalProperty = bind(Theme::paddingHorizontal)
    var paddingHorizontal by paddingHorizontalProperty
    val paddingVerticalProperty = bind(Theme::paddingVertical)
    var paddingVertical by paddingVerticalProperty

    val fontShadowColorProperty: ObjectProperty<Color> = bind(Theme::fontShadowColor)
    var fontShadowColor: Color by fontShadowColorProperty
    val fontShadowRadiusProperty = bind(Theme::fontShadowRadius)
    var fontShadowRadius by fontShadowRadiusProperty
    val fontShadowSpreadProperty = bind(Theme::fontShadowSpread)
    var fontShadowSpread by fontShadowSpreadProperty
    val fontShadowOffsetXProperty = bind(Theme::fontShadowOffsetX)
    var fontShadowOffsetX by fontShadowOffsetXProperty
    val fontShadowOffsetYProperty = bind(Theme::fontShadowOffsetY)
    var fontShadowOffsetY by fontShadowOffsetYProperty
}