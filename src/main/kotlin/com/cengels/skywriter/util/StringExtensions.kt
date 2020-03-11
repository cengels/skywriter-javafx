package com.cengels.skywriter.util

import com.cengels.skywriter.persistence.codec.isEscaped

private val WORD_REGEX = Regex("\\b\\S+\\b")

/** Counts the number of words in the [String]. */
fun CharSequence.splitWords(): List<String> {
    return this.split(WORD_REGEX)
}

/** Counts the words by applying the regex `\b\S+\b`. */
fun CharSequence.countWords(): Int {
    return this.splitWords().size - 1
}

/** Surrounds the [String] with the specified text. */
fun CharSequence.surround(with: CharSequence): String = "$with$this$with"

/** Removes the specified substrings from the [String]. */
fun CharSequence.remove(vararg strings: CharSequence): String {
    return strings.fold(this) { acc, string -> acc.toString().replace(string.toString(), "") }.toString()
}

/** Finds the index of the specified unescaped character or returns -1 if no unescaped character is found. */
fun CharSequence.indexOfUnescaped(char: Char, startIndex: Int = 0): Int {
    var index: Int = startIndex - 1

    do {
        index = this.indexOf(char, index + 1)
    } while (this.isEscaped(index))

    return index
}

/** Finds the index of the specified unescaped substring or returns -1 if no unescaped substring is found. */
fun CharSequence.indexOfUnescaped(string: CharSequence, startIndex: Int = 0): Int {
    var index: Int = startIndex - 1

    do {
        index = this.indexOf(string.toString(), index + 1)
    } while (this.isEscaped(index))

    return index
}

/** Removes the specified substrings from the [String] only if they are not escaped (preceded by a backslash). If they are escaped, removes the escaping. */
fun CharSequence.removeUnescaped(vararg strings: CharSequence): String {
    return strings.fold(this) { acc, string ->
        var index = acc.indexOf(string.toString())
        var newString = acc

        while (index != -1) {
            var endIndex = index + string.length - 1
            if (newString.getOrNull(index - 1)?.equals('\\') != true) {
                // unescaped
                newString = newString.removeRange(index..endIndex)
                endIndex = -1
            } else {
                newString = newString.removeRange(index - 1 until index)
                endIndex -= 1
            }

            index = newString.indexOf(string.toString(), endIndex + 1)
        }

        newString
    }.toString()
}

/** Splits the string at the specified exclusive positions. */
fun CharSequence.split(vararg at: Int): Collection<String> {
    if (at.isEmpty()) {
        return mutableListOf(this.toString())
    }

    val splitString: MutableCollection<String> = mutableListOf(this.slice(0 until at.first()).toString())

    if (at.size > 1) {
        (0 until at.size - 1).mapTo(splitString) { this.slice(at[it]..at[it + 1]).toString() }
    }

    splitString.add(this.slice(at.last() until this.length).toString())

    return splitString
}

/** Finds the word boundaries at the specified index. */
fun CharSequence.findWordBoundaries(at: Int): IntRange {
    val splitString = this.split(at)
    val first = splitString.first().indexOfLast { c -> c == ' ' || c == '\n' || c == '\r' }
    val second = splitString.last().indexOfFirst { c -> c == ' ' || c == '\n' || c == '\r' }

    return kotlin.math.max(first + 1, 0)..if (second == -1) this.length else second + at
}

fun CharSequence.containsAny(vararg chars: Char): Boolean {
    return this.indexOfAny(chars) != -1
}
