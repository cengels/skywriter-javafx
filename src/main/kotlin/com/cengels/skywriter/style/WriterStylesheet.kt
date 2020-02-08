package com.cengels.skywriter.style

import javafx.scene.paint.Color
import tornadofx.*

class WriterStylesheet : Stylesheet() {
    companion object {
        val textArea by cssclass()
        val textAreaBackground by cssclass()
    }

    init {
        textArea {
            fontSize = 14.pt
            maxWidth = Double.POSITIVE_INFINITY.px
            padding = box(0.5.em, 1.5.em)
        }

        textAreaBackground {
            backgroundColor += Color.LIGHTGRAY
        }
    }
}