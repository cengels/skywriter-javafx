package com.cengels.skywriter.theming

import com.cengels.skywriter.fragments.ThemedView
import com.cengels.skywriter.style.ThemedStylesheet
import com.cengels.skywriter.style.ThemingStylesheet
import com.cengels.skywriter.util.bindSelectedItem
import com.cengels.skywriter.util.getBackgroundFor
import com.cengels.skywriter.util.onChangeAndNow
import com.cengels.skywriter.util.setRadiusClip
import javafx.geometry.Pos
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.ScrollPane
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import tornadofx.*

class ThemesView(val themesManager: ThemesManager) : ThemedView("Themes", ThemingStylesheet()) {
    override fun onDock() {
        super.onDock()

        themesManager.initializeFonts()
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        setWindowMinSize(500.0, 350.0)
        setWindowInitialSize(750.0, 450.0)
    }

    override val content = borderpane {
        center {
            this.useMaxWidth = true
            this.useMaxHeight = true

            scrollpane {
                hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
                // fitTo properties cause the scrollbars to always be visible

                datagrid(themesManager.themes) {
                    addClass(ThemingStylesheet.themesGrid)

                    minHeightProperty().bind(this@scrollpane.heightProperty())
                    minWidthProperty().bind(this@scrollpane.widthProperty())

                    bindSelectedItem(themesManager.selectedThemeProperty)

                    cellFormat {
                        toggleClass(Stylesheet.selectedClass, this.selectedProperty())
                    }

                    cellCache {
                        vbox {
                            useMaxWidth = true
                            alignment = Pos.CENTER
                            setRadiusClip(ThemedStylesheet.cornerRadius.value)
                            gridpane {
                                addClass(ThemingStylesheet.gridPane)
                                this.background =  getBackgroundFor(it.windowBackground, it.backgroundImage, it.backgroundImageSizingType)
                                this.useMaxWidth = true
                                this.useMaxHeight = true
                                setRadiusClip(ThemedStylesheet.cornerRadius.value)
                                vgrow = Priority.ALWAYS
                                this.columnConstraints.addAll(
                                    ColumnConstraints().apply { this.hgrow = Priority.ALWAYS },
                                    ColumnConstraints().apply {
                                        this.percentWidth = if (it.documentWidth <= 1.0) it.documentWidth * 100.0 else this@gridpane.width / it.documentWidth * 100.0
                                    },
                                    ColumnConstraints().apply { this.hgrow = Priority.ALWAYS }
                                )
                                this.rowConstraints.addAll(
                                    RowConstraints().apply { this.vgrow = Priority.ALWAYS },
                                    RowConstraints().apply {
                                        this.percentHeight = if (it.documentHeight <= 1.0) it.documentHeight * 100.0 else this@gridpane.height / it.documentHeight * 100.0
                                    },
                                    RowConstraints().apply { this.vgrow = Priority.ALWAYS }
                                )

                                label {
                                    gridpaneConstraints { columnRowIndex(1, 1) }
                                    useMaxSize = true
                                    vgrow = Priority.ALWAYS
                                    background = getBackgroundFor(it.documentBackground)
                                    text = "A"
                                    font = Font.font(it.fontFamily, (it.fontSize * 3.0).coerceIn(30.0, 100.0))
                                    textFill = it.fontColor
                                    alignment = Pos.CENTER
                                }
                            }
                            label(it.name) {
                                addClass(ThemedStylesheet.skyText)
                                hgrow = Priority.ALWAYS
                                textAlignment = TextAlignment.CENTER
                            }
                        }
                    }
                }
            }
        }

        right {
            vbox {
                spacing = 10.0
                button("Add").action {
                    openEditDialog(Theme()).result {
                        if (ok) {
                            themesManager.themes.add(result)
                            themesManager.save()
                        }
                    }
                }
                button("Duplicate").action {
                    themesManager.duplicate()
                    themesManager.save()
                }
                button("Edit") {
                    action {
                        openEditDialog(themesManager.selectedTheme!!).result {
                            if (ok) {
                                themesManager.save()
                            }
                        }
                    }
                    // For some reason, the condition needs to evaluate to false for it to enable the button.
                    enableWhen { themesManager.selectedThemeProperty.booleanBinding { it != null && !it.default } }
                }
                button("Remove") {
                    enableWhen { themesManager.selectedThemeProperty.booleanBinding { it != null && !it.default } }
                    shortcut("Delete")

                    action {
                        warning("Delete", "Are you sure you want to delete this theme? This operation cannot be undone.", ButtonType.YES, ButtonType.NO) {
                            if (this.result == ButtonType.YES) {
                                themesManager.themes.remove(themesManager.selectedTheme)
                                themesManager.save()
                            }
                        }
                    }
                }
            }
        }

        bottom {
            buttonbar {
                button("OK", ButtonBar.ButtonData.OK_DONE) {
                    enableWhen { themesManager.selectedThemeProperty.isNotNull }

                    action {
                        themesManager.applySelected()
                        close()
                    }
                }
                button("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE).action { close() }
            }
        }
    }

    private fun openEditDialog(theme: Theme): EditThemeView {
        return EditThemeView(theme, themesManager.themes.filter { it != theme }.map { it.name }).apply {
            this.openModal(owner = currentWindow)
        }
    }
}