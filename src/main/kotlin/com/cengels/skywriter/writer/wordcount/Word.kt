package com.cengels.skywriter.writer.wordcount

/** Represents a self-contained word. */
data class Word(
    /** The word itself. Guaranteed to be lowercase. */
    val text: String,
    /** The number of words of this type found in the section. */
    val count: Int
)