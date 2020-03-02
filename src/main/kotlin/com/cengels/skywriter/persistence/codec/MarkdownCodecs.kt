package com.cengels.skywriter.persistence.codec

import com.cengels.skywriter.persistence.*
import com.cengels.skywriter.util.surround
import javafx.scene.input.DataFormat
import org.fxmisc.richtext.model.*
import java.io.BufferedReader
import java.io.BufferedWriter

object MarkdownCodecs {
    val DOCUMENT_CODEC = object : DocumentCodec<BufferedReader> {
        override val dataFormat: DataFormat by lazy { DataFormat("text/markdown") }

        override fun encode(writer: BufferedWriter, element: List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>>) {
            element.forEachIndexed { index, paragraph ->
                PARAGRAPH_CODEC.encode(writer, paragraph)

                if (index != element.lastIndex) {
                    repeat(2) { writer.newLine() }
                }
            }
        }

        override fun decode(input: BufferedReader): List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>> {
            var line: String? = input.readLine() ?: ""
            val paragraphs = mutableListOf<Paragraph<MutableCollection<String>, String, MutableCollection<String>>>()

            while (line != null) {
                var segmentText = ""
                var paragraphBreak = false

                while (line != null) {
                    if (line.trim().isNotEmpty()) {
                        if (paragraphBreak) {
                            break
                        }

                        segmentText += if (segmentText.isEmpty()) line else " $line"
                    } else if (paragraphBreak) {
                        line = input.readLine()
                        break
                    } else {
                        paragraphBreak = true
                    }

                    line = input.readLine()
                }

                paragraphs.add(PARAGRAPH_CODEC.decode(segmentText))
            }

            return paragraphs
        }
    }

    val PARAGRAPH_CODEC = object : ParagraphCodec<String> {
        override fun encode(writer: BufferedWriter, element: Paragraph<MutableCollection<String>, String, MutableCollection<String>>) {
            val hashCount: Int? = Character.getNumericValue(element.paragraphStyle.find { it.matches(Regex("h\\d")) }?.last() ?: '0')

            if (hashCount != null) {
                writer.append("#".repeat(hashCount))
            }

            SEGMENT_CODEC.encode(writer, element.styledSegments)
        }

        override fun decode(input: String): Paragraph<MutableCollection<String>, String, MutableCollection<String>> {
            val hashCount: Int = input.takeWhile { it == '#' }.length
            var segmentText = input.trimStart('#')
            val paragraphStyles: MutableList<String> = mutableListOf()

            if (hashCount != 0) {
                if (segmentText.startsWith(' ')) {
                    segmentText = segmentText.slice(1..segmentText.lastIndex)
                }

                paragraphStyles.add("h$hashCount")
            }

            val textSegments: List<StyledSegment<String, MutableCollection<String>>> = SEGMENT_CODEC.decode(segmentText)

            return Paragraph(paragraphStyles, SegmentOps.styledTextOps<MutableCollection<String>>(), textSegments)
        }
    }

