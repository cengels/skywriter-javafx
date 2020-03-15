package com.cengels.skywriter.util

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.text.Text
import tornadofx.addClass
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.useMaxHeight

class SpacedLabel(text: String = "", spacing: Number = 0) : HBox(spacing.toDouble()) {
    val textProperty = SimpleStringProperty(text)
    var text: String by textProperty

    init {
        addClass("label", "spaced-label")
        isFillHeight = false
        alignment = Pos.CENTER
        textProperty.onChangeAndNow { newValue ->
            children.clear()
            newValue?.forEach {
                children.add(Text(it.toString()).apply { addClass("spaced-label-text", "text") })
            }
        }
    }
}