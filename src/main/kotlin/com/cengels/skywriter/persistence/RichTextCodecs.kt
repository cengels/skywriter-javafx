package com.cengels.skywriter.persistence

import javafx.scene.input.DataFormat
import jdk.nashorn.internal.runtime.regexp.joni.constants.NodeType
import org.fxmisc.richtext.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeFilter
import java.io.BufferedWriter

interface CodecGroup : PlainTextCodec<List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>>, Any> {
    /** The DataFormat to apply the codecs on. If the DataFormat does not match, the codec is not used. */
    val dataFormat: DataFormat
}

object RichTextCodecs {
    object HTML : CodecGroup {
        private const val SELECTED_TAGS = "p, h1, h2, h3, h4, h5, h6"
        override val dataFormat: DataFormat = DataFormat.HTML

        private val paragraph = object : PlainTextCodec<Paragraph<MutableCollection<String>, String, MutableCollection<String>>, Element> {
            override fun encode(writer: BufferedWriter, element: Paragraph<MutableCollection<String>, String, MutableCollection<String>>) {
                TODO("not implemented")
            }

            override fun decode(input: Element): Paragraph<MutableCollection<String>, String, MutableCollection<String>> {
                TODO("not implemented")
            }
        }

        private val segment = object : PlainTextCodec<StyledSegment<String, MutableCollection<String>>, String> {
            override fun encode(writer: BufferedWriter, element: StyledSegment<String, MutableCollection<String>>) {
                TODO("not implemented")
            }

            override fun decode(input: String): StyledSegment<String, MutableCollection<String>> {
                TODO("not implemented")
            }
        }

        override fun encode(writer: BufferedWriter, element: List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>>) {
            element.forEach { paragraph.encode(writer, it) }

            writer.close()
        }

        override fun decode(input: Any): List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>> {
            if (input !is String) {
                throw IllegalArgumentException("An HTML codec can only handle an input of type String, but input was of type ${input::class.simpleName}")
            }

            return Jsoup.parse(input).select(SELECTED_TAGS).map { paragraph.decode(it) }
        }
    }

    object RTF : CodecGroup {
        override val dataFormat: DataFormat = DataFormat.RTF

        private val paragraph = object : PlainTextCodec<Paragraph<MutableCollection<String>, String, MutableCollection<String>>, String> {
            override fun encode(writer: BufferedWriter, element: Paragraph<MutableCollection<String>, String, MutableCollection<String>>) {
                TODO("not implemented")
            }

            override fun decode(input: String): Paragraph<MutableCollection<String>, String, MutableCollection<String>> {
                TODO("not implemented")
            }
        }

        private val segment = object : PlainTextCodec<StyledSegment<String, MutableCollection<String>>, String> {
            override fun encode(writer: BufferedWriter, element: StyledSegment<String, MutableCollection<String>>) {
                TODO("not implemented")
            }

            override fun decode(input: String): StyledSegment<String, MutableCollection<String>> {
                TODO("not implemented")
            }
        }

        override fun encode(writer: BufferedWriter, element: List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>>) {
            element.forEach { paragraph.encode(writer, it) }

            writer.close()
        }

        override fun decode(input: Any): List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>> {
            if (input !is String) {
                throw IllegalArgumentException("An RTF codec can only handle an input of type String, but input was of type ${input::class.simpleName}")
            }

            TODO("not implemented")
        }
    }
}
