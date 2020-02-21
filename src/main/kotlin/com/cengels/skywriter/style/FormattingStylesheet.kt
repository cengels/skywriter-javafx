package com.cengels.skywriter.style

import com.cengels.skywriter.enum.Heading
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*

class FormattingStylesheet : Stylesheet() {
    companion object {
        val bold by cssclass()
        val italic by cssclass()
        val strikethrough by cssclass()
        val headings: Map<Heading, String> = mapOf(
            Pair(Heading.H1, "h1"),
            Pair(Heading.H2, "h2"),
            Pair(Heading.H3, "h3"),
            Pair(Heading.H4, "h4"),
            Pair(Heading.H5, "h5"),
            Pair(Heading.H6, "h6")
        )
        val paragraphText by cssclass()
        val text by cssclass()
    }

    init {
        bold {
            fontWeight = FontWeight.BOLD
        }

        italic {
            fontStyle = FontPosture.ITALIC
        }

        strikethrough {
            strikethrough = true
        }
    }
}