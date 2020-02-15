package com.cengels.skywriter.theming

import com.cengels.skywriter.SkyWriterApp
import com.cengels.skywriter.persistence.AppConfig
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import tornadofx.*
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import tornadofx.getValue
import tornadofx.setValue

class ThemesManager {
    companion object {
        val DEFAULT = Theme(name = "Default", default = true)
    }

    val themesProperty = SimpleListProperty<Theme>(observableListOf())
    var themes: ObservableList<Theme> by themesProperty
    val selectedThemeProperty = SimpleObjectProperty<Theme>()
    var selectedTheme by selectedThemeProperty

    val file: File
            get() = File("${SkyWriterApp.userDirectory}user.themes")

    init {
        themes.add(DEFAULT)
    }

    /** Duplicates the selected theme. Throws an exception if no theme is selected. */
    fun duplicate() {
        themes.add(themes.indexOf(selectedTheme) + 1, selectedTheme!!.clone().apply {
            while (themes.any { it.name == this.name }) {
                val endingNumber = this.name.takeLastWhile { it.isDigit() }.toIntOrNull()
                this.name = "${this.name.dropLastWhile { it.isDigit() }.trimEnd()} ${(endingNumber ?: 1) + 1}"
            }
        })
        selectedTheme = themes[themes.indexOf(selectedTheme) + 1]
    }

    /** Applies the selected theme. Throws an exception if no theme is selected. */
    fun applySelected() {
        AppConfig.activeTheme = selectedTheme!!.name
        AppConfig.save()
    }

    /** Loads all themes from file. */
    fun load() {
        if (file.exists()) {
            file.inputStream().apply {
                ObjectInputStream(this).apply {
                    themes.filterNot { it.default }.apply { themes.removeAll(this) }
                    themes.addAll(this.readObject() as List<Theme>)

                    this.close()
                }

                this.close()
            }
        }
    }

    /** Saves all themes to file. */
    fun save() {
        file.outputStream().apply {
            ObjectOutputStream(this).apply {
                this.writeObject(themes.filter { !it.default }.toList())

                this.close()
            }

            this.close()
        }
    }
}