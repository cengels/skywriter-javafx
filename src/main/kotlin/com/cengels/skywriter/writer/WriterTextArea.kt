package com.cengels.skywriter.writer

import com.cengels.skywriter.enum.Heading
import com.cengels.skywriter.enum.TextSelectionMode
import com.cengels.skywriter.persistence.AppConfig
import com.cengels.skywriter.style.FormattingStylesheet
import com.cengels.skywriter.util.findWordBoundaries
import com.cengels.skywriter.util.splitWords
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.concurrent.Task
import javafx.scene.control.IndexRange
import javafx.scene.input.KeyCode
import org.fxmisc.richtext.NavigationActions
import org.fxmisc.richtext.StyleClassedTextArea
import org.fxmisc.richtext.model.*
import tornadofx.getValue
import tornadofx.runAsync
import tornadofx.ui
import java.util.*

class WriterTextArea : StyleClassedTextArea() {
    var insertionStyle: MutableCollection<String>? = null
    val smartReplacer: SmartReplacer = SmartReplacer(SmartReplacer.DEFAULT_QUOTES_MAP, SmartReplacer.DEFAULT_SYMBOL_MAP)
    val wordCountProperty = SimpleIntegerProperty(this.countWords())
    val wordCount by wordCountProperty
    val readyProperty = SimpleBooleanProperty(false)
    val ready by readyProperty
    var onInitialized: ((textArea: WriterTextArea) -> Unit)? = null
    var initialized: Boolean = false
        private set
    private var midChange: Boolean = false
    private val queue: Queue<() -> Unit> = LinkedList<() -> Unit>()
    private var textSelectionMode: TextSelectionMode = TextSelectionMode.None

