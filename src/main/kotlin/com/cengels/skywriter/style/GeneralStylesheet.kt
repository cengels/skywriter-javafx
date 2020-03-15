package com.cengels.skywriter.style

import javafx.scene.paint.Color
import javafx.scene.text.FontSmoothingType
import tornadofx.*

class GeneralStylesheet : Stylesheet() {
    companion object {
        val plainButton by cssclass()
    }

    init {
        plainButton {
            backgroundColor += Color.TRANSPARENT
            borderWidth += CssBox(0.px, 0.px, 0.px, 0.px)
        }

        s(scrollPane, scrollPane contains viewport) {
            backgroundColor += Color.TRANSPARENT
        }
    }
}