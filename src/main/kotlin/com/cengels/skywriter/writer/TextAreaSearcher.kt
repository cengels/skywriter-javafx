package com.cengels.skywriter.writer

import com.cengels.skywriter.util.replace
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.util.regex.PatternSyntaxException

/**
 * This component provides find and replace functionality to the [WriterTextArea].
 *
 * Changing the find term automatically looks for the new matches and updates the TextAreaSearcher accordingly.
 **/
class TextAreaSearcher(private val context: WriterTextArea) {
    companion object {
        /** Assembles a regex that can be used to find matches of the search term across a range of text. */
        fun assembleRegex(searchTerm: String, wholeWords: Boolean = false, caseSensitive: Boolean = false, useRegex: Boolean = false): Regex {
            if (searchTerm.isEmpty()) {
                throw IllegalArgumentException("searchTerm must be a non-empty String.")
            }

            val surrounding = if (wholeWords) "\\b+" else ""
            val searchString = if (useRegex) searchTerm else Regex.escape(searchTerm)
            val options = mutableSetOf<RegexOption>()

            if (!caseSensitive) {
                options.add(RegexOption.IGNORE_CASE)
            }

            return Regex("$surrounding$searchString$surrounding", options)
        }
    }

    val findTermProperty = SimpleStringProperty("")
    val findTerm: String by findTermProperty
    val replaceTermProperty = SimpleStringProperty("")
    val replaceTerm: String by replaceTermProperty
    val findWholeWordsProperty = SimpleBooleanProperty(false)
    /** Describes whether the search matches only whole "words", i.e. any matches must be surrounded by word boundaries. */
    val findWholeWords: Boolean by findWholeWordsProperty
    val caseSensitiveProperty = SimpleBooleanProperty(false)
    /** Describes whether the case must be matched in addition to the search string. */
    val caseSensitive: Boolean by caseSensitiveProperty
    val useRegexProperty = SimpleBooleanProperty(false)
    /** If true, Regex tokens in the search term will not be escaped. If false, literal matching is possible without explicit escapes, but you cannot use Regex in the search term. */
    val useRegex: Boolean by useRegexProperty
    private val searchRegexBinding = findTermProperty.objectBinding(findWholeWordsProperty, caseSensitiveProperty, useRegexProperty) {
        if (it == null || it.isEmpty()) {
            return@objectBinding null
        }

        try {
            assembleRegex(it, this.findWholeWords, this.caseSensitive, this.useRegex)
        } catch (e: PatternSyntaxException) {
            null
        }
    }
    private val searchRegex: Regex? by searchRegexBinding
    private val matchesBinding = searchRegexBinding.objectBinding {
        it?.findAll(context.text).orEmpty().toList()
    }
    private val matches
        get() = matchesBinding.value.orEmpty()
    val countBinding = matchesBinding.integerBinding { it?.size ?: 0 }
    /** Gets the current number of matches. */
    val count: Int by countBinding
    private var matchIndex = 0

    init {
        context.plainTextChanges().subscribe {
            matchesBinding.invalidate()
        }

        matchesBinding.addListener { observable, oldValue, newValue ->
            matchIndex = 0

            runLater {
                context.clearStyle("search-highlighting")

                if (matches === newValue) {
                    highlightMatches()
                }
            }
        }
    }

    /** Highlights all matches of the search term in the document. */
    fun highlightMatches() {
        context.mergeStyles(matches.map { it.range.first..it.range.last + 1 }, "search-highlighting")
    }

    /** Scrolls the text area to the next occurrence of the search term. */
    fun scrollToNext() {
        getNextMatch()?.let {
            context.selectRange(it.range.first, it.range.last + 1)
            context.requestCenterCaret()
        }
    }

    /** Scrolls the text area to the previous occurrence of the search term. */
    fun scrollToPrevious() {
        getPreviousMatch()?.let {
            context.selectRange(it.range.first, it.range.last + 1)
            context.requestCenterCaret()
        }
    }

    /** Replaces the current match with the replace term. */
    fun replaceCurrent() {
        if (count == 0) {
            return
        }

        val match = matches[matchIndex]
        context.replaceText(match.range.first, match.range.last + 1, createReplacementString(match))
        context.requestCenterCaret()
        matchesBinding.invalidate()
    }

    /** Replaces all occurrences of the search term with the replace term. */
    fun replaceAll() {
        if (count == 0) {
            return
        }

        context.createMultiChange(matches.size).apply {
            matches.forEach {
                this.replaceText(it.range.first, it.range.last + 1, createReplacementString(it))
            }
            this.commit()
        }
        matchesBinding.invalidate()
    }

    private fun createReplacementString(match: MatchResult): String {
        if (useRegex) {
            return match.replace(replaceTerm)
        }

        return replaceTerm
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