package com.cengels.skywriter.style

import com.cengels.skywriter.theming.Theme
import com.cengels.skywriter.util.allDescendants
import com.cengels.skywriter.util.shiftBy
import javafx.scene.Cursor
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontSmoothingType
import tornadofx.*

class WriterViewStylesheet(theme: Theme) : Stylesheet() {
    companion object {
        val styledTextArea by cssclass()
        val paragraphText by cssclass()
        val text by cssclass()
        val statusBar by cssclass()
        val caret by cssclass()
        val paragraphBox by cssclass()
        val findBar by cssclass()
        val container by cssclass()
        val textButton by cssclass()
        val svg by cssclass()
        val clickable by cssclass()
        val trackBackground by cssclass()
        val active by cssclass()
        val popupBox by cssclass()
        val selection by cssclass()

        val firstParagraph by csspseudoclass("first-paragraph")
        val lastParagraph by csspseudoclass("last-paragraph")

        val lineSpacing by cssproperty<Dimension<Dimension.LinearUnits>>("-fx-line-spacing")
        val height by cssproperty<Dimension<Dimension.LinearUnits>>("-fx-height")
        val width by cssproperty<Dimension<Dimension.LinearUnits>>("-fx-width")
    }

    init {
        val documentHoverColor = theme.documentBackground.brighter()
        val desaturatedFontColor = theme.fontColor.shiftBy(-0.25)
        val documentColorDarkest = theme.documentBackground.shiftBy(-0.15)
        
        s(paragraphText, menuBar, statusBar) {
            text {
                fill = theme.fontColor
            }
        }

        styledTextArea {
            star {
                textAlignment = theme.textAlignment
                fontSmoothingType = FontSmoothingType.GRAY
            }

            caret {
                stroke = theme.fontColor
            }

            text {
                effect = DropShadow(BlurType.GAUSSIAN, theme.fontShadowColor, theme.fontShadowRadius, theme.fontShadowSpread, theme.fontShadowOffsetX, theme.fontShadowOffsetY)
            }

            selection {
                fill = documentHoverColor
            }

            paragraphText {
                lineSpacing.value = (theme.lineHeight * 100).percent
                padding = box(0.px, 0.px, 20.px, 0.px)
            }

            paragraphBox {
                and(firstParagraph) {
                    padding = box(1000.px, 0.px, 0.px, 0.px)
                }

                and(lastParagraph) {
                    padding = box(0.px, 0.px, 1000.px, 0.px)
                }
            }
        }

        separator child line {
            borderColor += CssBox(theme.fontColor, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT)
            borderInsets += box(0.0.px)
        }

        s(menuButton, menuItem) {
            and(hover) {
                backgroundColor += documentHoverColor
            }
        }

        s(menuBar, menuButton, statusBar, menuItem, menu.contains(container), findBar) {
            backgroundColor += theme.documentBackground.shiftBy(-0.05)
        }
        
        s(findBar.contains(label), findBar.contains(text), findBar.contains(textInput)) {
            fill = theme.fontColor
            textFill = theme.fontColor
            promptTextFill = desaturatedFontColor
        }
        
        s(findBar.contains(textField), checkBox.contains(box)) {
            backgroundColor += documentColorDarkest
        }
        
        checkBox {
            and(hover) {
                box {
                    backgroundColor += documentHoverColor
                }
            }
            
            and(selected) {
                mark {
                    backgroundColor += theme.fontColor
                }
            }
        }

        GeneralStylesheet.plainButton {
            s(svg, svg.allDescendants) {
                stroke = theme.fontColor
            }

            and(hover) {
                s(svg, svg.allDescendants) {
                    stroke = desaturatedFontColor
                }
            }
        }

        textButton {
            fill = theme.fontColor
            textFill = theme.fontColor
            backgroundColor += documentColorDarkest

            and(hover) {
                backgroundColor += documentHoverColor
            }

            and(disabled) {
                opacity = 0.5
            }
        }

        statusBar {
            label {
                padding = box(3.0.px, 6.0.px)

                and(clickable) {
                    and(hover) {
                        backgroundColor += documentHoverColor
                        cursor = Cursor.HAND
                    }
                }
            }
        }

        contextMenu {
            fontSize = 10.pt
            backgroundColor += theme.documentBackground.shiftBy(-0.05)
            backgroundRadius += box(0.px, 0.px, 6.px, 6.px)

            star {
                fill = theme.fontColor
                textFill = theme.fontColor
            }
        }

        s(scrollBar, trackBackground) {
            backgroundColor += Color.TRANSPARENT
        }

        thumb {
            backgroundColor += theme.documentBackground.shiftBy(0.15)
            backgroundColor += documentHoverColor

            and(hover) {
                backgroundColor += documentHoverColor
            }
        }

        s(scrollBar.and(active).child(thumb)) {
            backgroundColor += documentHoverColor
        }

        s(incrementButton, decrementButton) {
            height.value = 0.px
            opacity = 0.0
        }

        popupBox {
            padding = box(10.px, 20.px)
            backgroundColor += theme.documentBackground.shiftBy(-0.1)
            backgroundRadius += box(3.px)
            effect = DropShadow(BlurType.GAUSSIAN, Color(0.0, 0.0, 0.0, 0.3), 12.0, 0.0, 2.0, 2.0)

            star {
                fill = theme.fontColor
                textFill = theme.fontColor
            }

            textField {
                fontSize = 14.pt
                backgroundColor += documentColorDarkest
            }
        }
    }
}