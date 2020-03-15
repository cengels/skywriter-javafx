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
        val skyText by cssclass()
        val titleBarHeight = 30.px
        val titleBarButtonWidth = 40.px
        val cornerRadius = 16.px
        const val titleBarIconScale = 0.9
    }

    init {
        themedView {
            backgroundColor += Colors.accentBackground
            backgroundRadius += box(cornerRadius)
            borderRadius += box(cornerRadius)

            s(spacedLabelText, skyText) {
                font = SkyWriterApp.applicationFont

                +fontSmoothing(Colors.lightFontColor)
            }
        }

        titleBar {
            backgroundColor += Colors.accent
            minHeight = titleBarHeight
            maxHeight = titleBarHeight
            padding = box(0.px, 0.px, 0.px, 8.px)

            GeneralStylesheet.plainButton {
                padding = box(0.px)
                minWidth = titleBarButtonWidth
                maxWidth = titleBarButtonWidth
                // It is unclear why the -1 is necessary here, but it is.
                minHeight = titleBarHeight - 1
                maxHeight = titleBarHeight - 1

                and(hover) {
                    backgroundColor += Colors.accentDark
                }

                svg {
                    scaleX = titleBarIconScale
                    scaleY = titleBarIconScale
                }
            }

            applicationIcon {
                fill = Colors.lightFontColor
            }
        }

        GeneralStylesheet.plainButton {
            cursor = Cursor.HAND

            s(svg, svg.allDescendants) {
                stroke = Colors.lightFontColor
                strokeWidth = 2.px
                strokeLineCap = StrokeLineCap.ROUND
            }
        }
    }
}