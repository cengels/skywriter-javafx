package com.cengels.skywriter.fragments

import tornadofx.*

/** Represents a [View] that returns a result when it is closed. */
abstract class Dialog<T>(title: String? = null) : View(title) {
    private var resultCallback: (Dialog<T>.() -> Unit)? = null

    /** If the form returns a concrete result like an edited or added object, this field contains it. */
    var result: T? = null
        protected set

    /** Whether the dialog contains a positive result. */
    var ok: Boolean = false
        protected set

    fun result(resultCallback: Dialog<T>.() -> Unit) {
        this.resultCallback = resultCallback
    }

    override fun onUndock() {
        super.onUndock()

        this.resultCallback?.invoke(this)
    }

    /** Closes the dialog with a positive result. */
    protected fun submit(result: T? = null) {
        ok = true
        if (result != null) {
            this.result = result
        }
        close()
    }

    /** Closes the dialog with a negative result and clears the [result] object. */
    protected fun cancel() {
        ok = false
        result = null
        close()
    }
}
