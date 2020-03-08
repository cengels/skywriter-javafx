package com.cengels.skywriter.writer

import com.cengels.skywriter.enum.Heading
import com.cengels.skywriter.enum.TextSelectionMode
import com.cengels.skywriter.persistence.AppConfig
import com.cengels.skywriter.persistence.codec.DocumentCodec
import com.cengels.skywriter.persistence.codec.HtmlCodecs
import com.cengels.skywriter.persistence.codec.RtfCodecs
import com.cengels.skywriter.style.FormattingStylesheet
import com.cengels.skywriter.util.countWords
import com.cengels.skywriter.util.findWordBoundaries
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.concurrent.Task
import javafx.scene.Node
import javafx.scene.control.IndexRange
import javafx.scene.input.*
import org.fxmisc.flowless.Cell
import org.fxmisc.flowless.VirtualFlow
import org.fxmisc.richtext.NavigationActions
import org.fxmisc.richtext.StyleClassedTextArea
import org.fxmisc.richtext.model.*
import org.fxmisc.wellbehaved.event.EventPattern
import org.fxmisc.wellbehaved.event.InputMap
import org.fxmisc.wellbehaved.event.Nodes
import tornadofx.FX
import tornadofx.getValue
import tornadofx.runAsync
import tornadofx.ui
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.BreakIterator
import java.util.*

class WriterTextArea : StyleClassedTextArea() {
    val smartReplacer: SmartReplacer = SmartReplacer(SmartReplacer.DEFAULT_QUOTES_MAP, SmartReplacer.DEFAULT_SYMBOL_MAP)
    val searcher = TextAreaSearcher(this)
    var insertionStyle: MutableCollection<String>? = null
    val wordCountProperty = SimpleIntegerProperty(this.countWords())
    val wordCount by wordCountProperty
    val readyProperty = SimpleBooleanProperty(false)
    val ready by readyProperty
    var onInitialized: ((textArea: WriterTextArea) -> Unit)? = null
    var initialized: Boolean = false
        private set
    private var midChange: Boolean = false
    private val queue: Queue<() -> Unit> = LinkedList()
    private var textSelectionMode: TextSelectionMode = TextSelectionMode.None
    val document: EditableStyledDocument<MutableCollection<String>, String, MutableCollection<String>>
        get() = this.content
    var encoderCodec: DocumentCodec<Any>? = null
    var decoderCodecs: List<DocumentCodec<Any>> = listOf()
    private var centerCaretRequested: Boolean = false
    private val virtualFlow: VirtualFlow<Any, Cell<Any, Node>> = this.children.filterIsInstance<VirtualFlow<Any, Cell<Any, Node>>>().single()

    init {
        this.isWrapText = true

        encoderCodec = HtmlCodecs.DOCUMENT_CODEC
        // prefer HTML (the RTF codecs are imperfect)
        decoderCodecs = listOf(HtmlCodecs.DOCUMENT_CODEC, RtfCodecs.DOCUMENT_CODEC)

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

            if (!updatingStyles && (change.inserted.any { !it.isLetterOrDigit() } || change.removed.any { !it.isLetterOrDigit() })) {
                wordCountProperty.set(this.countWordsWithoutComments())
            }
        }

        paragraphs.sizeProperty().addListener { observable, oldValue, newValue ->
            runAsync {} ui {
                // dummy call to force a repaint and reapply padding to first and last paragraphs
                setParagraphStyle(currentParagraph, getParagraph(currentParagraph).paragraphStyle)
            }
        }

        // paragraphs.addListener { it: ListChangeListener.Change<out Paragraph<MutableCollection<String>, String, MutableCollection<String>>> ->
        //     insertText(text.lastIndex + 1, " ")
        //     deleteText(text.lastIndex - 1, text.lastIndex)
        // }

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

        Nodes.addInputMap(this, InputMap.sequence<KeyEvent>(
            InputMap.consume(EventPattern.keyPressed(KeyCode.END)) { event -> this.moveTo(this.text.lastIndex) },
            InputMap.consume(EventPattern.keyPressed(KeyCode.HOME)) { event -> this.moveTo(0) },
            InputMap.consume(EventPattern.keyPressed(KeyCode.BACK_SPACE, KeyCombination.CONTROL_DOWN)) { event -> this.deleteLastWord() },
            InputMap.consume(EventPattern.keyPressed(KeyCode.DELETE, KeyCombination.CONTROL_DOWN)) { event -> this.deleteNextWord() }
        ))

