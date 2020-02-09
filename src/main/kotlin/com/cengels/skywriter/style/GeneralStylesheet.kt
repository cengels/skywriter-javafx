package com.cengels.skywriter.style

import javafx.scene.paint.Color
import tornadofx.*

class GeneralStylesheet : Stylesheet() {
    companion object {
        val titleBar by cssclass()
        val themedView by cssclass()
    }

    init {
        titleBar {
            fontSize = 12.pt
            maxWidth = Double.POSITIVE_INFINITY.px
            minHeight = 40.px
            maxHeight = 40.px
        }

        themedView {
            backgroundColor += Color.ORANGE
        }
    }
}