package com.cengels.skywriter.util

/** Counts the number of words in the [String]. */
fun String.countWords(): Int {
    return this.split(Regex("(\\s|\\n)+")).size
}