    init {
        this.isWrapText = true

        this.caretPositionProperty().addListener { _, _, _ ->
            this.requestFollowCaret()
        }

        this.plainTextChanges().subscribe { change ->
            var updatingStyles = false
            if (change.inserted.isNotEmpty()) {
                midChange = true

                applyInsertionStyle(change)

                if (AppConfig.commentTokens.any { change.inserted.contains(it.first) || change.inserted.contains(if (it.second.isBlank()) "\\n" else it.second) }) {
                    updatingStyles = true
                    updateStyles()
                }
            }

            if (!updatingStyles) {
                wordCountProperty.set(this.countWordsWithoutComments())
            }
        }

        this.beingUpdatedProperty().addListener { observable, oldValue, newValue ->
            if (oldValue && !newValue) {
                if (midChange) {
                    midChange = false
                } else {
                    readyProperty.set(true)
                }
            } else {
                readyProperty.set(false)
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

        this.setOnKeyReleased { event ->
            when (event.code) {
                KeyCode.END -> this.moveTo(this.text.lastIndex)
                KeyCode.HOME -> this.moveTo(0)
                else -> return@setOnKeyReleased
            }
        }

        this.setOnMousePressed {
            if (it.clickCount == 1) {
                textSelectionMode = TextSelectionMode.Character
            } else if (it.clickCount == 2) {
                textSelectionMode = TextSelectionMode.Word
            } else if (it.clickCount >= 3) {
                textSelectionMode = TextSelectionMode.Line
            }
        }

        this.setOnMouseReleased {
            textSelectionMode = TextSelectionMode.None
        }

        this.setOnNewSelectionDrag {
            // By default, RichTextFX has no special behaviour for double or triple click selections, so it's
            // manually implemented here.
            updateSelection(hit(it.x, it.y).insertionIndex)
        }

        this.setOnNewSelectionDragFinished { /* overridden so the selection doesn't change on mouse release */ }

        smartReplacer.observe(this)
    }

    /** Queues the specified action until after the document has completed all its queued changes and is ready to accept new ones. */
    fun whenReady(callback: () -> Unit) {
        if (this.ready) {
            callback()
        } else {
            this.queue.offer(callback)
        }
    }

    /** Gets the paragraph at the specified absolute character position. */
    fun getParagraphAt(characterPosition: Int): Paragraph<MutableCollection<String>, String, MutableCollection<String>> {
        return getParagraph(getParagraphIndexAt(characterPosition))
    }

    /** Gets the paragraph index at the specified absolute character position. */
    fun getParagraphIndexAt(characterPosition: Int): Int {
        return this.offsetToPosition(characterPosition, TwoDimensional.Bias.Backward).major
    }

    fun updateSelectionWith(className: String) {
        val selection: IndexRange = this.selection
        this.toggleStyleClass(selection.start, selection.end, className)
    }

    /** Counts the number of selected words in the text area. */
    fun countSelectedWords(): Int {
        return selectedText.splitWords().size
    }

    fun isRangeStyled(start: Int, end: Int, className: String): Boolean {
        val styleSpans = getStyleSpans(start, end)

        return styleSpans.all { span -> span.style.any { style -> style == className } }
    }

    fun toggleStyleClass(start: Int, end: Int, className: String) {
        if (this.isRangeStyled(start, end, className)) {
            clearStyle(start, end, className)
        } else {
            addStyle(start, end, className)
        }
    }

    fun clearStyle(start: Int, end: Int, className: String) {
        setStyleSpans(start, StyleSpansBuilder<MutableCollection<String>>().addAll(getStyleSpans(start, end).map { styleSpan ->
            StyleSpan<MutableCollection<String>>(styleSpan.style.filter {
                    style -> style != className
            }.toMutableList(), styleSpan.length)
        }).create())
    }

    fun addStyle(start: Int, end: Int, className: String) {
        setStyleSpans(start, StyleSpansBuilder<MutableCollection<String>>().addAll(getStyleSpans(start, end).map { styleSpan ->
            StyleSpan<MutableCollection<String>>(styleSpan.style.filter {
                    style -> style != className
            }.plus(className).toMutableList(), styleSpan.length)
        }).create())
    }

    /** If text is selected, styles the selected text with the specified class. Otherwise, starts a new segment with the specified style class. */
    fun activateStyle(className: String) {
        if (selection.length > 0) {
            updateSelectionWith(className)
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

    /** Selects the specified range plus the words immediately surrounding the start and end points. */
    fun selectWords(anchorPosition: Int, caretPosition: Int) {
        val anchorWord = text.findWordBoundaries(anchorPosition)
        val caretWord = text.findWordBoundaries(caretPosition)

        println("anchor at $anchorPosition finds $anchorWord AND caret at $caretPosition finds $caretWord")

        if (caretPosition > anchorPosition || anchorWord == caretWord) {
            selectRange(anchorWord.first, caretWord.last)
        } else {
            selectRange(anchorWord.last, caretWord.first)
        }
    }

    /** Selects the lines between the specified start and end points. */
    fun selectLines(anchorPosition: Int, caretPosition: Int) {
        if (caretPosition > anchorPosition) {
            val caretParagraph = getParagraphIndexAt(caretPosition)
            selectRange(getParagraphIndexAt(anchorPosition), 0, caretParagraph, getParagraphLength(caretParagraph))
        } else {
            val anchorParagraph = getParagraphIndexAt(anchorPosition)
            selectRange(anchorParagraph, getParagraphLength(anchorParagraph), getParagraphIndexAt(caretPosition), 0)
        }
    }

    /** Skims the text for any tokens that define a style range and applies the style. */
    private fun updateStyles(): Task<Unit> {
        return runAsync { } ui {
            clearStyle(0, text.lastIndex, "comment")
            AppConfig.commentTokens.forEach { token ->
                var startIndex = text.indexOf(token.first)

                while (startIndex != -1) {
                    var endIndex = text.indexOf(if (token.second.isBlank()) "\\n" else token.second, startIndex)
                    val found = endIndex != -1

                    if (endIndex == -1 && paragraphs.lastIndex == offsetToPosition(startIndex, TwoDimensional.Bias.Backward).major) {
                        endIndex = text.lastIndex
                    }

                    if (endIndex != -1) {
                        toggleStyleClass(startIndex, endIndex + 1, "comment")

                        if (caretPosition == endIndex + 1 && found) {
                            insertionStyle = mutableListOf("comment")
                        }
                    }

                    startIndex = text.indexOf(token.first, startIndex + 1)
                }
            }

            wordCountProperty.set(this.countWordsWithoutComments())
            initialized = true
            onInitialized?.invoke(this)
        }
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

    /** Counts the number of words in the text area. */
    private fun countWords(): Int {
        return text.splitWords().size
    }

    /** Counts the number of words in the text area, minus any comments. */
    private fun countWordsWithoutComments(): Int {
        return getTextWithoutComments().splitWords().size
    }

    private fun getTextWithoutComments(): String {
        var index = 0
        return getStyleSpans(0, text.lastIndex).fold("") { acc, styleSpan ->
            if (styleSpan.style.contains("comment")) {
                index += styleSpan.length
                acc
            } else {
                acc + getText(index, index + styleSpan.length).also {
                    index += styleSpan.length
                }
            }
        }
    }

    private fun updateSelection(hit: Int) {
        when (textSelectionMode) {
            TextSelectionMode.Character -> moveTo(hit, NavigationActions.SelectionPolicy.ADJUST)
            TextSelectionMode.Word -> selectWords(anchor, hit)
            TextSelectionMode.Line -> selectLines(anchor, hit)
            else -> return
        }
    }
}