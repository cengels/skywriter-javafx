package com.cengels.skywriter.util.convert

import javafx.beans.InvalidationListener
import javafx.beans.binding.Binding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import tornadofx.*

/** An observable property that automatically converts values to a source property and back. */
class ConverterProperty<TFrom, TTo>(val sourceProperty: Property<TFrom>, val converter: Converter<TFrom, TTo>) : ObjectProperty<TTo>() {
    private var bindingToSource: Binding<TFrom?>? = null
    private val bindingFromSource: Binding<TTo?>? = sourceProperty.objectBinding { if (it != null) converter.convert(it) else null }
    private val listeners: MutableMap<ChangeListener<in TTo>?, ChangeListener<in TFrom>?> = mutableMapOf()

    override fun getName(): String {
        return sourceProperty.name
    }

    override fun getBean(): Any? {
        return null
    }

    override fun addListener(listener: InvalidationListener?) {
        sourceProperty.addListener(listener)
    }

    override fun removeListener(listener: InvalidationListener?) {
        sourceProperty.removeListener(listener)
    }

    override fun addListener(listener: ChangeListener<in TTo>?) {
        if (listener != null) {
            val listener2: ChangeListener<in TFrom>? = ChangeListener { observable, oldValue, newValue -> listener.changed(bindingFromSource, converter.convert(oldValue), converter.convert(newValue)) }
            listeners[listener] = listener2
            sourceProperty.addListener(listener2)
        }
    }

    override fun removeListener(listener: ChangeListener<in TTo>?) {
        if (listener != null) {
            if (!listeners.containsKey(listener)) {
                throw IllegalArgumentException("Listener must be bound to source property.")
            }

            sourceProperty.removeListener(listeners[listener])
            listeners.remove(listener)
        }
    }

    override fun get(): TTo {
        return converter.convert(sourceProperty.value)
    }

    override fun set(value: TTo) {
        if (isBound) {
            throw RuntimeException("A bound value cannot be set.")
        }

        val convertedValue = converter.convertBack(value)
        if (sourceProperty.value != convertedValue) {
            sourceProperty.value = convertedValue
        }
    }

    override fun bind(observable: ObservableValue<out TTo>?) {
        if (observable == null) {
            throw NullPointerException("Cannot bind to null")
        }

        bindingToSource = observable.objectBinding { if (it != null) converter.convertBack(it) else null }
        sourceProperty.bind(bindingToSource)
    }

    override fun unbind() {
        if (isBound) {
            sourceProperty.unbind()
            bindingToSource = null
        }
    }

    override fun isBound(): Boolean {
        return bindingToSource != null
    }
}
