package com.cengels.skywriter.writer

import com.cengels.skywriter.enum.Heading
import com.cengels.skywriter.style.FormattingStylesheet
import javafx.scene.control.IndexRange
import javafx.scene.text.TextAlignment
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

    /** Sets the alignment on the selected paragraphs. */
    fun setAlignment(textAlignment: TextAlignment) {
        val range = this.getSelectedParagraphs()
        for (i in range.start..range.end) {
            val paragraph = this.getParagraph(i)
            val stylesWithoutAlignment = paragraph.paragraphStyle.filter { !FormattingStylesheet.alignments.values.contains(it) }.toMutableList()

            if (textAlignment != TextAlignment.LEFT) {
                stylesWithoutAlignment.add(FormattingStylesheet.alignments[textAlignment])
            }

            this.setParagraphStyle(i, stylesWithoutAlignment)
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
}