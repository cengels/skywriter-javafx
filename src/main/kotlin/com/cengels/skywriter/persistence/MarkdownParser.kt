package com.cengels.skywriter.persistence

import org.fxmisc.richtext.model.*
import java.io.*


class MarkdownParser(val document: StyledDocument<MutableCollection<String>, String, MutableCollection<String>>) {
    companion object {
        var segOps: SegmentOps<String, MutableCollection<String>>? = null

        val TOKEN_MAP: Map<String, String> = mapOf(
            Pair("**", "bold"),
            Pair("__", "bold"),
            Pair("*", "italic"),
            Pair("_", "italic")
        )

        val DOCUMENT_CODEC = object: PlainTextCodec<StyledDocument<MutableCollection<String>, String, MutableCollection<String>>, BufferedReader> {
            override fun encode(writer: BufferedWriter, element: StyledDocument<MutableCollection<String>, String, MutableCollection<String>>) {
                element.paragraphs.forEach { paragraph ->
                    PARAGRAPH_CODEC.encode(writer, paragraph.paragraphStyle)
                    SEGMENT_CODEC.encode(writer, paragraph.styledSegments)
                }
            }

            override fun decode(input: BufferedReader): StyledDocument<MutableCollection<String>, String, MutableCollection<String>> {
                var line: String? = input.readLine()
                val document: EditableStyledDocument<MutableCollection<String>, String, MutableCollection<String>> = SimpleEditableStyledDocument(
                    mutableListOf(), mutableListOf())

                while (line != null) {
                    val paragraphStyles: MutableCollection<String> = PARAGRAPH_CODEC.decode(line)
                    val textSegments: List<StyledSegment<String, MutableCollection<String>>> = SEGMENT_CODEC.decode(line)

                    document.paragraphs.add(Paragraph(paragraphStyles, segOps, textSegments))
                    line = input.readLine()
                }

                return document
            }
        }

        val PARAGRAPH_CODEC = object: PlainTextCodec<MutableCollection<String>, String> {
            override fun encode(writer: BufferedWriter, element: MutableCollection<String>) {
                val hashCount: Int? = element.find { it.matches(Regex("h\\d")) }?.last()?.toInt()

                if (hashCount != null) {
                    writer.append("#".repeat(hashCount))
                }
            }

            override fun decode(input: String): MutableCollection<String> {
                val hashCount: Int = input.takeWhile { it == '#' }.length

                if (hashCount != 0) {
                    return mutableListOf("h$hashCount")
                }

                return mutableListOf()
            }
        }

        val SEGMENT_CODEC = object: PlainTextCodec<List<StyledSegment<String, MutableCollection<String>>>, String> {
            override fun encode(writer: BufferedWriter, element: List<StyledSegment<String, MutableCollection<String>>>) {
                element.forEach {
                    val escapedText: String = escape(it.segment)

                    var text: String = escapedText

                    if (it.style.contains("italic")) {
                        text = text.surround("*")
                    }

                    if (it.style.contains("bold")) {
                        text = text.surround("**")
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
                        segments.add(StyledSegment(remainingString, mutableListOf()))
                        remainingString = ""
                    } else if (openingTokens.isNotEmpty() && openingTokens.last() == nextToken.second) {
                        if (nextToken.first != 0) {
                            segments.add(StyledSegment(remainingString.slice(0 until nextToken.first), openingTokens.map { TOKEN_MAP[it]!! }.toMutableSet()))
                        }

                        openingTokens.removeAt(openingTokens.size - 1)
                        remainingString = remainingString.slice(nextToken.first + nextToken.second.length until remainingString.length)
                    } else if (nextToken.first == 0) {
                        openingTokens.add(nextToken.second)
                        remainingString = remainingString.slice(nextToken.second.length until remainingString.length)
                    } else {
                        if (openingTokens.isEmpty()) {
                            segments.add(StyledSegment(remainingString.slice(0 until nextToken.first), mutableListOf()))
                        } else {
                            segments.add(StyledSegment(remainingString.slice(0 until nextToken.first), openingTokens.map { TOKEN_MAP[it]!! }.toMutableSet()))
                        }

                        openingTokens.add(nextToken.second)
                        remainingString = remainingString.slice(nextToken.first + nextToken.second.length until remainingString.length)
                    }
                }

                return segments
            }
        }
    }

    fun save(file: File) {
        try {
            val fileOutputStream = FileOutputStream(file)
            val bufferedWriter = BufferedWriter(OutputStreamWriter(fileOutputStream, "UTF-8"))
            DOCUMENT_CODEC.encode(bufferedWriter, this.document)
            bufferedWriter.close()
            fileOutputStream.close()
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
    }

    fun load(file: File, segmentOps: SegmentOps<String, MutableCollection<String>>) {
        try {
            val bufferedReader = BufferedReader(FileReader(file))
            segOps = segmentOps
            DOCUMENT_CODEC.decode(bufferedReader)
            bufferedReader.close()
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
    }
}
// *[I'd like to tell you ]**[something]**[, my friend.]
// [I don't know what though.]*[ That is unfortunate.]

/** Tries to find the next Markdown token in the specified string and returns its index along with the token itself if found or null if not. */
private fun findNextToken(string: String): Pair<Int, String> {
    return MarkdownParser.TOKEN_MAP.keys.fold(Pair(-1, "")) { acc, token ->
        val index: Int = string.indexOf(token)

        // TODO: Ignore escaped tokens
        if (index == -1) {
            return@fold acc
        }

        if (index < acc.first || acc.first == -1) {
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
    return string.replace("*", "\\*")
        .replace("_", "\\_")
        .replace("#", "\\#")
        .replace("\\", "\\\\")
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