    val SEGMENT_CODEC = object : SegmentCodec<String> {
        override fun encode(writer: BufferedWriter, element: List<StyledSegment<String, MutableCollection<String>>>) {
            element.forEachIndexed { index, it ->
                val escapedText: String = escape(it.segment)

                var text: String = escapedText

                MarkdownParser.TOKEN_MAP.entries.apply {
                    it.style.forEach {
                        this.find { entry -> entry.value == it }.apply {
                            if (this != null) {
                                text = text.surround(this.key)
                            }
                        }
                    }
                }

                if (index == 0 && text.startsWith('#')) {
                    text = text.replaceFirst("#", "\\#")
                }

                writer.write(text)
            }
        }

        override fun decode(input: String): List<StyledSegment<String, MutableCollection<String>>> {
            val segments: MutableList<StyledSegment<String, MutableCollection<String>>> = mutableListOf()
            var remainingString: String = input
            val openingTokens: MutableList<String> = mutableListOf()

            while (remainingString.isNotEmpty()) {
                val nextToken = findNextToken(remainingString)

                if (nextToken.first < 0) {
                    if (openingTokens.size > 0 && segments.size > 0) {
                        // unterminated token
                        // TODO: Not all test cases work with this, but it's not important enough to warrant putting more time into.
                        segments[segments.lastIndex] = StyledSegment(unescape("${segments.last().segment}${openingTokens.last()}$remainingString"), mutableListOf())
                        openingTokens.removeAt(openingTokens.lastIndex)

                        while (openingTokens.size > 0) {
                            val segment = segments.last()
                            val index: Int = segments.indexOf(segment)
                            segments[index - 1] = StyledSegment(unescape("${segments[index - 1].segment}${openingTokens.last()}${segment.segment}"), segments[index - 1].style)
                            segments.removeAt(index)
                            openingTokens.removeAt(openingTokens.lastIndex)
                        }
                    } else {
                        segments.add(StyledSegment(unescape(remainingString), mutableListOf()))
                    }

                    remainingString = ""
                } else if (openingTokens.isNotEmpty() && openingTokens.last() == nextToken.second) {
                    if (nextToken.first != 0) {
                        segments.add(StyledSegment(unescape(remainingString.slice(0 until nextToken.first)), openingTokens.map { MarkdownParser.TOKEN_MAP[it]!! }.toMutableSet()))
                    }

                    openingTokens.removeAt(openingTokens.size - 1)
                    remainingString = remainingString.slice(nextToken.first + nextToken.second.length until remainingString.length)
                } else if (nextToken.first == 0) {
                    openingTokens.add(nextToken.second)
                    remainingString = remainingString.slice(nextToken.second.length until remainingString.length)
                } else {
                    if (openingTokens.isEmpty()) {
                        segments.add(StyledSegment(unescape(remainingString.slice(0 until nextToken.first)), mutableListOf()))
                    } else {
                        segments.add(StyledSegment(unescape(remainingString.slice(0 until nextToken.first)), openingTokens.map { MarkdownParser.TOKEN_MAP[it]!! }.toMutableSet()))
                    }

                    openingTokens.add(nextToken.second)
                    remainingString = remainingString.slice(nextToken.first + nextToken.second.length until remainingString.length)
                }
            }

            if (segments.isEmpty()) {
                segments.add(StyledSegment("", mutableListOf()))
            }

            return segments
        }
    }
}

/** Tries to find the next Markdown token in the specified string and returns its index along with the token itself if found or null if not. */
private fun findNextToken(string: String): Pair<Int, String> {
    return MarkdownParser.TOKEN_MAP.keys.fold(Pair(-1, "")) { acc, token ->
        val index: Int = string.indexOf(token)

        if (index == -1) {
            return@fold acc
        }

        if ((index < acc.first || acc.first == -1) && !string.isEscaped(index)) {
            return@fold Pair(index, token)
        }

        return@fold acc
    }
}

/** Checks if the specified input string starts with a substring surrounded by delimiter and returns it as a StyledSegment. */
private fun getSegment(input: String, delimiter: String, className: String): StyledSegment<String, MutableCollection<String>>? {
    if (input.startsWith(delimiter)) {
        return StyledSegment(input.slice(delimiter.length until input.length).substringBefore(delimiter), mutableListOf(className))
    }

    return null
}

/** Escapes symbols reserved for Markdown from the specified String. */
private fun escape(string: String): String {
    return string.replace("\\", "\\\\")
        .replace("*", "\\*")
        .replace("_", "\\_")
}

/** Unescapes symbols reserved for Markdown from the specified String. */
private fun unescape(string: String): String {
    var result = string
    var index: Int = result.lastIndexOf(MarkdownParser.ESCAPE_CHARACTER)

    while (index != -1) {
        if (!string.isEscaped(index)) {
            result = result.removeRange(index..index)
        }

        index = result.slice(0 until index).lastIndexOf(MarkdownParser.ESCAPE_CHARACTER)
    }

    return result
}

/** Checks whether the character at the specified index is escaped. */
fun String.isEscaped(index: Int): Boolean {
    return this.slice(0 until index).takeLastWhile { it == MarkdownParser.ESCAPE_CHARACTER }.length % 2 == 1
}