package com.cengels.skywriter.writer

import com.cengels.skywriter.enum.Heading
import com.cengels.skywriter.style.FormattingStylesheet
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.Event
import javafx.event.EventType
import javafx.scene.control.IndexRange
import javafx.scene.text.TextAlignment
import org.fxmisc.richtext.StyleClassedTextArea
import org.fxmisc.richtext.model.*
import tornadofx.select
import tornadofx.getValue
import tornadofx.setValue
import java.util.*

class WriterTextArea : StyleClassedTextArea() {
    var insertionStyle: MutableCollection<String>? = null
    val smartReplacer: SmartReplacer = SmartReplacer(SmartReplacer.DEFAULT_QUOTES_MAP, SmartReplacer.DEFAULT_SYMBOL_MAP)
    val readyProperty = SimpleBooleanProperty(false)
    var ready by readyProperty
        private set
    private var midChange: Boolean = false
    private val queue: Queue<() -> Unit> = LinkedList<() -> Unit>()

    init {
        this.plainTextChanges().subscribe { change ->
            if (change.inserted.isNotEmpty()) {
                midChange = true
                applyInsertionStyle(change)
            }
        }

        this.beingUpdatedProperty().addListener { observable, oldValue, newValue ->
            if (oldValue && !newValue) {
                if (midChange) {
                    midChange = false
                } else {
                    ready = true
                }
            } else {
                ready = false
            }
        }

        this.readyProperty.addListener { observable, oldValue, newValue ->
            if (!oldValue && newValue) {
                do {
                    val queuedElement = this.queue.poll()
                    queuedElement?.invoke()
                } while (queuedElement != null)
            }
        }

        smartReplacer.observe(this)
    }

    /** A safe version of [StyleClassedTextArea.replaceText()] that waits to replace text until the text area is done updating.. */
    fun replaceTextWhenReady(start: Int, end: Int, text: String) {
        this.queue.offer {
            super.replaceText(start, end, text)
            moveTo(caretPosition + 1)
        }
    }

    /** Gets the paragraph at the specified absolute character position. */
    fun getParagraphAt(characterPosition: Int): Paragraph<MutableCollection<String>, String, MutableCollection<String>> {
        return getParagraph(this.offsetToPosition(characterPosition, TwoDimensional.Bias.Backward).major)
    }

    private fun applyInsertionStyle(change: PlainTextChange) {
        if (insertionStyle != null) {
            val from = change.position
            val length = change.inserted.length

            val styles = getStyleAtPosition(from).toMutableList()

            insertionStyle!!.forEach {
                if (styles.contains(it)) {
                    styles.remove(it)
                } else {
                    styles.add(it)
                }
            }

            setStyle(from, from + length, styles)
            insertionStyle = null
        }
    }

    fun updateSelection(className: String) {
        val selection: IndexRange = this.selection
        this.toggleStyleClass(selection.start, selection.end, className)
    }

    fun isRangeStyled(start: Int, end: Int, className: String): Boolean {
        val styleSpans = getStyleSpans(start, end)

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

    /** If text is selected, styles the selected text with the specified class. Otherwise, starts a new segment with the specified style class. */
    fun activateStyle(className: String) {
        if (selection.length > 0) {
            updateSelection(className)
        } else {
            if (insertionStyle == null) {
                insertionStyle = mutableListOf(className)
            } else if (insertionStyle!!.contains(className)) {
                insertionStyle!!.remove(className)
            } else {
                insertionStyle!!.add(className)
            }
        }
    }

    /** Sets the currently selected paragraphs as the specified heading. */
    fun setHeading(heading: Heading?) {
        val range = this.getSelectedParagraphs()
        for (i in range.start..range.end) {
            val paragraph = this.getParagraph(i)
            val stylesWithoutHeading = paragraph.paragraphStyle.filter { !FormattingStylesheet.headings.values.contains(it) }.toMutableList()

            if (heading != null) {
                stylesWithoutHeading.add(FormattingStylesheet.headings[heading])
            }

            this.setParagraphStyle(i, stylesWithoutHeading)
        }
    }

    /** Gets the index range of selected paragraphs. If only one paragraph is selected, start and end will be the same. */
    fun getSelectedParagraphs(): IndexRange =
        IndexRange(this.caretSelectionBind.startParagraphIndex, this.caretSelectionBind.endParagraphIndex)

    /** Selects the sentence around the caret. */
    fun selectSentence() {
        throw NotImplementedError()
        val caretPosition = this.caretPosition
        val paragraph = this.currentParagraph
        val paragraphText = this.getParagraph(paragraph).text
//        this.selectRange()
    }

    fun toMarkdown() {
        println(this.document)
    }
}