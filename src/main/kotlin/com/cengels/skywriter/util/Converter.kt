package com.cengels.skywriter.util

interface Converter<TFrom, TTo> {
    fun convert(value: TFrom): TTo
    fun convertBack(value: TTo): TFrom
}
