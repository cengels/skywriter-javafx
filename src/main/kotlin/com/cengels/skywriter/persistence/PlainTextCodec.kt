package com.cengels.skywriter.persistence

import java.io.BufferedReader
import java.io.BufferedWriter

interface PlainTextCodec<TElement, TInput> {
    fun encode(writer: BufferedWriter, element: TElement)
    fun decode(input: TInput): TElement
}
