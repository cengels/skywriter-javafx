package com.cengels.skywriter.util

/** Counts the number of words in the [String]. */
fun String.countWords(): Int {
    return this.split(Regex("\\b\\S+\\b")).size
}

fun String.surround(with: String): String = "$with$this$with"

/** Splits the string at the specified exclusive positions. */
fun String.split(vararg at: Int): Collection<String> {
    if (at.isEmpty()) {
        return mutableListOf(this)
    }

    val splitString: MutableCollection<String> = mutableListOf(this.slice(0 until at.first()))

    if (at.size > 1) {
        (0 until at.size - 1).mapTo(splitString) { this.slice(at[it]..at[it + 1]) }
    }

    splitString.add(this.slice(at.last() until this.length))

    return splitString
}