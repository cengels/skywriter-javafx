package com.cengels.skywriter.theming

import com.cengels.skywriter.enum.FieldType
import com.cengels.skywriter.enum.ImageSizingType
import com.cengels.skywriter.fragments.Dialog
import com.cengels.skywriter.util.*
import javafx.beans.binding.DoubleBinding
import javafx.beans.property.Property
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ButtonBar
import javafx.scene.control.OverrunStyle
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.SVGPath
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Screen
import tornadofx.*
import java.io.File

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
                            combobox(model.fontFamilyProperty, Font.getFamilies()).required()
                            numberfield(model.fontSizeProperty).prefWidth = 120.0
                            colorpicker(model.fontColorProperty, ColorPickerMode.Button)
                        }
                        field("Line height") {
                            (inputContainer as HBox).alignment = Pos.CENTER_LEFT
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
                            button(graphic = SVGPath().apply { content = "M 18 6 L 6 18 M 6 6 L 18 18"; stroke = Color.BLACK; strokeWidth = 2.0 }) {
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
                }
            }
        }

        center {
            vbox parentContainer@ {
                alignment = Pos.CENTER

                vbox textArea@ {
                    isFillWidth = false

                    backgroundProperty().bind(model.windowBackgroundProperty.objectBinding(model.backgroundImageProperty, model.backgroundImageSizingTypeProperty) {
                        if (model.backgroundImage.isNullOrBlank()) {
                            return@objectBinding Background(BackgroundFill(it, CornerRadii.EMPTY, Insets.EMPTY))
                        }

                        return@objectBinding Background(
                            arrayOf(BackgroundFill(it, CornerRadii.EMPTY, Insets.EMPTY)),
                            arrayOf(BackgroundImage(Image("file:///${model.backgroundImage}"),
                                if (model.backgroundImageSizingType == ImageSizingType.TILE) BackgroundRepeat.REPEAT else BackgroundRepeat.NO_REPEAT,
                                if (model.backgroundImageSizingType == ImageSizingType.TILE) BackgroundRepeat.REPEAT else BackgroundRepeat.NO_REPEAT,
                                if (model.backgroundImageSizingType == ImageSizingType.CENTER) BackgroundPosition.CENTER else BackgroundPosition.DEFAULT,
                                BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false,
                                    model.backgroundImageSizingType == ImageSizingType.CONTAIN,
                                    model.backgroundImageSizingType == ImageSizingType.COVER))))
                    })
                    // 16:9
                    documentHeightBinding = widthProperty().multiply(0.5625)
                    prefHeightProperty().bind(documentHeightBinding)

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

