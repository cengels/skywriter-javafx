package com.cengels.skywriter.writer

import org.fxmisc.richtext.model.PlainTextChange
import org.fxmisc.richtext.model.TwoDimensional
import java.util.*

/** Specifices methods to find and replace smart quotes and other typographic symbols. */
class SmartReplacer(
    /** A map of the "dumb" quotes to replace with smart quotes. */
    private val quotesMap: MutableMap<Char, Pair<Char, Char>> = mutableMapOf(),
    /** A map of the symbols to replace with the specified symbol. */
    private val symbolMap: SortedMap<String, String> = mutableMapOf<String, String>().toSortedMap()) {

    companion object {
        val DEFAULT_QUOTES_MAP = mutableMapOf('"' to ('“' to '”'), '\'' to ('‘' to '’'))
        val DEFAULT_SYMBOL_MAP = mutableMapOf("---" to "—", "--" to "–").toSortedMap(compareBy<String> { -it.length }.thenBy { it })
        private val SYMBOLS_BEFORE_OPENING_QUOTE = arrayOf(' ', '-', '–', '—')
    }

    /** Subscribes to changes made on the specified text area and automatically replaces the configured symbols with the specified ones. */
    fun observe(textArea: WriterTextArea) {
        textArea.plainTextChanges().subscribe { change ->
            apply(textArea, change)
        }
    }

    /** Defines a smart quote pair to replace the specified string with. The first pair member is used as an opening quote, the second one as the closing quote. */
    fun defineSmartQuote(replaceThis: Char, withThese: Pair<Char, Char>) {
        quotesMap[replaceThis] = withThese
    }

    /** Defines a symbol to replace the selected symbol whenever it is typed in text. */
    fun defineReplacement(replaceThis: String, withThis: String) {
        symbolMap[replaceThis] = withThis
    }

    fun deleteSmartQuote(forSymbol: Char) {
        quotesMap.remove(forSymbol)
    }

    fun deleteReplacement(forSymbol: String) {
        symbolMap.remove(forSymbol)
    }

    /** Applies the configured replacements in the given character range. */
    private fun apply(textArea: WriterTextArea, change: PlainTextChange) {
        if (change.inserted.isEmpty() || change.inserted.length > 1 || change.insertionEnd != textArea.caretPosition) {
            return
        }

        val insertedChar = change.inserted.single()
        val offset = textArea.offsetToPosition(change.position, TwoDimensional.Bias.Backward)
        val textBefore = textArea.getParagraph(offset.major).text.slice(0 until offset.minor)

        symbolMap.forEach { (replaceThis, withThis) ->
            if (textBefore.endsWith(replaceThis) && (replaceThis.any { it != replaceThis.first() } || insertedChar != replaceThis.first())) {
                textArea.whenReady {
                    textArea.replaceText(change.position - replaceThis.length, change.position, withThis)
                    textArea.moveTo(textArea.caretPosition + 1)
                }
                return@apply
            }
        }

        quotesMap.forEach { (replaceThis, withThis) ->
            if (replaceThis == insertedChar) {
                val replacer = if (textBefore.isEmpty() || SYMBOLS_BEFORE_OPENING_QUOTE.any { textBefore.endsWith(it) }) withThis.first else withThis.second

                textArea.whenReady { textArea.replaceText(change.position, change.insertionEnd, replacer.toString()) }
                return@apply
            }
        }
    }
}
