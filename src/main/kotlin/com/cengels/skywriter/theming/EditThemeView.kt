package com.cengels.skywriter.theming

import com.cengels.skywriter.util.backgroundBinding
import com.cengels.skywriter.fragments.Dialog
import com.cengels.skywriter.util.loremIpsum
import javafx.geometry.Pos
import javafx.scene.control.ButtonBar
import javafx.scene.control.OverrunStyle
import javafx.scene.control.ScrollPane
import javafx.scene.text.Font
import tornadofx.*

class EditThemeView(theme: Theme, private val otherThemes: List<String>) : Dialog<Theme>(if (theme.name.isNotEmpty()) "Edit theme" else "Add theme") {
    private val model: EditThemeViewModel = EditThemeViewModel(theme)

    override fun onDock() {
        super.onDock()

        setWindowMinSize(900.0, 440.0)
    }

    override val root = borderpane {
        left {
            scrollpane {
                hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                maxWidth = 400.0

                form {
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
                            combobox(model.fontFamilyProperty, Font.getFamilies()).required()
                            textfield(model.fontSizeProperty, getDefaultConverter()!!) {
                                required()
                                filterInput { it.controlNewText.isDouble() }
                            }
                        }
                        colorpicker(model.fontColorProperty, ColorPickerMode.Button)
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
                    prefHeightProperty().bind(widthProperty().multiply(0.5625))

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

                            return@doubleBinding it / FX.primaryStage.width * this@textArea.width
                        })
                        prefHeightProperty().bind(model.documentHeightProperty.doubleBinding(this@textArea.heightProperty(), FX.primaryStage.heightProperty()) {
                            if (it!! <= 1) {
                                return@doubleBinding this@textArea.height * it
                            }

                            return@doubleBinding it / FX.primaryStage.height * this@textArea.height
                        })

                        label {
                            useMaxWidth = true
                            loremIpsum()
                            textFillProperty().bind(model.fontColorProperty)
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

