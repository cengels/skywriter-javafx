package com.cengels.skywriter.persistence.codec

import com.cengels.skywriter.enum.RtfCommand
import com.cengels.skywriter.style.FormattingStylesheet
import com.cengels.skywriter.util.containsAny
import com.cengels.skywriter.util.remove
import javafx.scene.input.DataFormat
import org.fxmisc.richtext.model.Paragraph
import org.fxmisc.richtext.model.SegmentOps
import org.fxmisc.richtext.model.StyledSegment
import java.io.*
import java.util.*

object RtfCodecs : CodecGroup<Any, Iterable<RtfCodecs.CommandSegment>> {
    private val BRACES_MATCHER = Regex("\\{.*?}")

    private val RTF_TO_STYLES = mapOf(
        RtfCommand.B to FormattingStylesheet.bold,
        RtfCommand.I to FormattingStylesheet.italic,
        RtfCommand.STRIKE to FormattingStylesheet.strikethrough
    )

    override val DOCUMENT_CODEC = object : DocumentCodec<Any> {
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
            val segments = findCommands(cleanedInput)
            val paragraphs = ArrayDeque<Paragraph<MutableCollection<String>, String, MutableCollection<String>>>()
            var currentSegments = ArrayDeque<CommandSegment>()

            segments.forEach {
                if (it.commands.containsAny(RtfCommand.LINE_TERMINATORS)) {
                    paragraphs.add(PARAGRAPH_CODEC.decode(currentSegments))
                    currentSegments = ArrayDeque()
                }

                currentSegments.add(it)
            }

            if (currentSegments.isNotEmpty()) {
                paragraphs.add(PARAGRAPH_CODEC.decode(currentSegments))
            }

            return paragraphs.toList()
        }
    }

    override val PARAGRAPH_CODEC = object : ParagraphCodec<Iterable<CommandSegment>> {
        override fun encode(writer: BufferedWriter, element: Paragraph<MutableCollection<String>, String, MutableCollection<String>>) {
            TODO("not implemented")
        }

        override fun decode(input: Iterable<CommandSegment>): Paragraph<MutableCollection<String>, String, MutableCollection<String>> {
            // Paragraph styles are currently not supported by this codec.

            return Paragraph(mutableListOf(), SegmentOps.styledTextOps(), SEGMENT_CODEC.decode(input))
        }
    }

    override val SEGMENT_CODEC = object : SegmentCodec<Iterable<CommandSegment>> {
        override fun encode(writer: BufferedWriter, element: List<StyledSegment<String, MutableCollection<String>>>) {
            TODO("not implemented")
        }

        override fun decode(input: Iterable<CommandSegment>): List<StyledSegment<String, MutableCollection<String>>> {
            return input.map {
                StyledSegment(it.segment, mapToStyles(it.commands))
            }
        }

        private fun mapToStyles(commands: Iterable<RtfCommand>): MutableCollection<String> {
            return commands.fold(listOf<String>()) { acc, rtfCommand ->
                if (RTF_TO_STYLES.containsKey(rtfCommand)) {
                    acc.plus(RTF_TO_STYLES.getValue(rtfCommand).name)
                } else {
                    acc
                }
            }.toMutableList()
        }
    }

    /** Cleans a proper RTF input string by stripping it of any useless information enclosed in braces. */
    private fun cleanRtfString(input: String): String {
        return BRACES_MATCHER.replace(input, "")
            .remove("{", "}", "\n", "\r", "\t")
    }

    /** Skims the text and divides it into a list of [CommandSegment]s. Note that only defined commands (see [RtfCommand]) are retained. */
    private fun findCommands(input: String): Iterable<CommandSegment> {
        // To understand this method, it is important to understand the way RTF commands work.
        // An RTF command is terminated by one of two tokens: a space (if there is no command immediately following)
        // or the backslash of the next command.
        // If it is succeeded by a space, that space is part of `commandRange` and must be stripped out.
        // If there would be a space in text right after a command, it is two spaces instead, so the command space can still be safely stripped.

        val deque = ArrayDeque<CommandSegment>(64)
        var currentCommands = ArrayDeque<RtfCommand>(16)
        var lastIndex = -1
        var commandRange = findNextCommand(input, lastIndex)

        while (commandRange != IntRange.EMPTY) {
            val segmentText: String = input.substring(lastIndex + 1 until commandRange.first)
            val command = input.substring(commandRange).trimEnd(' ')
            // Some commands are "segment commands," i.e. they operate on a portion of text. These segments are terminated by their command and a 0.
            val rtfCommand = RtfCommand.getCommand(if (command.last() == '0') command.removeRange(command.lastIndex..command.lastIndex) else command)

            if (segmentText.isNotEmpty()) {
                deque.add(CommandSegment(segmentText, currentCommands))
                currentCommands = ArrayDeque<RtfCommand>(currentCommands.filter { it.canTerminate() })
            }

            if (rtfCommand != null) {
                // This may not be entirely safe. This method doesn't distinguish between command terminators (command + 0) and normal commands,
                // so it is theoretically possible that this screws up the formatting if two "beginning commands" follow one another without a
                // terminating command in-between.

                if (currentCommands.contains(rtfCommand)) {
                    currentCommands.remove(rtfCommand)
                } else {
                    currentCommands.add(rtfCommand)
                }
            }

            lastIndex = commandRange.last
            commandRange = findNextCommand(input, lastIndex)
        }

        return deque
    }

    private fun findNextCommand(inString: CharSequence, start: Int): IntRange {
        var index = start

        do {
            index = inString.indexOf('\\', index + 1)
        } while (index != -1 && inString[index + 1] == '\\' && inString[index - 1] == '\\')

        if (index == -1) {
            return IntRange.EMPTY
        }

        var end = inString.indexOfAny(charArrayOf(' ', '\\'), index + 1)

        if (end == -1) {
            end = inString.lastIndex
        } else if (inString[end] == '\\') {
            end -= 1
        }

        return index..end
    }

    data class CommandSegment(
        val segment: String,
        val commands: Iterable<RtfCommand>
    )
}