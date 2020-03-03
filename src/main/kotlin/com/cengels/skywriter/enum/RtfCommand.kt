package com.cengels.skywriter.enum

enum class RtfCommand {
    B,
    PAR,
    LINE,
    ROW,
    TAB,
    CELL;

    val LINE_TERMINATORS
        get() = listOf(PAR, LINE, ROW)

    val TAB_INDICATORS
        get() = listOf(TAB, CELL)
}