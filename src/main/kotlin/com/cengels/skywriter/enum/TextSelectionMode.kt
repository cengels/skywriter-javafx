package com.cengels.skywriter.enum

enum class TextSelectionMode {
    None,
    /** The user has clicked once. The selection increments by single characters. */
    Character,
    /** The user has clicked twice. The selection increments by whole words. */
    Word,
    /** The user has clicked three times. The selection increments by paragraphs. */
    Paragraph
}