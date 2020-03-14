package com.cengels.skywriter.style

import javafx.geometry.Pos
import tornadofx.*
import tornadofx.Stylesheet.Companion.box

class ThemedStylesheet : Stylesheet() {
    companion object {
        val themedView by cssclass()
        val titleBar by cssclass()
        val accent = c("#584C8D")
        val lightFontColor = c("#f5f2fa")
        val titleBarHeight = 30.px
    }

    init {
        titleBar {
            backgroundColor += accent
            minHeight = titleBarHeight
            maxHeight = titleBarHeight
            padding = box(0.px, 0.px, 0.px, 15.px)

            label {
                textFill = lightFontColor
            }
        }
    }
}