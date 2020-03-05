package com.cengels.skywriter.persistence

import com.cengels.skywriter.persistence.codec.MarkdownCodecs
import org.fxmisc.richtext.model.*
import java.io.*

object MarkdownParser {
    var segOps: SegmentOps<String, MutableCollection<String>>? = null

    const val ESCAPE_CHARACTER: Char = '\\'

    val TOKEN_MAP: Map<String, String> = mapOf(
        Pair("**", "bold"),
        Pair("__", "bold"),
        Pair("*", "italic"),
        Pair("_", "italic"),
        Pair("~", "strikethrough")
    )

    fun save(file: File, document: ReadOnlyStyledDocument<MutableCollection<String>, String, MutableCollection<String>>) {
        try {
            file.bufferedWriter().apply {
                MarkdownCodecs.DOCUMENT_CODEC.encode(this, document.paragraphs)
                this.close()
            }
        } catch (exception: IOException) {
            exception.printStackTrace()

            throw exception
        }
    }

    fun load(file: File, segmentOps: SegmentOps<String, MutableCollection<String>>): ReadOnlyStyledDocument<MutableCollection<String>, String, MutableCollection<String>> {
        try {
            val bufferedReader = file.bufferedReader()
            segOps = segmentOps
            return MarkdownCodecs.DOCUMENT_CODEC.decode(bufferedReader).let {
                bufferedReader.close()

                val documentBuilder = ReadOnlyStyledDocumentBuilder<MutableCollection<String>, String, MutableCollection<String>>(
                    segOps, mutableListOf())

                it.forEach { documentBuilder.addParagraph(it.styledSegments, it.paragraphStyle) }

                documentBuilder.build()
            }
        } catch (exception: IOException) {
            exception.printStackTrace()

            throw exception
        }
    }
}
