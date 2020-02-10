package com.cengels.skywriter.theming

import com.cengels.skywriter.persistence.AppConfig
import javafx.scene.control.ButtonType
import javafx.scene.control.ScrollPane
import javafx.scene.paint.Color
import tornadofx.*

class ThemesView : View("Themes") {
    val themesManager = ThemesManager()

    override fun onDock() {
        super.onDock()

        themesManager.load()

        AppConfig.activeTheme.apply {
            themesManager.selectedTheme = themesManager.themes.find { it.name == this } ?: ThemesManager.DEFAULT
        }
    }

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
                button("Add")
                button("Duplicate").action {
                    themesManager.duplicate()
                    themesManager.save()
                }
                button("Edit") {
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
            hbox {
                button("OK") {
                    enableWhen { themesManager.selectedThemeProperty.isNotNull }

                    action {
                        themesManager.applySelected()
                        close()
                    }
                }
                button("Cancel").action { close() }
            }
        }
    }
}