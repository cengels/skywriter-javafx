package com.cengels.skywriter.theming

import com.cengels.skywriter.enum.FieldType
import com.cengels.skywriter.fragments.Dialog
import com.cengels.skywriter.style.ThemedStylesheet
import com.cengels.skywriter.style.ThemingStylesheet
import com.cengels.skywriter.svg.Icons
import com.cengels.skywriter.util.*
import javafx.application.Platform
import javafx.beans.property.Property
import javafx.geometry.Pos
import javafx.scene.control.ButtonBar
import javafx.scene.control.OverrunStyle
import javafx.scene.control.ScrollPane
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Screen
import tornadofx.*
import java.io.File

class EditThemeView(theme: Theme, private val otherThemes: List<String>)
    : Dialog<Theme>(if (theme.name.isNotEmpty()) "Edit theme" else "Add theme", ThemingStylesheet()) {
    private val model: EditThemeViewModel = EditThemeViewModel(theme)
    private var textAreaBox: VBox? = null

    override fun onDock() {
        super.onDock()

        setWindowMinSize(900.0, 440.0)
        // During the first layout pass, widthProperty does not fire a changed event.
        // Therefore, prefHeight will remain unset without the below call.
        Platform.runLater { textAreaBox?.requestLayout() }
    }

    override val content = borderpane {
        left {
            scrollpane {
                hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                maxWidth = 307.5

                form {
                    maxWidth = 300.0
                    fieldset {
                        field("Name") {
                            textfield(model.nameProperty) {
                                validator {
                                    if (it!!.isEmpty()) {
                                        return@validator error("Please enter a name for this theme.")
                                    } else if (otherThemes.contains(it)) {
                                        return@validator error("Name must be unique.")
                                    }

                                    return@validator success()
                                }
                            }
                        }
                    }

                    fieldset("Font") {
                        field {
                            (inputContainer as HBox).alignment = Pos.CENTER_LEFT
                            fontpicker(model.fontFamilyProperty, ThemesManager.fonts) {
                                hgrow = Priority.NEVER
                                prefWidth = 240.0
                            }
                            numberfield(model.fontSizeProperty).prefWidth = 120.0
                            colorpicker(model.fontColorProperty, ColorPickerMode.Button)
                        }
                        field("Line height") {
                            (inputContainer as HBox).alignment = Pos.CENTER_LEFT
                            percentfield(model.lineHeightProperty, 10.0)
                            combobox(model.textAlignmentProperty) {
                                minWidth = 80.0
                            }
                        }
                        // TODO: Issue #15
                        // field("First line indent") {
                        //     textfield(model.firstLineIndentProperty, getDefaultConverter()!!) {
                        //         required()
                        //         filterInput { it.controlNewText.isDouble() }
                        //     }
                        // }
                    }

                    fieldset("Window") {
                        field("Background color") {
                            (inputContainer as HBox).alignment = Pos.CENTER_LEFT
                            colorpicker(model.windowBackgroundProperty, ColorPickerMode.Button) {
                                useMaxWidth = true
                            }
                        }
                        field("Image") {
                            (inputContainer as HBox).alignment = Pos.CENTER_LEFT
                            button(model.backgroundImageProperty.stringBinding { it?.takeLastWhile { char -> char != File.separatorChar } ?: "Choose an image..." }) {
                                addClass(ThemedStylesheet.skyButton)
                                action {
                                    val initialDir = if (!model.backgroundImage.isNullOrBlank()) File(model.backgroundImage).parent else System.getProperty("user.dir")

                                    chooseFile(
                                        "Open Image...",
                                        arrayOf(imageExtensionFilter),
                                        File(initialDir),
                                        FileChooserMode.Single).apply {
                                        if (this.isNotEmpty()) {
                                            model.backgroundImage = this.single().absolutePath
                                        }
                                    }
                                }
                                useMaxWidth = true
                            }
                            svgbutton(Icons.X) {
                                disableWhen { model.backgroundImageProperty.isBlank() }
                                action {
                                    model.backgroundImage = null
                                }
                            }
                        }
                        field(forceLabelIndent = true) {
                            combobox(model.backgroundImageSizingTypeProperty) {
                                useMaxWidth = true
                                disableWhen { model.backgroundImageProperty.isBlank() }
                            }
                        }
                    }

                    fieldset("Document") {
                        field("Background color") {
                            (inputContainer as HBox).alignment = Pos.CENTER_LEFT
                            colorpicker(model.documentBackgroundProperty, ColorPickerMode.Button) {
                                useMaxWidth = true
                            }
                        }

                        field("Height") {
                            (inputContainer as HBox).alignment = Pos.CENTER_LEFT
                            combinedfield(model.documentHeightProperty, onSwitch = { oldValue, newValue ->
                                this.hgrow = Priority.ALWAYS
                                if (newValue == FieldType.NUMBER) {
                                    model.documentHeight *= Screen.getPrimary().bounds.height
                                } else {
                                    model.documentHeight /= Screen.getPrimary().bounds.height
                                }
                            })
                        }
                        field("Width") {
                            (inputContainer as HBox).alignment = Pos.CENTER_LEFT
                            combinedfield(model.documentWidthProperty, onSwitch = { oldValue, newValue ->
                                if (newValue == FieldType.NUMBER) {
                                    model.documentWidth *= Screen.getPrimary().bounds.width
                                } else {
                                    model.documentWidth /= Screen.getPrimary().bounds.width
                                }
                            }) {
                                hgrow = Priority.ALWAYS
                            }
                        }
                    }

                    fieldset("Padding") {
                        field {
                            (inputContainer as HBox).alignment = Pos.CENTER_LEFT
                            label("Horizontal").minWidth = 60.0
                            pixelfield(model.paddingHorizontalProperty as Property<Number>)
                            label("Vertical") {
                                minWidth = 50.0
                                alignment = Pos.CENTER_RIGHT
                            }
                            pixelfield(model.paddingVerticalProperty as Property<Number>)
                        }
                    }

                    fieldset("Font shadow") {
                        field("Color") {
                            (inputContainer as HBox).alignment = Pos.CENTER_LEFT
                            colorpicker(model.fontShadowColorProperty, ColorPickerMode.Button) {
                                useMaxWidth = true
                            }
                            svgbutton(Icons.X) {
                                disableWhen { model.fontShadowColorProperty.isEqualTo(Color.TRANSPARENT) }
                                action {
                                    model.fontShadowColor = Color.TRANSPARENT
                                }
                            }
                        }
                        field {
                            label("Radius").minWidth = 50.0
                            pixelfield(model.fontShadowRadiusProperty as Property<Number>) {
                                validator {
                                    if (it == null || !it.isDouble() || it.toDouble() > 127.0) {
                                        error("Value must be between 0 and 127.");
                                    }

                                    success()
                                }
                            }
                            label("Spread").minWidth = 50.0
                            percentfield(model.fontShadowSpreadProperty)
                        }
                        field {
                            label("Offset X").minWidth = 50.0
                            pixelfield(model.fontShadowOffsetXProperty as Property<Number>)
                            label("Offset Y").minWidth = 50.0
                            pixelfield(model.fontShadowOffsetYProperty as Property<Number>)
                        }
                    }
                }
            }
        }

        center {
            vbox parentContainer@ {
                alignment = Pos.CENTER

                vbox textArea@ {
                    isFillWidth = false

                    backgroundProperty().bind(model.windowBackgroundProperty.objectBinding(model.backgroundImageProperty, model.backgroundImageSizingTypeProperty) { getBackgroundFor(it!!, model.backgroundImage, model.backgroundImageSizingType) })
                    // 16:9
                    prefHeightProperty().bind(widthProperty().multiply(0.5625))
                    textAreaBox = this@textArea

                    alignment = Pos.CENTER

                    vbox document@{
                        isFillWidth = true
                        paddingVerticalProperty.bind(model.paddingVerticalProperty)
                        paddingHorizontalProperty.bind(model.paddingHorizontalProperty)

                        backgroundProperty().bind(model.documentBackgroundProperty.backgroundBinding())
                        prefWidthProperty().bind(model.documentWidthProperty.doubleBinding(this@textArea.widthProperty(), FX.primaryStage.widthProperty()) {
                            if (it!! <= 1) {
                                return@doubleBinding this@textArea.width * it
                            }

                            return@doubleBinding it / Screen.getPrimary().bounds.width * this@textArea.width
                        })
                        prefHeightProperty().bind(model.documentHeightProperty.doubleBinding(this@textArea.heightProperty(), FX.primaryStage.heightProperty()) {
                            if (it!! <= 1) {
                                return@doubleBinding this@textArea.height * it
                            }

                            return@doubleBinding it / Screen.getPrimary().bounds.height * this@textArea.height
                        })

                        label {
                            useMaxWidth = true
                            loremIpsum(LoremIpsum.MAX)
                            textFillProperty().bind(model.fontColorProperty)
                            textAlignmentProperty().bind(model.textAlignmentProperty)
                            lineSpacingProperty().bind(model.lineHeightProperty)
                            effectProperty().bind(model.fontShadowColorProperty.objectBinding(model.fontShadowRadiusProperty, model.fontShadowSpreadProperty, model.fontShadowOffsetXProperty, model.fontShadowOffsetYProperty) {
                                DropShadow(BlurType.GAUSSIAN, model.fontShadowColor, model.fontShadowRadius, model.fontShadowSpread, model.fontShadowOffsetX, model.fontShadowOffsetY)
                            })
                            isWrapText = true
                            textOverrun = OverrunStyle.CLIP
                            fontProperty().bind(this@parentContainer.widthProperty().objectBinding(model.fontSizeProperty, model.fontFamilyProperty) {
                                Font.font(model.fontFamily, this@parentContainer.height / FX.primaryStage.height * model.fontSize)
                            })
                        }
                    }
                }
            }
        }

        bottom {
            buttonbar {
                button("OK", ButtonBar.ButtonData.OK_DONE) {
                    enableWhen { model.valid.and(model.dirty) }

                    action {
                        model.commit()
                        submit(model.item)
                    }
                }
                button("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE).action { cancel() }
            }
        }
    }
}

