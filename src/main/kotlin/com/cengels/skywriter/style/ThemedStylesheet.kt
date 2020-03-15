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
        val buttonBox by cssclass()
        val trackBackground by cssclass()
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

                +fontSmoothing(Colors.lighterFontColor)
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
                fill = Colors.lighterFontColor
            }
        }

        GeneralStylesheet.plainButton {
            cursor = Cursor.HAND

            s(svg, svg.allDescendants) {
                stroke = Colors.lighterFontColor
                strokeWidth = 2.px
                strokeLineCap = StrokeLineCap.ROUND
            }
        }

        s(buttonBox, buttonBar) {
            padding = box(6.px)

            button {
                minWidth = 7.5.em
                minHeight = 0.9.em
                borderWidth += CssBox(0.px, 0.px, 0.px, 0.px)
                backgroundColor += Colors.accentDark
                textFill = Colors.lightFontColor
                font = SkyWriterApp.applicationFont
                fontSize = 10.pt
                padding = box(3.px)

                and(hover) {
                    backgroundColor += Colors.accentSelected
                }
            }
        }

        scrollBar {
            backgroundColor += Color.TRANSPARENT
            minWidth = 20.px

            trackBackground {
                backgroundColor += Color.TRANSPARENT
            }

            s(incrementButton, decrementButton) {
                fill = Color.TRANSPARENT
                backgroundColor += Color.TRANSPARENT
            }

            thumb {
                backgroundColor += Colors.accentDark

                and(hover) {
                    backgroundColor += Colors.accentDarker
                }
            }
        }
    }
}