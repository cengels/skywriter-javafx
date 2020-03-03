package com.cengels.skywriter.persistence.codec

import com.cengels.skywriter.enum.RtfCommand
import com.cengels.skywriter.persistence.PlainTextCodec
import com.cengels.skywriter.util.remove
import javafx.scene.input.DataFormat
import org.fxmisc.richtext.model.Paragraph
import org.fxmisc.richtext.model.StyledSegment
import java.io.*

object RtfCodecs {
    private val BRACES_MATCHER = Regex("\\{.*?}")

    val DOCUMENT_CODEC = object : DocumentCodec<Any> {
        override val dataFormat: DataFormat = DataFormat.RTF

        override fun encode(writer: BufferedWriter, element: List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>>) {
            element.forEach { PARAGRAPH_CODEC.encode(writer, it) }

            writer.close()
        }

        override fun decode(input: Any): List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>> {
            if (input !is String) {
                throw IllegalArgumentException("An RTF codec can only handle an input of type String, but input was of type ${input::class.simpleName}")
            }

            val cleanedInput = cleanRtfString(input)

            TODO("not implemented")
        }
    }

    val PARAGRAPH_CODEC = object :
        PlainTextCodec<Paragraph<MutableCollection<String>, String, MutableCollection<String>>, String> {
        override fun encode(writer: BufferedWriter, element: Paragraph<MutableCollection<String>, String, MutableCollection<String>>) {
            TODO("not implemented")
        }

        override fun decode(input: String): Paragraph<MutableCollection<String>, String, MutableCollection<String>> {
            TODO("not implemented")
        }
    }

    val SEGMENT_CODEC = object : PlainTextCodec<StyledSegment<String, MutableCollection<String>>, String> {
        override fun encode(writer: BufferedWriter, element: StyledSegment<String, MutableCollection<String>>) {
            TODO("not implemented")
        }

        override fun decode(input: String): StyledSegment<String, MutableCollection<String>> {
            TODO("not implemented")
        }
    }

    /** Cleans a proper RTF input string by stripping it of any useless information enclosed in braces. */
    private fun cleanRtfString(input: String): String {
        return BRACES_MATCHER.replace(input, "")
            .remove("{", "}", "\n", "\r", "\t", " ")
    }

    private fun findNextCommand(input: String): Pair<RtfCommand?, String> {
        var index = -1

        do {
            index = input.indexOf('\\', index + 1)
        } while (input[index + 1] == '\\' && input[index - 1] == '\\')

        if (index == -1) {
            return null to input
        }

        val command = input.substring(index + 1).takeWhile { it != ' ' && it != '\\' }

        return RtfCommand.values().find { it.name.toLowerCase() == command } to removeCommand(input, index, command.length)
    }

    private fun removeCommand(input: String, index: Int, length: Int): String {
        val isSpaceSuffixed = input[index + length + 1] == ' '
        input.replaceRange(index..index + length + (if (isSpaceSuffixed) 1 else 0), "")
    }
}