package com.cengels.skywriter.style

import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import tornadofx.*

class FormattingStylesheet : Stylesheet() {
    companion object {
        val h1 by cssclass()
        val h2 by cssclass()
        val h3 by cssclass()
        val bold by cssclass()
        val italic by cssclass()
    }

    init {
        h1 {
            fontSize = 20.pt
            fontWeight = FontWeight.BOLD
        }

        h2 {
            fontSize = 17.pt
            fontWeight = FontWeight.BOLD
        }

        h2 {
            fontSize = 14.pt
            fontWeight = FontWeight.BOLD
        }

        bold {
            fontWeight = FontWeight.BOLD
        }

        italic {
            fontStyle = FontPosture.ITALIC
        }
    }
}