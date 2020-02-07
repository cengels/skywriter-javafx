package com.cengels.skywriter.writer

import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class TextDocument {
    val textProperty = SimpleStringProperty(this, "text")
    val text by textProperty
}