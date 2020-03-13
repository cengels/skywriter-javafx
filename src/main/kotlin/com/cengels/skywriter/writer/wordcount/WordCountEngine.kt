package com.cengels.skywriter.writer.wordcount

import com.cengels.skywriter.util.StyleClassedDocument
import com.cengels.skywriter.util.StyleClassedParagraph
import com.cengels.skywriter.util.regionMatches
import com.cengels.skywriter.util.toDigit
import org.fxmisc.richtext.model.StyledDocument
import kotlin.text.StringBuilder

/** A component responsible for counting words in a text document and structuring those word counts hierarchically. */
class WordCountEngine {
    inner class Behaviours {
        /** A list of class names corresponding to a [StyledDocument]'s style ranges. Any style ranges matching these class names will be excluded from the count. Default is an empty list. */
        var excludedStyles: Collection<String> = listOf()
        /** If true, words with different casing are considered not equal. If false, words with the same characters but different casing will be accumulated into a single [Word] entry and lower-cased. */
        var caseSensitive: Boolean = false
        /** If true, counts free-standing numbers in digit form (e.g. "there are 7 words in this string"), else ignores them. */
        var countNumbers: Boolean = false
        /** A list of word separators. If one of these tokens is found in the middle of a word, the words are considered separate. By default, it matches whitespace, commas, colons, semicolons, periods, newlines, and en- and em-dashes (in the form of `--` and `---` as well as `–` and `—`), but not hyphens. */
        var wordSeparators: Collection<String> = listOf(" ", "--", "---", "–", "—", ",", ".", "\n")
    }
    /** Allows configuration of the [WordCountEngine] by changing the way words are counted. */
    val behaviour = Behaviours()

    /** Counts the words in the specified [StyleClassedDocument] and builds a detailed word and section statistic around it. */
    fun count(document: StyleClassedDocument): Collection<Section> {
        val sections: MutableCollection<Section> = mutableListOf()

        var currentSection = ""
        var currentSectionLevel = 1
        var currentSectionWords: MutableMap<String, Int> = mutableMapOf()

        document.paragraphs.forEachIndexed { index, paragraph ->
            paragraph.paragraphStyle.find { it.length == 2 && it[0] == 'h' && it[1].isDigit() }?.let { headingStyle ->
                // Paragraph represents a new heading and therefore a new section
                if (index != 0) {
                    // index == 0 means that the first paragraph is a heading already
                    sections.add(Section(currentSection, currentSectionLevel, currentSectionWords.map { Word(it.key, it.value) }))
                }

                currentSection = paragraph.text
                currentSectionLevel = headingStyle[1].toDigit()
                currentSectionWords = mutableMapOf()
            }

            forEachWord(getIncludedStyleSpans(paragraph)) {
                currentSectionWords[it] = currentSectionWords.getOrDefault(it, 0) + 1
            }
        }

        sections.add(Section(currentSection, currentSectionLevel, currentSectionWords.map { Word(it.key, it.value) }))

        return sections
    }

    /** Counts the words in the specified [String] and builds a detailed word statistic around it. */
    fun count(text: String): Collection<Word> {
        val words: MutableMap<String, Int> = mutableMapOf()

        forEachWord(text) {
            words[it] = words.getOrDefault(it, 0) + 1
        }

        return words.map { Word(it.key, it.value) }
    }

    /** Sums the number of words without building any word statistics. This method is roughly 67% faster than [WordCountEngine.count()] and should be preferred whenever no detailed statistics are required. */
    fun sum(text: String): Int {
        var counter = 0

        forEachWord(text) { counter++ }

        return counter
    }

    /** Sums the number of words without building any word or section statistics. This method is roughly 67% faster than [WordCountEngine.count()] and should be preferred whenever no detailed statistics are required. */
    fun sum(document: StyleClassedDocument): Int {
        return sum(document.text)
    }

    private fun forEachWord(text: String, callback: (word: String) -> Unit) {
        val stringBuilder = StringBuilder()
        // Cache the functions to be used here to save a CPU cycle per char.
        val isValidChar = if (this.behaviour.countNumbers) { char: Char -> char.isLetterOrDigit() } else { char: Char -> char.isLetter() }
        val getString = if (this.behaviour.caseSensitive) { b: StringBuilder ->  b.toString() } else { b: StringBuilder -> b.toString().toLowerCase() }

        text.forEachIndexed { index, char ->
            if (isValidChar(char)) {
                stringBuilder.append(char)
            } else if (stringBuilder.isNotEmpty() && this.behaviour.wordSeparators.any { separator -> text.regionMatches(separator, index) }) {
                callback(getString(stringBuilder))
                stringBuilder.clear()
            }
        }

        if (stringBuilder.isNotEmpty()) {
            callback(getString(stringBuilder))
        }
    }

    private fun getIncludedStyleSpans(paragraph: StyleClassedParagraph): String {
        if (this.behaviour.excludedStyles.isEmpty()) {
            return paragraph.text
        }

        val stringBuilder = StringBuilder()

        paragraph.styledSegments.forEach { segment ->
            if (this.behaviour.excludedStyles.none { segment.style.contains(it) }) {
                stringBuilder.append(segment.segment)
            }
        }

        return stringBuilder.toString()
    }
}