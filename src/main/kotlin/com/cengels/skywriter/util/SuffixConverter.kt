package com.cengels.skywriter.util

import javafx.util.converter.NumberStringConverter

/** An [Int] <-> [String] converter that adds a suffix to the displayed text. */
class SuffixConverter(val suffix: String) : NumberStringConverter() {
    override fun toString(value: Number?): String {
        if (value == null) {
            return "0 $suffix"
        }

        return "${super.toString(value)} $suffix"
    }

    override fun fromString(string: String?): Number {
        if (string == null) {
            return 0
        }

        return super.fromString(string.removeSuffix(" $suffix"))
    }
}