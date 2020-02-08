package com.cengels.skywriter.writer

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import java.io.File
import tornadofx.getValue
import tornadofx.onChange
import tornadofx.setValue

class WriterViewModel {
    val fileProperty = SimpleObjectProperty<File?>()
    var file: File? by fileProperty

    val fileExistsProperty = SimpleBooleanProperty(file != null)
    var fileExists by fileExistsProperty

    val dirtyProperty = SimpleBooleanProperty(false)
    /** Whether the document has been modified since its last save. */
    var dirty by dirtyProperty

    init {
        fileProperty.onChange { fileExists = file != null }
    }
}