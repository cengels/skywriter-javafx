package com.cengels.skywriter.util

import java.awt.Color

object ColorConverter : Converter<Color, javafx.scene.paint.Color> {
    override fun convert(value: Color): javafx.scene.paint.Color {
        return javafx.scene.paint.Color(value.red / 255.0, value.green / 255.0, value.blue / 255.0, value.alpha / 255.0)
    }

    override fun convertBack(value: javafx.scene.paint.Color): Color {
        return java.awt.Color(value.red.toFloat(), value.green.toFloat(), value.blue.toFloat())
    }

}