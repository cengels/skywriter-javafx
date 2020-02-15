package com.cengels.skywriter.util.convert

import javafx.util.converter.IntegerStringConverter
import javafx.util.converter.NumberStringConverter

/** An [Int] <-> [String] converter that adds a suffix to the displayed text. */
class SuffixConverter(val suffix: String) : NumberStringConverter() {
    private val integerConverter = IntegerStringConverter()

    override fun toString(value: Number?): String {
        if (value == null) {
            return "0 $suffix"
        }

        return "${integerConverter.toString(value.toInt())} $suffix"
    }

    override fun fromString(string: String?): Number {
        if (string == null) {
            return 0
        }

        return integerConverter.fromString(string.removeSuffix(" $suffix"))
    }
}