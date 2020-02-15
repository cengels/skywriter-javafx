package com.cengels.skywriter.util

import com.cengels.skywriter.enum.FieldType
import com.cengels.skywriter.util.convert.EnumConverter
import com.cengels.skywriter.util.convert.SuffixConverter
import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.util.StringConverter
import javafx.util.converter.PercentageStringConverter
import tornadofx.*
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

        return@format it.assureSuffix("%")
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

/** Adds a custom text field that only accepts integers and shows a suffix. */
fun EventTarget.pixelfield(property: Property<Number>, op: TextField.() -> Unit = {}) = textfield(property, SuffixConverter("pixels"), op).apply {
    alignment = Pos.CENTER_RIGHT
    val suffix = " pixels"
    hgrow = Priority.ALWAYS

    format {
        val number = it.controlNewText.removeSuffix(suffix)
        val int: Int? = if (number.isInt()) number.toInt() else null

        if (!it.controlNewText.endsWith(suffix) || (int == null && number.isNotBlank()) || (int != null && int < 0)) {
            return@format null
        }

        return@format it.assureSuffix(suffix)
    }

    validator {
        if (it == null || it.removeSuffix(" pixels").isBlank() || it.removeSuffix(" pixels") == "0") {
            error("Please enter a value.")
        } else {
            success()
        }
    }
}

/** Applies several formatting rules to prevent the suffix from being removed or selected, and to prevent more than one leading zero. */
fun TextFormatter.Change.assureSuffix(suffix: String): TextFormatter.Change? {
    val number = this.controlNewText.removeSuffix(suffix)

    this.controlNewText.lastIndexOf(suffix).also {
        if (this.selection.end > it) {
            // Prevent the user from selecting the suffix.
            this.selectRange(min(this.selection.start, it), it)
        }
    }

    if (number.isBlank() || this.controlNewText.startsWith("0")) {
        val beforeDecimal = number.takeWhile { char -> char.isDigit() }
        if (number.isBlank()) {
            // Replace a blank field with an automatic zero.
            this.text = "0"
        } else if (beforeDecimal.all { digit -> digit == '0' } && beforeDecimal != "0") {
            // Reject input that attempts to add extraneous zeroes.
            return null
        } else if (beforeDecimal != "0") {
            // Trim leading zeroes off numbers that don't consist of only zeroes.
            this.text = this.text.trimStart('0')
            this.setRange(0, this.controlNewText.lastIndexOf('0') + 1)

            val suffixIndex = this.controlNewText.lastIndexOf(suffix)

            this.caretPosition = suffixIndex
            this.anchor = suffixIndex
        }
    } else if (this.controlText.removeSuffix(suffix) == "0" && this.isAdded) {
        if (this.text == ".") {
            // If text is zero, adding a decimal will always add it to the end.
            this.setRange(1, 1)
            this.caretPosition = 2
            this.anchor = 2
        } else {
            // All other digits will replace the zero with that digit.
            this.setRange(0, 1)
        }
    }

    return this
}

/** Adds a combobox consisting of values from the specified enum. */
inline fun <reified E : Enum<E>> EventTarget.combobox(property: Property<E>? = null, noinline op: ComboBox<E>.() -> Unit = {}) = combobox(property, enumValues<E>().asList(), op).apply {
    converter = EnumConverter<E>()
}

/** Adds a custom text field whose type depends on an accompanying combo box. */
fun EventTarget.combinedfield(property: Property<Double>, orientation: Orientation = Orientation.HORIZONTAL, onSwitch: (oldValue: FieldType, newValue: FieldType) -> Unit = { oldValue, newValue -> }, op: TextField.() -> Unit = {}): Pane {
    val innerOp: Pane.() -> Unit = {
        this.useMaxSize = true
        var textField: TextField? = null
        combobox<FieldType> {
            minWidth = 110.0
            selectionModel.select(if (property.value <= 1.0) FieldType.PERCENTAGE else FieldType.NUMBER)
            selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
                if (oldValue != newValue && textField != null) {
                    textField!!.textProperty().unbindBidirectional(property)
                    onSwitch(oldValue, newValue)
                    val newField = when (newValue) {
                        FieldType.NUMBER -> pixelfield(property as Property<Number>, op = op)
                        FieldType.PERCENTAGE -> percentfield(property, op = op)
                        else -> throw NullPointerException("FieldType cannot be null.")
                    }

                    textField!!.replaceWith(newField)
                    textField = newField
                }
            }
        }
        textField = if (property.value <= 1.0) percentfield(property, op = op) else pixelfield(property as Property<Number>, op = op)
    }

    return if (orientation == Orientation.VERTICAL) vbox { spacing = 10.0; innerOp(this) } else hbox { spacing = 10.0; innerOp(this) }
}
