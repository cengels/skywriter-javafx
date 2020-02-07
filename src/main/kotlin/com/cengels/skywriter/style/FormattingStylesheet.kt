package com.cengels.skywriter.style

import com.cengels.skywriter.enum.Heading
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.*

class FormattingStylesheet : Stylesheet() {
    companion object {
        val h1 by cssclass()
        val h2 by cssclass()
        val h3 by cssclass()
        val h4 by cssclass()
        val h5 by cssclass()
        val h6 by cssclass()
        val centered by cssclass()
        val justified by cssclass()
        val rightaligned by cssclass()
        val bold by cssclass()
        val italic by cssclass()
        val alignments: Map<TextAlignment, String> = mapOf(
            Pair(TextAlignment.RIGHT, "rightaligned"),
            Pair(TextAlignment.CENTER, "centered"),
            Pair(TextAlignment.JUSTIFY, "justified")
        )
        val headings: Map<Heading, String> = mapOf(
            Pair(Heading.H1, "h1"),
            Pair(Heading.H2, "h2"),
            Pair(Heading.H3, "h3"),
            Pair(Heading.H4, "h4"),
            Pair(Heading.H5, "h5"),
            Pair(Heading.H6, "h6")
        )
    }

    init {
        h1 {
            fontSize = 22.pt
            fontWeight = FontWeight.BOLD
        }

        h2 {
            fontSize = 20.pt
            fontWeight = FontWeight.BOLD
        }

        h3 {
            fontSize = 18.pt
            fontWeight = FontWeight.BOLD
        }

        h4 {
            fontSize = 16.pt
            fontWeight = FontWeight.BOLD
        }

        h5 {
            fontSize = 14.pt
            fontWeight = FontWeight.BOLD
        }

        h6 {
            fontSize = 12.pt
            fontWeight = FontWeight.BOLD
        }

        centered {
            textAlignment = TextAlignment.CENTER
        }

        justified {
            textAlignment = TextAlignment.JUSTIFY
        }

        rightaligned {
            textAlignment = TextAlignment.RIGHT
        }

        bold {
            fontWeight = FontWeight.BOLD
        }

        italic {
            fontStyle = FontPosture.ITALIC
        }
    }
}