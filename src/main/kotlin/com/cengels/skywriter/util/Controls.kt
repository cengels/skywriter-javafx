package com.cengels.skywriter.util

import com.cengels.skywriter.enum.FieldType
import com.cengels.skywriter.style.GeneralStylesheet
import com.cengels.skywriter.style.ThemedStylesheet
import com.cengels.skywriter.style.WriterViewStylesheet
import com.cengels.skywriter.svg.SVG
import com.cengels.skywriter.util.convert.EnumConverter
import com.cengels.skywriter.util.convert.SuffixConverter
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.shape.Line
import javafx.scene.shape.SVGPath
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.stage.Popup
import javafx.stage.PopupWindow
import javafx.util.StringConverter
import javafx.util.converter.PercentageStringConverter
import tornadofx.*
import tornadofx.Stylesheet.Companion.star
import java.time.Instant
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

/** Adds a custom text field that only accepts the number type which was passed in. */
inline fun <reified T : Any> EventTarget.numberfield(value: T, noinline op: TextField.() -> Unit = {}) = textfield(
    getDefaultConverter<T>()!!.toString(value), op).apply {
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

/** Adds a custom field that only accepts percentages and converts to doubles by dividing by 100. */
fun EventTarget.percentfield(property: Property<Double>, max: Double = 1.0, op: TextField.() -> Unit = {}) = textfield(property.apply { this.value = this.value.coerceIn(0.0..max) }, PercentageStringConverter() as StringConverter<Double>, op).apply {
    alignment = Pos.CENTER_RIGHT

    format {
        val number = it.controlNewText.removeSuffix("%")
        val double: Double? = if (number.isDouble()) number.toDouble() else null

        if (!it.controlNewText.endsWith("%") || (double == null && number.isNotBlank()) || (double != null && double !in 0.0..max * 100)) {
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

@Suppress("UNCHECKED_CAST")
/** Adds a custom text field that only accepts integers and shows a suffix. */
fun <T : Number> EventTarget.pixelfield(property: Property<T>, op: TextField.() -> Unit = {}) = textfield(property as Property<Number>, SuffixConverter("pixels"), op).apply {
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
        if (it == null || it.removeSuffix(" pixels").isBlank()) {
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

private const val TIME_MS_BEFORE_FONT_PICKER_AUTO_COMPLETE_RESET: Long = 500

/** Adds a font picker combobox that allows for easy navigation using keyboard inputs and renders each listed font with its actual font family. */
fun EventTarget.fontpicker(property: Property<String>, fonts: List<String>? = Font.getFamilies(), op: ComboBox<String>.() -> Unit = {}) = combobox(property, fonts) {
    required()

    // This increases performance. Without this, opening the combobox for the first time takes over a second.
    properties["comboBoxRowsToMeasureWidth"] = 0

    // Show the font in its own font family
    setCellFactory {
        return@setCellFactory object: ListCell<String>() {
            override fun updateItem(item: String?, empty: Boolean) {
                super.updateItem(item, empty)

                prefWidth = 300.0

                if (empty || item == null) {
                    text = null
                    graphic = null
                } else {
                    text = item
                    font = Font.font(item)
                }
            }
        }
    }

    // Auto complete
    var searchString = ""
    var lastInput: Instant = Instant.MIN

    setOnKeyTyped { keyEvent ->
        val now = Instant.now()

        if (lastInput <= now.minusMillis(TIME_MS_BEFORE_FONT_PICKER_AUTO_COMPLETE_RESET)) {
            searchString = ""
        }

        lastInput = now

        if (this.isShowing) {
            searchString += keyEvent.character
            this.items.find { it.toLowerCase().startsWith(searchString.toLowerCase()) }.also {
                if (it != null) {
                    this.selectionModel.select(it)
                    (this.skin as ComboBoxListViewSkin<String>).listView.scrollTo(it)
                } else {
                    searchString = ""
                }
            }
        }
    }

    op(this)
}

fun Node.popup(op: VBox.(popup: Popup) -> Unit = {}): Popup = Popup().apply {
    isAutoHide = true
    isAutoFix = false
    anchorLocation = PopupWindow.AnchorLocation.CONTENT_BOTTOM_LEFT
    val popup = this

    content.add(VBox().apply {
        // initializeStyle()
        spacing = 7.5
        useMaxSize = true
        addClass(WriterViewStylesheet.popupBox)
        op(this, popup)
    })
}

/**
 * Creates a new [Popup] that will automatically be shown/hidden when the anchor node receives a click event.
 *
 * @param fadeDurationMs The number of milliseconds the popup should be faded in or out for. If this popup should not receive a [FadeTransition], specify `0.0`. Note that the transition will not be applied if the user clicks outside the popup and [Popup.autoHide] is on.
 * @param offsetX The horizontal offset of this popup to its anchor.
 * @param offsetY The vertical offset of this popup to its anchor.
 */
fun Node.popupOnClick(fadeDurationMs: Number = 0.0, offsetX: Number = 0.0, offsetY: Number = 0.0, op: VBox.(popup: Popup) -> Unit = {}): Popup {
    return popup { popup ->
        val fadeEnabled = fadeDurationMs.toDouble() > 0.0
        val shouldFadeInProperty = SimpleBooleanProperty(false)

        if (fadeEnabled) { fadeWhen(shouldFadeInProperty, fadeDurationMs) }

        this@popupOnClick.setOnMouseClicked {
            if (popup.isShowing) {
                if (fadeEnabled) { shouldFadeInProperty.set(false) }
                runLater(fadeDurationMs.millis) { popup.hide() }
            } else {
                val anchorNodeBounds = this@popupOnClick.localToScreen(this@popupOnClick.boundsInLocal)
                val anchorX = anchorNodeBounds.minX + offsetX.toDouble()
                val anchorY = anchorNodeBounds.minY + offsetY.toDouble()

                popup.show(this@popupOnClick, anchorX, anchorY)
                if (fadeEnabled) { shouldFadeInProperty.set(true) }
            }
        }

        op(this, popup)
    }
}

fun EventTarget.svgbutton(svg: SVG, hint: String, addClass: Boolean = true, op: Button.() -> Unit = {}): Button {
    return button(graphic = svg) {
        addClass(GeneralStylesheet.plainButton)
        if (addClass) {
            addClass(ThemedStylesheet.svgButton)
        }
        tooltip(hint)
        useMaxSize = true
        alignment = Pos.CENTER
        textAlignment = TextAlignment.CENTER

        op(this)
    }
}

fun SelectionHolder.s(selector: String) = select(selector) {}

/** Sets the style min, max, and pref heights of this element to the specified value. */
var PropertyHolder.height: Dimension<Dimension.LinearUnits>
    get() = if (this.prefHeight.value != 0.0) this.prefHeight else this.minHeight
    set(value) {
        this.minHeight = value
        this.prefHeight = value
        this.maxHeight = value
    }

/** Sets the style min, max, and pref widths of this element to the specified value. */
var PropertyHolder.width: Dimension<Dimension.LinearUnits>
    get() = if (this.prefWidth.value != 0.0) this.prefWidth else this.minWidth
    set(value) {
        this.minWidth = value
        this.prefWidth = value
        this.maxWidth = value
    }

/** Represents a selector of style "`element *`". */
val Scoped.allDescendants
    get() = this contains star
