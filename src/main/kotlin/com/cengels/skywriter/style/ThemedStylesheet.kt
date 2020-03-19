package com.cengels.skywriter.style

import com.cengels.skywriter.SkyWriterApp
import com.cengels.skywriter.util.allDescendants
import com.cengels.skywriter.util.height
import com.cengels.skywriter.util.width
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeLineCap
import tornadofx.*

class ThemedStylesheet : Stylesheet() {
    companion object {
        val themedView by cssclass()
        val themedViewContainer by cssclass()
        val titleBar by cssclass()
        val applicationIcon by cssclass()
        val spacedLabel by cssclass()
        val spacedLabelText by cssclass()
        val skyText by cssclass()
        val skyButton by cssclass()
        val buttonBox by cssclass()
        val trackBackground by cssclass()
        val primary by cssclass()
        val secondary by cssclass()
        val svgButton by cssclass()
        val svgMaximize by cssclass()

        val titleBarHeight = 30.px
        val titleBarButtonWidth = 40.px
        val cornerRadius = 16.px
        val svgIconSize = 21.px
        const val shadowRadius = 10.0
        val shadowColor = c(0.05, 0.05, 0.05, 0.3)
    }

    init {
        themedView {
            backgroundColor += Color.TRANSPARENT
            backgroundRadius += box(cornerRadius)
            borderRadius += box(cornerRadius)

            themedViewContainer {
                backgroundColor += Colors.Background.REGULAR
                backgroundRadius += box(cornerRadius)
                borderRadius += box(cornerRadius)
            }

            backgroundInsets += box(shadowRadius.px)
            padding = box(shadowRadius.px)
            effect = DropShadow(shadowRadius, 2.5, 2.5, shadowColor)

            s(form contains label, form contains text, textField) {
                font = SkyWriterApp.applicationFont
                fontSize = 9.5.pt
            }

            s(spacedLabelText, skyText) {
                fill = Colors.Font.REGULAR
                textFill = Colors.Font.REGULAR
            }

            titleBar {
                backgroundColor += Colors.Background.HIGH
                minHeight = titleBarHeight
                maxHeight = titleBarHeight
                padding = box(0.px, 0.px, 0.px, 8.px)

                svgButton {
                    padding = box(0.px)
                    minWidth = titleBarButtonWidth
                    maxWidth = titleBarButtonWidth
                    // It is unclear why the -1 is necessary here, but it is.
                    minHeight = titleBarHeight - 1
                    maxHeight = titleBarHeight - 1

                    +selectable(Color.TRANSPARENT)

                    and(hover) {
                        backgroundColor += Colors.Background.REGULAR
                    }

                    s(GeneralStylesheet.svg, GeneralStylesheet.svg.allDescendants) {
                        stroke = Colors.Font.DEEP
                    }
                }

                applicationIcon {
                    star {
                        fill = Colors.Font.REGULAR
                    }
                }
            }

            s(buttonBox, buttonBar) {
                padding = box(6.px)

                svgButton {
                    padding = box(8.px)
                    backgroundRadius += box(50.percent)
                    borderRadius += box(50.percent)

                    GeneralStylesheet.svg {
                        height = svgIconSize
                        width = svgIconSize
                    }
                }
            }

            buttonBar {
                minHeight = 40.px
            }

            s(buttonBar contains button, skyButton) {
                minWidth = 7.5.em
                minHeight = 0.9.em
                borderWidth += CssBox(0.px, 0.px, 0.px, 0.px)
                textFill = Colors.Font.REGULAR
                padding = box(3.px)
            }

            scrollBar {
                backgroundColor += Color.TRANSPARENT
                prefWidth = 14.px
                prefHeight = 14.px

                trackBackground {
                    backgroundColor += Color.TRANSPARENT
                }

                s(incrementArrow, decrementArrow) {
                    // Can't apply this style to incrementButton, decrementButton
                    // because the VirtualizedScrollPane uses these buttons to calculate its own size
                    // meaning without them it will always be invisible.
                    height = 0.px
                    width = 0.px
                    fill = Color.TRANSPARENT
                    backgroundColor += Color.TRANSPARENT
                }

                thumb {
                    +selectable()
                }
            }

            form {
                s(button, comboBoxBase, textField) {
                    height = 32.px
                }

                label {
                    and(legend) {
                        +textColor(Colors.Font.REGULAR)
                    }

                    +textColor(Colors.Font.DEEP)
                }

                button {
                    +textColor(Colors.Font.LOW)
                }
            }

            textField {
                backgroundColor += Colors.Background.LOW
                +textColor(Colors.Font.LOW)

                and(focused) {
                    backgroundColor += Colors.Background.HOVER
                }
            }

            comboBoxBase {
                +selectable(Colors.Background.LOW)
                +textColor(Colors.Font.LOW)

                arrowButton {
                    padding = box(0.px, 9.px)
                    // backgroundColor += c("#382f5c")
                    backgroundColor += Colors.Background.HOVER
                }
            }

            colorPicker {
                minWidth = 110.px
            }

            button {
                +selectable()

                and(secondary) {
                    textFill = Colors.Font.LOW
                }
            }

            svgButton {
                +selectable(Color.TRANSPARENT)
            }
        }

        svgButton {
            +selectable(Color.TRANSPARENT)

            s(GeneralStylesheet.svg, GeneralStylesheet.svg.allDescendants) {
                fill = Color.TRANSPARENT
                stroke = Colors.Font.LOW
                strokeWidth = 2.px
                strokeLineCap = StrokeLineCap.ROUND
            }

            s(svgMaximize, svgMaximize.allDescendants) {
                strokeWidth = 2.5.px
            }
        }
    }
}