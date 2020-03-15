package com.cengels.skywriter.style

import com.cengels.skywriter.SkyWriterApp
import com.cengels.skywriter.util.allDescendants
import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeLineCap
import javafx.scene.text.FontSmoothingType
import tornadofx.*

class ThemedStylesheet : Stylesheet() {
    companion object {
        val themedView by cssclass()
        val titleBar by cssclass()
        val applicationIcon by cssclass()
        val svg by cssclass()
        val spacedLabel by cssclass()
        val spacedLabelText by cssclass()
        val accent = c("#584C8D")
        val accentDark = c("#483e75")
        val accentDarker = c("#231e38")
        val lightFontColor = c("#d6d3db")
        val titleBarHeight = 30.px
        val titleBarButtonWidth = 40.px
        val cornerRadius = 16.px
        const val titleBarIconScale = 0.9
    }

    init {
        themedView {
            backgroundRadius += box(cornerRadius)
            borderRadius += box(cornerRadius)
        }

        titleBar {
            backgroundColor += accent
            minHeight = titleBarHeight
            maxHeight = titleBarHeight
            padding = box(0.px, 0.px, 0.px, 8.px)

            spacedLabelText {
                font = SkyWriterApp.applicationFont

                +fontSmoothing(lightFontColor)
            }

            GeneralStylesheet.plainButton {
                padding = box(0.px)
                minWidth = titleBarButtonWidth
                maxWidth = titleBarButtonWidth
                // It is unclear why the -1 is necessary here, but it is.
                minHeight = titleBarHeight - 1
                maxHeight = titleBarHeight - 1

                and(hover) {
                    backgroundColor += accentDark
                }

                svg {
                    scaleX = titleBarIconScale
                    scaleY = titleBarIconScale
                }
            }

            applicationIcon {
                fill = lightFontColor
            }
        }

        GeneralStylesheet.plainButton {
            cursor = Cursor.HAND

            s(svg, svg.allDescendants) {
                stroke = lightFontColor
                strokeWidth = 2.px
                strokeLineCap = StrokeLineCap.ROUND
            }
        }
    }
}