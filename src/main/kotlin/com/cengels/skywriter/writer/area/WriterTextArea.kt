package com.cengels.skywriter.writer.area

import com.cengels.skywriter.enum.Heading
import com.cengels.skywriter.enum.TextSelectionMode
import com.cengels.skywriter.persistence.AppConfig
import com.cengels.skywriter.persistence.codec.DocumentCodec
import com.cengels.skywriter.persistence.codec.HtmlCodecs
import com.cengels.skywriter.persistence.codec.RtfCodecs
import com.cengels.skywriter.style.FormattingStylesheet
import com.cengels.skywriter.util.*
import com.cengels.skywriter.writer.wordcount.WordCountEngine
import javafx.beans.property.*
import javafx.concurrent.Task
import javafx.scene.Node
import javafx.scene.control.IndexRange
import javafx.scene.input.*
import org.fxmisc.flowless.Cell
import org.fxmisc.flowless.VirtualFlow
import org.fxmisc.richtext.NavigationActions
import org.fxmisc.richtext.StyleClassedTextArea
import org.fxmisc.richtext.model.*
import org.fxmisc.richtext.util.UndoUtils
import org.fxmisc.wellbehaved.event.EventPattern
import org.fxmisc.wellbehaved.event.InputMap
import org.fxmisc.wellbehaved.event.Nodes
import org.reactfx.SuspendableYes
import tornadofx.FX
import tornadofx.getValue
import tornadofx.onChangeOnce
import tornadofx.runLater
import tornadofx.ui
import tornadofx.runAsync
import tornadofx.finally
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.BreakIterator
import kotlin.math.max
import kotlin.math.min

class WriterTextArea : StyleClassedTextArea() {
    val smartReplacer: SmartReplacer = SmartReplacer(SmartReplacer.DEFAULT_QUOTES_MAP, SmartReplacer.DEFAULT_SYMBOL_MAP)
    val searcher = TextAreaSearcher(this)
    val wordCountProperty: ReadOnlyIntegerProperty = SimpleIntegerProperty(this.countWords())
    /** The current word count of the document. */
    val wordCount by wordCountProperty
    val readyProperty: ReadOnlyBooleanProperty = SimpleBooleanProperty(false)
    /** Whether the document is currently open to receive changes. Queue code by using whenReady(). */
    val ready by readyProperty
    val initializedProperty: ReadOnlyBooleanProperty = SimpleBooleanProperty(false)
    /** Whether the text area has finished initializing. */
    val initialized: Boolean by initializedProperty
    /** Alias for [content] with the appropriate generics. */
    val document: EditableStyleClassedDocument?
        get() = if (this.initialized) this.content else null
    private val wordCountEngine = WordCountEngine()
    private var encoderCodec: DocumentCodec<Any>? = null
    private var decoderCodecs: List<DocumentCodec<Any>> = listOf()
    private var midChange: Boolean = false
    private var centerCaretRequested: Boolean = false
    private val virtualFlow: VirtualFlow<Any, Cell<Any, Node>> = this.children.filterIsInstance<VirtualFlow<Any, Cell<Any, Node>>>().single()
    private val undoEnabled = SuspendableYes()

    init {
        this.isWrapText = true
        encoderCodec = HtmlCodecs.DOCUMENT_CODEC
        // prefer HTML (the RTF codecs are imperfect)
        decoderCodecs = listOf(HtmlCodecs.DOCUMENT_CODEC, RtfCodecs.DOCUMENT_CODEC)
        wordCountEngine.behaviour.excludedStyles = listOf("comment")
        undoManager = UndoUtils.richTextSuspendableUndoManager(this, undoEnabled)
        this.initializeNavigation()
        smartReplacer.observe(this)

        this.caretPositionProperty().addListener { _, _, _ ->
            if (this.initialized) {
                this.requestFollowCaret()
                this.textInsertionStyle = null

                if (caretPosition != 0 && AppConfig.commentTokens.any {
                        text.slice(caretPosition - it.second.length until caretPosition) == if (it.second.isBlank()) "\n" else it.second
                    }) {
                    textInsertionStyle = getStyleAtPosition(caretPosition).minus("comment")
                }
            }
        }

        this.plainTextChanges().subscribe { change ->
            midChange = true

            if (this.initialized) {
                (wordCountProperty as IntegerProperty).set(this.countWords())
            }

            updateComments(change).finally {
                midChange = false

                if (!this.isBeingUpdated) {
                    (readyProperty as BooleanProperty).set(true)
                }
            }
        }

        paragraphs.sizeProperty().addListener { observable, oldValue, newValue ->
            runLater {
                // dummy call to force a repaint and reapply padding to first and last paragraphs
                setParagraphStyle(currentParagraph, getParagraph(currentParagraph).paragraphStyle)
            }
        }

        this.beingUpdatedProperty().addListener { observable, oldValue, newValue ->
            if (this.initialized) {
                if (oldValue && !newValue) {
                    if (!midChange) {
                        (readyProperty as BooleanProperty).set(true)
                    }
                } else {
                    (readyProperty as BooleanProperty).set(false)
                }
            } else if (this.ready) {
                (readyProperty as BooleanProperty).set(false)
            }
        }

        this.readyProperty.addListener { observable, oldValue, newValue ->
            if (!this.initialized && newValue) {
                (initializedProperty as BooleanProperty).set(true)
            }
        }

        this.initializedProperty.addListener { observable, oldValue, newValue ->
            if (!oldValue && newValue) {
                (wordCountProperty as IntegerProperty).set(this.countWords())
            }
        }
    }

