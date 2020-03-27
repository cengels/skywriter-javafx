package com.cengels.skywriter.writer.area

import com.cengels.skywriter.util.length
import com.cengels.skywriter.util.minusAll
import com.cengels.skywriter.util.plusDistinct
import javafx.scene.control.IndexRange
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder

fun WriterTextArea.updateSelectionWith(className: String) {
    val selection: IndexRange = this.selection
    this.toggleStyleClass(selection.start, selection.end, className)
}

fun WriterTextArea.isRangeStyled(start: Int, end: Int, className: String): Boolean {
    val styleSpans = getStyleSpans(start, end)

    return styleSpans.all { span -> span.style.any { style -> style == className } }
}

/** Maintains all styles in the given range and adds or removes the given class from the range. */
fun WriterTextArea.toggleStyleClass(start: Int, end: Int, className: String) {
    if (isRangeStyled(start, end, className)) {
        clearStyle(start, end, className)
    } else {
        addStyle(start, end, className)
    }
}

/** Clears all styles of the given class from the document. */
fun WriterTextArea.clearStyle(className: String) {
    suspendUndo { setStyleSpans(0, getStyleSpans(0, text.lastIndex).mapStyles { style -> style.minusAll(className) }) }
}

/** Clears all styles of the given class from the given range. */
fun WriterTextArea.clearStyle(start: Int, end: Int, className: String) {
    setStyleSpans(start, getStyleSpans(start, end).mapStyles { style -> style.minusAll(className) })
}

/** Adds the style of the given class to the given range, maintaining all pre-existing styles. */
fun WriterTextArea.addStyle(start: Int, end: Int, className: String) {
    setStyleSpans(start, getStyleSpans(start, end).mapStyles { style -> style.plusDistinct(className) })
}

/** Merges the style spans in the given range with the given style spans by adding each className from the given style spans to the current style spans in a union. */
fun WriterTextArea.unionStyles(start: Int, end: Int, styleSpans: StyleSpans<MutableCollection<String>>) {
    suspendUndo {
        setStyleSpans(start, getStyleSpans(start, end).overlay(styleSpans) { first, second ->
            return@overlay first.plus(second)
        })
    }
}

/** Merges the style spans in the given range with the given style spans by subtracting each className from the given style spans to the current style spans in an exclusion. */
fun WriterTextArea.excludeStyles(start: Int, end: Int, styleSpans: StyleSpans<MutableCollection<String>>) {
    suspendUndo {
        setStyleSpans(start, getStyleSpans(start, end).overlay(styleSpans) { first, second ->
            return@overlay first.minus(second)
        })
    }
}

/** Adds the given style class to each style span in the given ranges. This allows you to update multiple parts of the document in one pass. */
fun WriterTextArea.mergeStyles(ranges: List<IntRange>, className: String) {
    if (ranges.isEmpty()) {
        return
    }

    val styleSpans = createStyleSpans(ranges, className)
    unionStyles(ranges.first().first, ranges.last().last, styleSpans)
}

/** Clears the given style class from each style span in the given ranges. This allows you to update multiple parts of the document in one pass. */
fun WriterTextArea.clearStyles(ranges: List<IntRange>, className: String) {
    if (ranges.isEmpty()) {
        return
    }

    val styleSpans = createStyleSpans(ranges, className)
    excludeStyles(ranges.first().first, ranges.last().last, styleSpans)
}

/** If text is selected, styles the selected text with the specified class. Otherwise, starts a new segment with the specified style class. */
fun WriterTextArea.activateStyle(className: String) {
    if (selection.length > 0) {
        updateSelectionWith(className)
    } else {
        textInsertionStyle.let {
            val styleAtPosition = getStyleAtPosition(caretPosition)
            textInsertionStyle = when {
                it == null -> if (styleAtPosition.contains(className))
                    styleAtPosition.minus(className)
                else styleAtPosition.plus(className)
                it.contains(className) -> it.minus(className)
                else -> it.plus(className)
            }
        }
    }
}

/** Highlights all occurrences of the specified string in the document. */
fun WriterTextArea.highlight(searchString: String, className: String, wholeWords: Boolean = false, caseSensitive: Boolean = false) {
    val matches = TextAreaSearcher.assembleRegex(
            searchString,
            wholeWords,
            caseSensitive,
            useRegex = false
        )
        .findAll(this.text)

    mergeStyles(matches.map { it.range.first..it.range.last + 1 }.toList(), className)
}

private fun createStyleSpans(ranges: List<IntRange>, className: String): StyleSpans<MutableCollection<String>> {
    return StyleSpansBuilder<MutableCollection<String>>().apply {
        ranges.forEachIndexed { index, it ->
            add(mutableListOf(className), it.length)

            if (ranges.size > index + 1) {
                val inBetween = ranges[index + 1].first - it.last

                if (inBetween > 0) {
                    add(mutableListOf(), ranges[index + 1].first - it.last)
                }
            }
        }
    }.create()
}