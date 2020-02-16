package com.cengels.skywriter.style

import javafx.scene.effect.DropShadow
import javafx.scene.effect.Effect
import javafx.scene.paint.Color
import tornadofx.*

class GeneralStylesheet : Stylesheet() {
    companion object {
        val titleBar by cssclass()
        val themedView by cssclass()
        val selected by cssclass()
        val dataGridCell by cssclass("datagrid-cell")
    }

    init {
        titleBar {
            fontSize = 12.pt
            maxWidth = Double.POSITIVE_INFINITY.px
            minHeight = 40.px
            maxHeight = 40.px
        }

        dataGridCell {
            and(selected) {
                backgroundColor = MultiValue(arrayOf(Color.CORNFLOWERBLUE))
            }

            backgroundColor = MultiValue(arrayOf(Color.rgb(244, 244, 244)))
        }

        themedView {
            backgroundColor += Color.ORANGE
        }
    }
}