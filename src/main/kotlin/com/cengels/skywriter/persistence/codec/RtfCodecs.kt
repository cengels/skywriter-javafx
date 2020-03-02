package com.cengels.skywriter.persistence.codec

import com.cengels.skywriter.persistence.PlainTextCodec
import javafx.scene.input.DataFormat
import org.fxmisc.richtext.model.Paragraph
import org.fxmisc.richtext.model.StyledSegment
import java.io.BufferedWriter


object RtfCodecs {
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
}