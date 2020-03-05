package com.cengels.skywriter.enum

enum class RtfCommand {
    /** Bold. */ B,
    /** Italics. */ I,
    /** Strikethrough. */ STRIKE,
    /** Paragraph break. */ PAR,
    /** Line break. */ LINE,
    /** Page break. */ PAGE,
    ROW,
    TAB,
    CELL;

    companion object {
        /** Commands that modify a segment of text and can be terminated using a 0 suffix. */
        val SEGMENT_COMMANDS = listOf(B, I, STRIKE)

        val LINE_TERMINATORS = listOf(PAR, LINE, ROW, PAGE)

        val TAB_INDICATORS = listOf(TAB, CELL)

        fun getCommand(command: String): RtfCommand? {
            val commandText = command.trimStart('\\')
            return RtfCommand.values().find { it.name.toLowerCase() == commandText }
        }
    }

    fun canTerminate(): Boolean {
        return SEGMENT_COMMANDS.contains(this)
    }

    fun asString(): String {
        return "\\${this.name.toLowerCase()}"
    }
}