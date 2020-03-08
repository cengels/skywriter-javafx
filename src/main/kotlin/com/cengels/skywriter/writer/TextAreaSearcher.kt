package com.cengels.skywriter.writer

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

/**
 * This component provides find and replace functionality to the [WriterTextArea].
 *
 * Changing the find term automatically looks for the new matches and updates the TextAreaSearcher accordingly.
 **/
class TextAreaSearcher(private val context: WriterTextArea) {
    val findTermProperty = SimpleStringProperty("")
    val findTerm: String by findTermProperty
    val replaceTermProperty = SimpleStringProperty("")
    val replaceTerm: String by replaceTermProperty
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

        if (it == null || it.isEmpty()) {
            null
        } else {
            Regex("$surrounding$it$surrounding", options)
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

            runAsync { } ui {
                context.clearStyle("search-highlighting")

                if (matches === newValue) {
                    highlightMatches()
                }
            }
        }
    }

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
        context.replaceText(match.range.first, match.range.last + 1, replaceTerm)
        context.requestCenterCaret()
    }

    /** Replaces all occurrences of the search term with the replace term. */
    fun replaceAll() {
        if (count == 0) {
            return
        }

        context.createMultiChange(matches.size).apply {
            matches.forEach {
                this.replaceText(it.range.first, it.range.last + 1, replaceTerm)
            }
            this.commit()
        }
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