    /** Executes the given block with the [undoManager] ignoring any changes emitted during the execution. */
    fun suspendUndo(op: () -> Unit) {
        undoEnabled.suspendWhile { op() }
    }

    /** Resets any values that mutated since the creation of this text area back to their defaults, including the document itself. */
    fun reset() {
        (this.initializedProperty as BooleanProperty).set(false)
        this.undoManager.forgetHistory()
        textInsertionStyle = null
        midChange = false
    }

    /** Queues the specified call until after the document has completed all its queued changes and is ready to accept new ones. */
    fun whenReady(callback: () -> Unit) {
        if (this.ready) {
            callback()
        } else {
            this.readyProperty.onChangeOnce {
                if (it == true) {
                    callback()
                }
            }
        }
    }

    /** Queues the specified call until after the document has finished initializing. */
    fun whenInitialized(callback: (document: EditableStyleClassedDocument) -> Unit) {
        if (this.initialized) {
            callback(this.document!!)
        } else {
            this.initializedProperty.onChangeOnce {
                if (it == true) {
                    callback(this.document!!)
                }
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

    /** Vertically centers the caret in the viewport. */
    fun requestCenterCaret() {
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

    override fun selectWord() {
        selectWords(caretPosition..caretPosition)
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
            var paragraphs: List<StyleClassedParagraph> = listOf()

            try {
                paragraphs = decoderCodec.decode(contents)
            } catch (e: IOException) {
                System.err.println("Codec error: Failed to decode '" + decoderCodec.dataFormat + "':")
                e.printStackTrace()
            }

            if (paragraphs.isNotEmpty()) {
                replaceSelection(paragraphs)
            }
        } else {
            this.pasteUnformatted()
        }
    }

    fun pasteUnformatted() {
        val clipboard = Clipboard.getSystemClipboard()

        if (clipboard.hasString()) {
            clipboard.string?.let { replaceSelection(it) }
        }
    }

    /** Replaces the contents of the text area with the specified list of paragraphs. */
    fun replaceSelection(paragraphs: List<StyleClassedParagraph>) {
        ReadOnlyStyledDocumentBuilder<MutableCollection<String>, String, MutableCollection<String>>(segOps, mutableListOf()).apply {
            paragraphs.forEach { addParagraph(it.styledSegments, it.paragraphStyle) }
        }.build().apply {
            replaceSelection(this)
        }
    }

    /** Skims the text for any tokens that define a comment range and applies the style. */
    private fun updateComments(change: PlainTextChange): Task<Pair<List<IntRange>, List<IntRange>>> {
        return runAsync {
            val insertionIndices = mutableSetOf<IntRange>()
            val removalIndices = mutableSetOf<IntRange>()

            AppConfig.commentTokens.forEach { (startToken, endToken) ->
                val tokenList = if (endToken.isBlank()) listOf(startToken) else listOf(startToken, endToken)
                val matchTokenList = listOf(startToken, if (endToken.isBlank()) "\n" else endToken)

                if (change.inserted.isNotEmpty()) {
                    var match = change.inserted.findAnyOf(tokenList)

                    while (match != null) {
                        val (relativeIndex, token) = match
                        val absoluteIndex = relativeIndex + change.position

                        if (token == startToken) {
                            text.findAnyOf(matchTokenList, absoluteIndex + token.length)?.also { (endIndex, nextToken) ->
                                if (nextToken == matchTokenList.last()) {
                                    insertionIndices.add(absoluteIndex..endIndex + nextToken.length)
                                }
                            }
                        } else {
                            text.findLastAnyOf(matchTokenList, absoluteIndex - token.length)?.also { (startIndex, previousToken) ->
                                if (previousToken == matchTokenList.first()) {
                                    insertionIndices.add(startIndex..absoluteIndex + token.length)
                                }
                            }
                        }

                        match = change.inserted.findAnyOf(tokenList, relativeIndex + token.length)
                    }
                }

                if (change.removed.isNotEmpty()) {
                    val startTokenIndex = change.removed.indexOf(startToken)
                    val endTokenIndex = change.removed.indexOf(endToken)

                    if (startTokenIndex != -1) {
                        text.findAnyOf(tokenList, change.position)?.also {
                            if (it.second == endToken && text.findLastAnyOf(tokenList, change.position)?.second != startToken) {
                                removalIndices.add(change.position..it.first + it.second.length)
                            }
                        }
                    }

                    if (endTokenIndex != -1) {
                        text.findLastAnyOf(tokenList, change.position)?.also {
                            if (it.second == startToken && text.findAnyOf(tokenList, change.position)?.second != endToken) {
                                removalIndices.add(it.first..change.position)
                            }
                        }
                    }
                }
            }

            return@runAsync insertionIndices.toList() to removalIndices.toList()
        } ui { (insertionIndices, removalIndices) ->
            clearStyles(removalIndices, "comment")
            mergeStyles(insertionIndices, "comment")

            if (this.initialized) {
                (wordCountProperty as IntegerProperty).set(this.countWords())
            }
        }
    }

    /** Counts the number of selected words in the text area. */
    fun countSelectedWords(): Int {
        return this.countWords(this.selection)
    }

    private fun countWords(range: IndexRange): Int {
        return wordCountEngine.sum(this.subDocument(range))
    }

    /** Counts the number of words in the text area. */
    private fun countWords(): Int {
        if (this.text.isEmpty()) {
            return 0
        }

        return this.countWords(IndexRange(0, this.text.lastIndex))
    }
}
