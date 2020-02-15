package com.cengels.skywriter.theming

import com.cengels.skywriter.enum.FieldType
import com.cengels.skywriter.fragments.Dialog
import com.cengels.skywriter.util.*
import javafx.beans.binding.DoubleBinding
import javafx.beans.binding.DoubleExpression
import javafx.beans.property.Property
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.ButtonBar
import javafx.scene.control.OverrunStyle
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.stage.Screen
import javafx.util.StringConverter
import javafx.util.converter.DoubleStringConverter
import javafx.util.converter.PercentageStringConverter
import tornadofx.*

class EditThemeView(theme: Theme, private val otherThemes: List<String>) : Dialog<Theme>(if (theme.name.isNotEmpty()) "Edit theme" else "Add theme") {
    private val model: EditThemeViewModel = EditThemeViewModel(theme)
    private lateinit var documentHeightBinding: DoubleBinding

    override fun onDock() {
        super.onDock()

        setWindowMinSize(900.0, 440.0)
    }

    override val root = borderpane {
        left {
            scrollpane {
                hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                maxWidth = 300.0

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
                            alignment = Pos.CENTER_LEFT
                            combobox(model.fontFamilyProperty, Font.getFamilies()).required()
                            numberfield(model.fontSizeProperty).prefWidth = 120.0
                            colorpicker(model.fontColorProperty, ColorPickerMode.Button)
                        }
                        field("Line height") {
                            percentfield(model.lineHeightProperty)
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

                    fieldset("Dimensions") {
                        field("Height") {
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
                            label("Horizontal").minWidth = 60.0
                            pixelfield(model.paddingHorizontalProperty as Property<Number>)
                            label("Vertical") {
                                minWidth = 50.0
                                alignment = Pos.CENTER_RIGHT
                            }
                            pixelfield(model.paddingVerticalProperty as Property<Number>)
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

                    backgroundProperty().bind(model.backgroundFillProperty.backgroundBinding())
                    // 16:9
                    documentHeightBinding = widthProperty().multiply(0.5625)
                    prefHeightProperty().bind(documentHeightBinding)

                    alignment = Pos.CENTER

                    vbox document@{
                        isFillWidth = true
                        paddingVerticalProperty.bind(model.paddingVerticalProperty)
                        paddingHorizontalProperty.bind(model.paddingHorizontalProperty)

                        backgroundProperty().bind(model.backgroundDocumentProperty.backgroundBinding())
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
                            loremIpsum()
                            textFillProperty().bind(model.fontColorProperty)
                            textAlignmentProperty().bind(model.textAlignmentProperty)
                            lineSpacingProperty().bind(model.lineHeightProperty)
                            isWrapText = true
                            textOverrun = OverrunStyle.CLIP
                            this@parentContainer.widthProperty().addListener { observable, oldValue, newValue ->
                                font = Font.font(model.fontFamily, newValue!!.toDouble() / FX.primaryStage.width * model.fontSize * 2)
                            }
                            model.fontSizeProperty.addListener { observable, oldValue, newValue ->
                                font = Font.font(model.fontFamily, this@parentContainer.width / FX.primaryStage.width * model.fontSize * 2)
                            }
                            model.fontFamilyProperty.addListener { observable, oldValue, newValue ->
                                font = Font.font(model.fontFamily, this@parentContainer.width / FX.primaryStage.width * model.fontSize * 2)
                            }
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

