package com.cengels.skywriter.util

import com.cengels.skywriter.enum.FieldType
import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import javafx.util.Duration
import javafx.util.StringConverter
import javafx.util.converter.IntegerStringConverter
import javafx.util.converter.PercentageStringConverter
import tornadofx.*
import java.util.function.UnaryOperator
import kotlin.math.min

/** Adds a custom text field that only accepts the number type which was passed in. */
inline fun <reified T : Any> EventTarget.numberfield(property: Property<T>, noinline op: TextField.() -> Unit = {}) = textfield(property, getDefaultConverter()!!, op).apply {
    required()
    alignment = Pos.CENTER_RIGHT
    filterInput {
        when (T::class.javaPrimitiveType ?: T::class) {
            Int::class.javaPrimitiveType -> it.controlNewText.isInt()
            Long::class.javaPrimitiveType -> it.controlNewText.isLong()
            Double::class.javaPrimitiveType -> it.controlNewText.isDouble()
            Float::class.javaPrimitiveType -> it.controlNewText.isFloat()
            else -> throw TypeCastException("Invalid type parameter.")
        }
    }
}

/** Creates a TextFormatter with the specified filter. Return the Change object to accept the change or null to reject it. You can also setText() to edit the text that will be added or removed from the input field. */
fun TextField.format(filter: (it: TextFormatter.Change) -> TextFormatter.Change?) {
    textFormatter = TextFormatter<Any>(filter)
}

/** Adds a custom field that only accepts percentages and converts to doubles between 0 and 1. */
fun EventTarget.percentfield(property: Property<Double>, op: TextField.() -> Unit = {}) = textfield(property.apply { this.value = this.value.coerceIn(0.0..1.0) }, PercentageStringConverter() as StringConverter<Double>, op).apply {
    alignment = Pos.CENTER_RIGHT

    format {
        val number = it.controlNewText.removeSuffix("%")
        val double: Double? = if (number.isDouble()) number.toDouble() else null

        if (!it.controlNewText.endsWith("%") || (double == null && number.isNotBlank()) || (double != null && double !in 0.0..100.0)) {
            return@format null
        }

        if (it.controlNewText.removeSuffix("%").isBlank()) {
            it.text = "0"
        } else if (it.controlNewText != "0%" && it.controlNewText.startsWith("0")) {
            it.text = it.text.trimStart('0')
        }

        return@format it
    }

    this.selectionProperty().addListener { observable, oldValue, newValue ->
        val indexOfPercent = this.text.lastIndexOf('%')
        if (newValue.end > this.text.lastIndexOf('%')) {
            this.selectRange(min(newValue.start, indexOfPercent), indexOfPercent)
        }
    }

    validator {
        if (it == null || it.isBlank()) {
            error("Please enter a value.")
        } else if (it.endsWith('%') && it.dropLast(1).isDouble()) {
            success()
        } else {
            error("Please enter a valid percentage.")
        }
    }
}

/** Adds a combobox consisting of values from the specified enum. */
inline fun <reified E : Enum<E>> EventTarget.combobox(property: Property<E>? = null, noinline op: ComboBox<E>.() -> Unit = {}) = combobox(property, enumValues<E>().asList(), op).apply {
    converter = EnumConverter<E>()
}

/** Adds a custom text field that only accepts integers, shows a suffix, and can be incremented using arrows. */
fun EventTarget.pixelfield(property: Property<Number>, op: TextField.() -> Unit = {}) = textfield(property, SuffixConverter("pixels"), op).apply {
    alignment = Pos.CENTER_RIGHT
    filterInput { it.controlNewText.endsWith(" pixels") && it.controlNewText.removeSuffix(" pixels").isInt() }
    validator {
        if (it == null || it.removeSuffix(" pixels").isBlank() || it.removeSuffix(" pixels") == "0") {
            error("Please enter a value.")
        } else {
            success()
        }
    }
}

/** Adds a custom text field whose type depends on an accompanying combo box. */
fun EventTarget.combinedfield(property: Property<Double>, orientation: Orientation = Orientation.VERTICAL, op: TextField.() -> Unit = {}): Pane {
    val innerOp: Pane.() -> Unit = {
        var textField: TextField? = null
        combobox<FieldType> {
            selectionModel.select(if (property.value <= 1.0) FieldType.PERCENTAGE else FieldType.NUMBER)
            selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
                if (oldValue != newValue && textField != null) {
                    val newField = when (newValue) {
                        FieldType.NUMBER -> pixelfield(property as Property<Number>, op = op)
                        FieldType.PERCENTAGE -> percentfield(property, op = op)
                        else -> throw NullPointerException("FieldType cannot be null.")
                    }

                    textField!!.textProperty().unbind()
                    textField!!.replaceWith(newField)
                    textField = newField
                }
            }
        }
        textField = if (property.value <= 1.0) percentfield(property, op = op) else pixelfield(property as Property<Number>, op = op)
    }

    return if (orientation == Orientation.VERTICAL) vbox(op = innerOp) else hbox(op = innerOp)
}
