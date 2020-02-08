package com.cengels.skywriter.writer

import javafx.beans.property.SimpleObjectProperty
import java.io.File
import tornadofx.getValue
import tornadofx.setValue

class WriterViewModel {
    val fileProperty = SimpleObjectProperty<File?>()
    var file: File? by fileProperty
}