package com.cengels.skywriter.writer

import javafx.scene.control.IndexRange
import org.fxmisc.richtext.StyleClassedTextArea
import org.fxmisc.richtext.model.StyleSpan
import org.fxmisc.richtext.model.StyleSpansBuilder

class WriterTextArea : StyleClassedTextArea() {
    fun updateSelection(className: String) {
        val selection: IndexRange = this.selection
        this.toggleStyleClass(selection.start, selection.end, className)
    }

    fun isRangeStyled(start: Int, end: Int, className: String): Boolean {
        val styleSpans = getStyleSpans(start, end)
        println(getStyleSpans(start, end))

        return styleSpans.all { span -> span.style.any { style -> style == className } }
    }

    fun toggleStyleClass(start: Int, end: Int, className: String) {
        val styleSpans = this.getStyleSpans(start, end)

        if (this.isRangeStyled(start, end, className)) {
            setStyleSpans(start, StyleSpansBuilder<MutableCollection<String>>().addAll(styleSpans.map {
                styleSpan -> StyleSpan<MutableCollection<String>>(styleSpan.style.filter {
                    style -> style != className
                }.toMutableList(), styleSpan.length)
            }).create())
        } else {
            setStyleSpans(start, StyleSpansBuilder<MutableCollection<String>>().addAll(styleSpans.map {
                styleSpan -> StyleSpan<MutableCollection<String>>(styleSpan.style.filter {
                    style -> style != className
                }.plus(className).toMutableList(), styleSpan.length)
            }).create())
        }
    }
}