package com.cengels.skywriter.style

import com.cengels.skywriter.util.shift
import javafx.scene.Cursor
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontSmoothingType
import tornadofx.Stylesheet.Companion.hover
import tornadofx.Stylesheet.Companion.pressed
import tornadofx.Stylesheet.Companion.selectedClass
import tornadofx.mixin

fun fontSmoothing(color: Color) = mixin {
    fill = color
    textFill = color
    fontSmoothingType = FontSmoothingType.GRAY
    // effect = DropShadow(BlurType.GAUSSIAN, color.shift(opacityBy = -0.4), 2.0, 0.0, 0.0, 0.0)
    effect = DropShadow(BlurType.GAUSSIAN, color.shift(opacityBy = -0.25), 0.1, 0.0, 0.0, 0.0)
}

fun selectable(base: Color = Colors.Background.LOW) = mixin {
    cursor = Cursor.HAND

    backgroundColor += base

    and(hover) {
        backgroundColor += when (base) {
            Color.TRANSPARENT -> Colors.Background.LOW
            else -> Colors.Background.HOVER
        }
    }

    and(selectedClass, pressed) {
        backgroundColor += when (base) {
            Color.TRANSPARENT -> Colors.Background.HOVER
            else -> Colors.Background.SELECTION
        }
    }
}