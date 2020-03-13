package com.cengels.skywriter.writer.wordcount

/** Represents a section of text (started by a new heading). */
data class Section(
    /** The heading of this section of text. */
    val heading: String,
    /** The level of heading, from 1 to 6. */
    val level: Int,
    /** A collection of unique words found in this section, including the heading. */
    val words: Collection<Word>
) {
    /** The total count of words in this section. This includes the heading itself. */
    val wordCount: Int
        get() = words.sumBy { it.count }
}