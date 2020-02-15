package com.cengels.skywriter.theming

import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.ScrollPane
import tornadofx.*

class ThemesView(val themesManager: ThemesManager) : View("Themes") {
    override val root = borderpane {
        minHeight = 350.0
        minWidth = 500.0

        center {
            this.useMaxWidth = true
            this.useMaxHeight = true

            scrollpane {
                this.useMaxWidth = true
                this.useMaxHeight = true
                this.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                datagrid(themesManager.themes) {
                    this.useMaxWidth = true
                    this.useMaxHeight = true

                    bindSelected(themesManager.selectedThemeProperty)

                    cellFormat {
                        addClass("theme-container")
                    }

                    cellCache {
                        vbox {
                            rectangle(0.0, 0.0, 100.0, 100.0)
                            label(it.name)
                        }
                    }
                }
            }
        }

        right {
            vbox {
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
        return EditThemeView(theme, themesManager.themes.filter { it != theme }.map { it.name }).apply { this.openModal(owner = currentWindow) }
    }
}