package com.cengels.skywriter.writer

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.getValue
import tornadofx.integerBinding
import tornadofx.objectBinding

/**
 * This component provides find and replace functionality to the [WriterTextArea].
 *
 * Changing the find term automatically looks for the new matches and updates the TextAreaSearcher accordingly.
 **/
class TextAreaSearcher(private val context: WriterTextArea) {
    val findTermProperty = SimpleStringProperty("")
    val findTerm: String by findTermProperty
    val replaceTermProperty = SimpleStringProperty("")
    val replaceTerm: String by findTermProperty
    val findWholeWordsProperty = SimpleBooleanProperty(false)
    val findWholeWords: Boolean by findWholeWordsProperty
    val caseSensitiveProperty = SimpleBooleanProperty(false)
    val caseSensitive: Boolean by caseSensitiveProperty
    private val searchRegexBinding = findTermProperty.objectBinding(findWholeWordsProperty, caseSensitiveProperty) {
        val surrounding = if (findWholeWords) "\\b+" else ""
        val options = mutableSetOf<RegexOption>()

        if (!caseSensitive) {
            options.add(RegexOption.IGNORE_CASE)
        }

        Regex("$surrounding${it!!}$surrounding", options)
    }
    private val searchRegex by searchRegexBinding
    private val matchesBinding = searchRegexBinding.objectBinding {
        matchIndex = 0
        it?.findAll(context.text).orEmpty().toList()
    }
    private val matches
        get() = matchesBinding.value.orEmpty()
    val countBinding = matchesBinding.integerBinding { it?.size ?: 0 }
    /** Gets the current number of matches. */
    val count: Int by countBinding
    private var matchIndex = 0

    /** Scrolls the text area to the next occurrence of the search term. */
    fun scrollToNext() {
        getNextMatch()?.let {
            println("${it.range.first} to ${it.range.last + 1}")
            context.selectRange(it.range.first, it.range.last + 1)
            context.requestFollowCaret()
        }
    }

    /** Scrolls the text area to the previous occurrence of the search term. */
    fun scrollToPrevious() {
        getPreviousMatch()?.let {
            println("${it.range.first} to ${it.range.last + 1}")
            context.selectRange(it.range.first, it.range.last + 1)
            context.requestFollowCaret()
        }
    }

    /** Scrolls the text area to the next occurrence of the search term and replaces it with the replace term. */
    fun replaceNext() {

    }

    /** Scrolls the text area to the previous occurrence of the search term and replaces it with the replace term. */
    fun replacePrevious() {

    }

    /** Replaces all occurrences of the search term with the replace term. */
    fun replaceAll() {

    }

    private fun getNextMatch(): MatchResult? {
        if (this.matches.isEmpty()) {
            return null
        }

        this.matchIndex++

        if (this.matchIndex >= this.matches.size) {
            this.matchIndex = 0
        }

        return this.matches[this.matchIndex]
    }

    private fun getPreviousMatch(): MatchResult? {
        if (this.matches.isEmpty()) {
            return null
        }

        this.matchIndex--

        if (this.matchIndex < 0) {
            this.matchIndex = 0
        } else if (this.matchIndex >= this.matches.size) {
            this.matchIndex = this.matches.lastIndex
        }

        return this.matches[this.matchIndex]
    }
}