        this.setOnMousePressed {
            when {
                it.clickCount == 1 -> textSelectionMode = TextSelectionMode.Character
                it.clickCount == 2 -> textSelectionMode = TextSelectionMode.Word
                it.clickCount >= 3 -> textSelectionMode = TextSelectionMode.Line
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

        runAsync {} ui {
            initialized = true
            onInitialized?.invoke(this)
        }
    }

    fun reset() {
        this.undoManager.forgetHistory()
        this.insertionStyle = null
        midChange = false
        queue.clear()
        textSelectionMode = TextSelectionMode.None
    }

    /** Deletes the next word and only the next word, excluding the last space. */
    fun deleteNextWord() {
        val nextWordBoundary = getFollowingWordBreakIterator().next()
        deleteText(caretPosition, if (text[nextWordBoundary].isLetterOrDigit()) nextWordBoundary - 1 else nextWordBoundary)
    }

    /** Deletes the last word and only the last word, excluding the first space. */
    fun deleteLastWord() {
        val previousWordBoundary = getPrecedingWordBreakIterator().previous()
        deleteText(if (text[previousWordBoundary].isLetterOrDigit()) previousWordBoundary else previousWordBoundary + 1, caretPosition)
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
        return selectedText.countWords()
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
        setStyleSpans(start, getStyleSpans(start, end).mapStyles { style -> style.minus(className) })
    }

    fun addStyle(start: Int, end: Int, className: String) {
        setStyleSpans(start, getStyleSpans(start, end).mapStyles { style -> style.plus(className) })
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

    /** Vertically centers the caret in the viewport. */
    fun requestCenterCaret() {
        // this.showParagraphAtBottom(currentParagraph)
        // visible paragraphs will not be updated yet if this isn't run asynchronously
        centerCaretRequested = true
    }

    override fun layoutChildren() {
        super.layoutChildren()

        if (centerCaretRequested) {
            this.visibleParagraphs.suspendable().suspendWhile {
                this.caretSelectionBind.underlyingCaret.let { caret ->
                    val center = (scene ?: FX.primaryStage.scene).height / 2
                    virtualFlow.showAtOffset(currentParagraph, center - caret.boundsInLocal.maxY)
                }
            }
            centerCaretRequested = false
        }
    }

    /** Selects the specified range plus the words immediately surrounding the start and end points. */
    fun selectWords(anchorPosition: Int, caretPosition: Int) {
        val anchorWord = text.findWordBoundaries(anchorPosition)
        val caretWord = text.findWordBoundaries(caretPosition)

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

    override fun copy() {
        if (selection.length > 0) {
            val content = ClipboardContent()
            val encoderCodec = this.encoderCodec

            if (encoderCodec == null) {
                content.putString(selectedText)
            } else {
                val subDocument = subDocument(selection.start, selection.end)
                val byteOutputStream = ByteArrayOutputStream()
                try {
                    encoderCodec.encode(byteOutputStream.bufferedWriter(), subDocument.paragraphs)
                    content[encoderCodec.dataFormat] = byteOutputStream.toString("utf8")
                } catch (e: IOException) {
                    System.err.println("Codec error: Exception in encoding '" + encoderCodec.dataFormat + "':")
                    e.printStackTrace()
                }
            }

            Clipboard.getSystemClipboard().setContent(content)
        }
    }

    override fun paste() {
        val clipboard = Clipboard.getSystemClipboard()
        val decoderCodec = decoderCodecs.find { clipboard.hasContent(it.dataFormat) }

        if (decoderCodec != null) {
            val contents = clipboard.getContent(decoderCodec.dataFormat)
            var paragraphs: List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>> = listOf()

            try {
                paragraphs = decoderCodec.decode(contents)
            } catch (e: IOException) {
                System.err.println("Codec error: Failed to decode '" + decoderCodec.dataFormat + "':")
                e.printStackTrace()
            }

            if (paragraphs.isNotEmpty()) {
                replaceSelection(paragraphs)
            }
        } else if (clipboard.hasString()) {
            clipboard.string?.let { replaceSelection(it) }
        }
    }

    /** Replaces the contents of the text area with the specified list of paragraphs. */
    fun replaceSelection(paragraphs: List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>>) {
        ReadOnlyStyledDocumentBuilder<MutableCollection<String>, String, MutableCollection<String>>(segOps, mutableListOf()).apply {
            paragraphs.forEach { addParagraph(it.styledSegments, it.paragraphStyle) }
        }.build().apply {
            replaceSelection(this)
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
        return text.countWords()
    }

    /** Counts the number of words in the text area, minus any comments. */
    private fun countWordsWithoutComments(): Int {
        return getTextWithoutComments().countWords()
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

    private fun getPrecedingWordBreakIterator(at: Int = caretPosition): BreakIterator {
        return BreakIterator.getWordInstance().also {
            it.setText(text)
            it.preceding(at)
        }
    }

    private fun getFollowingWordBreakIterator(at: Int = caretPosition): BreakIterator {
        return BreakIterator.getWordInstance().also {
            it.setText(text)
            it.following(at)
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