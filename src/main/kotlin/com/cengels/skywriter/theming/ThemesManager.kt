package com.cengels.skywriter.theming

import com.cengels.skywriter.SkyWriterApp
import com.cengels.skywriter.persistence.AppConfig
import com.google.gson.GsonBuilder
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.scene.text.Font
import tornadofx.*
import java.io.File
import tornadofx.getValue
import tornadofx.setValue

object ThemesManager {
    val DEFAULT = Theme(name = "Default", default = true)
    private var fontsTask: Task<List<String>>? = null
    val fonts: List<String>?
        get() = fontsTask?.get()

    val gson = GsonBuilder().setPrettyPrinting().create()
    val themesProperty = SimpleListProperty<Theme>(observableListOf())
    var themes: ObservableList<Theme> by themesProperty
    val selectedThemeProperty = SimpleObjectProperty<Theme>(DEFAULT)
    var selectedTheme by selectedThemeProperty

    val file: File
            get() = SkyWriterApp.applicationDirectory.resolve("themes.json").toFile()

    init {
        themes.add(DEFAULT)
    }

    fun initializeFonts() {
        fontsTask = runAsync { Font.getFamilies() }
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
            val input = file.readText()
            themes.filterNot { it.default }.apply { themes.removeAll(this) }
            themes.addAll(gson.fromJson(input, Array<Theme>::class.java))
        }
    }

    /** Saves all themes to file. */
    fun save() {
        // Necessary to force an invalidation of the selectedTheme property, allowing the WriterView to immediately update its properties.
        // Without this, the theme would not be reflected in the WriterView until the user switched to another theme and back,
        // or restarted the application.
        this.selectedTheme.apply {
            selectedTheme = ThemesManager.DEFAULT
            selectedTheme = this
        }

        file.writeText(gson.toJson(themes.filter { !it.default }))
    }
}