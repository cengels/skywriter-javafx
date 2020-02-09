package com.cengels.skywriter.theming

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import tornadofx.getValue
import tornadofx.setValue
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.swing.filechooser.FileSystemView

class ThemesManager {
    val themesProperty = SimpleListProperty<Theme>()
    var themes by themesProperty
    val file: File
            get() = File("${FileSystemView.getFileSystemView().defaultDirectory.path}${File.separator}Skywriter${File.separator}user.themes")

    /** Loads all themes from file. */
    fun load() {
        if (file.exists()) {
            file.inputStream().apply {
                ObjectInputStream(this).apply {
                    themes.clear()
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
                this.writeObject(themes.toList())

                this.close()
            }

            this.close()
        }
    }
}