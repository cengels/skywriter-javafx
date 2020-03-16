package com.cengels.skywriter.style

import com.cengels.skywriter.SkyWriterApp
import com.cengels.skywriter.util.allDescendants
import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeLineCap
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
        val skyButton by cssclass()
        val buttonBox by cssclass()
        val trackBackground by cssclass()

        val titleBarHeight = 30.px
        val titleBarButtonWidth = 40.px
        val cornerRadius = 16.px
        const val titleBarIconScale = 0.9
    }

    init {
        themedView {
            backgroundColor += Colors.Background.REGULAR
            backgroundRadius += box(cornerRadius)
            borderRadius += box(cornerRadius)

            s(spacedLabelText, skyText) {
                font = SkyWriterApp.applicationFont
                fill = Colors.Font.REGULAR
                textFill = Colors.Font.REGULAR
            }

            button {
                +selectable
            }

            titleBar {
                backgroundColor += Colors.Primary.REGULAR
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

                    backgroundColor += Color.TRANSPARENT

                    and(hover) {
                        backgroundColor += Colors.Background.REGULAR
                    }

                    svg {
                        scaleX = titleBarIconScale
                        scaleY = titleBarIconScale
                    }
                }

                applicationIcon {
                    fill = Colors.Font.REGULAR
                }
            }

            GeneralStylesheet.plainButton {
                s(svg, svg.allDescendants) {
                    stroke = Colors.Font.REGULAR
                    strokeWidth = 2.px
                    strokeLineCap = StrokeLineCap.ROUND
                }
            }

            s(buttonBox, buttonBar) {
                padding = box(6.px)
            }

            buttonBar {
                minHeight = 40.px
            }

            s(buttonBox contains button, buttonBar contains button, skyButton) {
                minWidth = 7.5.em
                minHeight = 0.9.em
                borderWidth += CssBox(0.px, 0.px, 0.px, 0.px)
                textFill = Colors.Font.REGULAR
                font = SkyWriterApp.applicationFont
                fontSize = 10.pt
                padding = box(3.px)
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
                    backgroundColor += Colors.Background.LOW
                    +selectable
                }
            }

            s(form contains label, form contains text, textField, comboBoxBase contains label) {
                font = SkyWriterApp.applicationFont
                fill = Colors.Font.LOW
                textFill = Colors.Font.LOW
            }

            textField {
            }

            comboBoxBase {
                +selectable
            }
        }
    }
}