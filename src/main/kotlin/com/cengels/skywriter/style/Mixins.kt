package com.cengels.skywriter.style

import com.cengels.skywriter.util.shift
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontSmoothingType
import tornadofx.mixin

fun fontSmoothing(color: Color) = mixin {
    fill = color
    textFill = color
    fontSmoothingType = FontSmoothingType.GRAY
    // effect = DropShadow(BlurType.GAUSSIAN, color.shift(opacityBy = -0.4), 2.0, 0.0, 0.0, 0.0)
    effect = DropShadow(BlurType.GAUSSIAN, color.shift(opacityBy = -0.25), 0.1, 0.0, 0.0, 0.0)
}