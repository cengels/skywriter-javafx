package com.cengels.skywriter.style

import com.cengels.skywriter.enum.Heading
import com.cengels.skywriter.theming.Theme
import com.cengels.skywriter.util.shiftBy
import javafx.scene.paint.Paint
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import tornadofx.Stylesheet
import tornadofx.cssclass
import tornadofx.cssproperty
import tornadofx.pt

class FormattingStylesheet(theme: Theme) : Stylesheet() {
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

        val comment by cssclass()
        val searchHighlighting by cssclass()
        val h1 by cssclass()
        val h2 by cssclass()
        val h3 by cssclass()
        val h4 by cssclass()
        val h5 by cssclass()
        val h6 by cssclass()

        val borderStrokeColor by cssproperty<Paint>("-rtfx-border-stroke-color")
        val borderStrokeWidth by cssproperty<Int>("-rtfx-border-stroke-width")
    }

    init {
        val fontColorDesaturated = theme.fontColor.shiftBy(-0.25)

        bold {
            fontWeight = FontWeight.BOLD
        }

        italic {
            fontStyle = FontPosture.ITALIC
        }

        strikethrough {
            strikethrough = true
        }

        paragraphText {
            text and comment {
                fill = fontColorDesaturated
            }

            text and searchHighlighting {
                borderStrokeColor.value = fontColorDesaturated
                borderStrokeWidth.value = 2
            }
        }

        s(h1, h2, h3, h4, h5, h6) {
            fontWeight = FontWeight.BOLD
            textAlignment = TextAlignment.CENTER
        }

        h1 { fontSize = (theme.fontSize * 1.6).pt }
        h2 { fontSize = (theme.fontSize * 1.5).pt }
        h3 { fontSize = (theme.fontSize * 1.4).pt }
        h4 { fontSize = (theme.fontSize * 1.3).pt }
        h5 { fontSize = (theme.fontSize * 1.2).pt }
        h6 { fontSize = (theme.fontSize * 1.1).pt }
    }
}