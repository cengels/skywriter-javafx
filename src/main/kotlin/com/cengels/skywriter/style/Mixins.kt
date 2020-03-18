package com.cengels.skywriter.style

import com.cengels.skywriter.util.shift
import javafx.scene.Cursor
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontSmoothingType
import tornadofx.Stylesheet
import tornadofx.Stylesheet.Companion.arrowButton
import tornadofx.Stylesheet.Companion.pressed
import tornadofx.Stylesheet.Companion.selectedClass
import tornadofx.Stylesheet.Companion.showing
import tornadofx.Stylesheet.Companion.text
import tornadofx.mixin

fun fontSmoothing(color: Color) = mixin {
    fill = color
    textFill = color
    fontSmoothingType = FontSmoothingType.GRAY
    // effect = DropShadow(BlurType.GAUSSIAN, color.shift(opacityBy = -0.4), 2.0, 0.0, 0.0, 0.0)
    effect = DropShadow(BlurType.GAUSSIAN, color.shift(opacityBy = -0.25), 0.1, 0.0, 0.0, 0.0)
}

fun selectable(base: Color = Colors.Background.LOW, hover: Color? = null, selected: Color? = null) = mixin {
    cursor = Cursor.HAND

    backgroundColor += base

    and(Stylesheet.hover) {
        backgroundColor += hover ?: when (base) {
            Color.TRANSPARENT -> Colors.Background.LOW
            else -> Colors.Background.HOVER
        }
    }

    and(selectedClass, pressed, showing) parent@ {
        backgroundColor += selected ?: when (base) {
            Color.TRANSPARENT -> Colors.Background.HOVER
            else -> Colors.Background.SELECTION
        }

        arrowButton {
            backgroundColor = this@parent.backgroundColor
        }
    }
}

fun textColor(color: Color) = mixin {
    textFill = color

    text {
        fill = color
    }
}