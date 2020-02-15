package com.cengels.skywriter.util.convert

interface Converter<TFrom, TTo> {
    fun convert(value: TFrom): TTo
    fun convertBack(value: TTo): TFrom
}
