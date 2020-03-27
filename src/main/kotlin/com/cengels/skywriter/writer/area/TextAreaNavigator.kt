package com.cengels.skywriter.writer.area

import com.cengels.skywriter.enum.TextSelectionMode
import com.cengels.skywriter.util.StyleClassedParagraph
import com.cengels.skywriter.util.contains
import javafx.scene.control.IndexRange
import javafx.scene.input.*
import org.fxmisc.richtext.GenericStyledArea
import org.fxmisc.richtext.NavigationActions
import org.fxmisc.richtext.model.TwoDimensional
import org.fxmisc.wellbehaved.event.EventPattern
import org.fxmisc.wellbehaved.event.InputMap
import org.fxmisc.wellbehaved.event.Nodes
import java.text.BreakIterator
import kotlin.math.max
import kotlin.math.min

private var textSelectionMode: TextSelectionMode = TextSelectionMode.None
/** Describes the original index of the user's click when they hold the mouse down. */
private var hitOrigin: Int = -1

/** Initializes event handlers to handle common navigation actions. */
fun WriterTextArea.initializeNavigation() {
    Nodes.addInputMap(this, InputMap.sequence<KeyEvent>(
        InputMap.consume(EventPattern.keyPressed(KeyCode.END)) { event -> this.moveTo(this.text.lastIndex) },
        InputMap.consume(EventPattern.keyPressed(KeyCode.HOME)) { event -> this.moveTo(0) },
        InputMap.consume(EventPattern.keyPressed(KeyCode.BACK_SPACE, KeyCombination.CONTROL_DOWN)) { event -> this.deleteLastWord() },
        InputMap.consume(EventPattern.keyPressed(KeyCode.DELETE, KeyCombination.CONTROL_DOWN)) { event -> this.deleteNextWord() }
    ))

    Nodes.addInputMap(this, InputMap.sequence<MouseEvent>(
        InputMap.consume(EventPattern.mousePressed(MouseButton.PRIMARY)) { event ->

            when {
                event.clickCount == 1 -> {
                    moveTo(hit(event.x, event.y).insertionIndex)
                    textSelectionMode = TextSelectionMode.Character
                }
                event.clickCount == 2 -> {
                    selectWord()
                    textSelectionMode = TextSelectionMode.Word
                }
                event.clickCount >= 3 -> {
                    selectParagraph()
                    textSelectionMode = TextSelectionMode.Paragraph
                }
            }

            hitOrigin = caretPosition

            hideContextMenu()
        },
        InputMap.consume(EventPattern.mousePressed(MouseButton.SECONDARY)) { event ->
            val hit = hit(event.x, event.y).insertionIndex
            if (event.clickCount == 1 && hit !in this.selection) {
                moveTo(hit)
            }

            hideContextMenu()
        },
        InputMap.consume(EventPattern.mouseReleased(MouseButton.PRIMARY)) { event ->
            textSelectionMode = TextSelectionMode.None
            hitOrigin = -1
        }
    ))

    this.setOnNewSelectionDrag {
        // By default, RichTextFX has no special behaviour for double or triple click selections, so it's
        // manually implemented here.
        updateSelection(hit(it.x, it.y).insertionIndex)
    }

    this.setOnNewSelectionDragFinished { /* overridden so the selection doesn't change on mouse release */ }
}

/** Gets the paragraph at the specified absolute character position. */
fun WriterTextArea.getParagraphAt(characterPosition: Int): StyleClassedParagraph {
    return this.getParagraph(getParagraphIndexAt(characterPosition))
}

/** Gets the paragraph index at the specified absolute character position. */
fun WriterTextArea.getParagraphIndexAt(characterPosition: Int): Int {
    return this.offsetToPosition(characterPosition, TwoDimensional.Bias.Backward).major
}

/** Gets the index range of selected paragraphs. If only one paragraph is selected, start and end will be the same. */
fun WriterTextArea.getSelectedParagraphs(): IndexRange =
    IndexRange(this.caretSelectionBind.startParagraphIndex, this.caretSelectionBind.endParagraphIndex)

/** Selects the specified range plus the words immediately surrounding the start and end points. */
fun WriterTextArea.selectWords(range: IntRange) {
    val iterator = getWordBreakIterator()

    selectRange(max(iterator.preceding(range.first), 0), iterator.following(range.last - 1).let { if (it == -1) this.text.length else it })
}

/** Selects the paragraphs between the specified start and end points. */
fun WriterTextArea.selectParagraphs(range: IntRange) {
    val lastParagraph = getParagraphIndexAt(range.last)
    selectRange(getParagraphIndexAt(range.first), 0, lastParagraph, getParagraphLength(lastParagraph))
}

/** Deletes the next word and only the next word, excluding the last space. */
fun WriterTextArea.deleteNextWord() {
    val nextWordBoundary = getFollowingWordBreakIterator().next()
    deleteText(caretPosition, if (text[nextWordBoundary].isLetterOrDigit()) nextWordBoundary - 1 else nextWordBoundary)
}

/** Deletes the last word and only the last word, excluding the first space. */
fun WriterTextArea.deleteLastWord() {
    val previousWordBoundary = getPrecedingWordBreakIterator().previous()
    deleteText(if (text[previousWordBoundary].isLetterOrDigit()) previousWordBoundary else previousWordBoundary + 1, caretPosition)
}

private fun WriterTextArea.getPrecedingWordBreakIterator(at: Int = caretPosition): BreakIterator {
    return getWordBreakIterator().also {
        it.preceding(at)
    }
}

private fun WriterTextArea.getFollowingWordBreakIterator(at: Int = caretPosition): BreakIterator {
    return getWordBreakIterator().also {
        it.following(at)
    }
}

private fun WriterTextArea.updateSelection(hit: Int) {
    when (textSelectionMode) {
        TextSelectionMode.Character -> moveTo(hit, NavigationActions.SelectionPolicy.ADJUST)
        TextSelectionMode.Word -> selectWords(min(hit, hitOrigin)..max(hit, hitOrigin))
        TextSelectionMode.Paragraph -> selectParagraphs(min(hit, hitOrigin)..max(hit, hitOrigin))
        else -> return
    }
}

private fun WriterTextArea.getWordBreakIterator(): BreakIterator {
    return BreakIterator.getWordInstance().also {
        it.setText(text.replace('’', '\'').replace('‘', '\''))
    }
}