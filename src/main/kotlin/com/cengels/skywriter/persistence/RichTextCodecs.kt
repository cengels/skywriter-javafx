package com.cengels.skywriter.persistence

import com.cengels.skywriter.style.FormattingStylesheet
import javafx.scene.input.DataFormat
import org.fxmisc.richtext.model.Paragraph
import org.fxmisc.richtext.model.SegmentOps
import org.fxmisc.richtext.model.StyledSegment
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.BufferedWriter

interface CodecGroup : PlainTextCodec<List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>>, Any> {
    /** The DataFormat to apply the codecs on. If the DataFormat does not match, the codec is not used. */
    val dataFormat: DataFormat
}

object RichTextCodecs {
    object HTML : CodecGroup {
        private const val SELECTED_TAGS = "p, h1, h2, h3, h4, h5, h6, span, b, strong, i, em, s, del"
        override val dataFormat: DataFormat = DataFormat.HTML

        private val paragraph = object : PlainTextCodec<Paragraph<MutableCollection<String>, String, MutableCollection<String>>, Element> {
            override fun encode(writer: BufferedWriter, element: Paragraph<MutableCollection<String>, String, MutableCollection<String>>) {
                TODO("not implemented")
            }

            override fun decode(input: Element): Paragraph<MutableCollection<String>, String, MutableCollection<String>> {
                val paragraphStyles: MutableCollection<String> = mutableListOf()

                if (input.tagName().startsWith("h")) {
                    paragraphStyles.add(input.tagName())
                }

                return Paragraph(paragraphStyles, SegmentOps.styledTextOps<MutableCollection<String>>(), segment.decode(input))
            }
        }

        private val segment = object : PlainTextCodec<List<StyledSegment<String, MutableCollection<String>>>, Element> {
            override fun encode(writer: BufferedWriter, element: List<StyledSegment<String, MutableCollection<String>>>) {
                TODO("not implemented")
            }

            override fun decode(input: Element): List<StyledSegment<String, MutableCollection<String>>> {
                val styledSegments: MutableList<StyledSegment<String, MutableCollection<String>>> = mutableListOf()

                if (input.wholeOwnText().isNotEmpty()) {
                    val style: MutableCollection<String> = mutableListOf()

                    if (isBold(input)) {
                        style.add(FormattingStylesheet.bold.name)
                    }

                    if (isItalicized(input)) {
                        style.add(FormattingStylesheet.italic.name)
                    }

                    if (isStrikethrough(input)) {
                        style.add(FormattingStylesheet.strikethrough.name)
                    }

                    styledSegments.add(StyledSegment(input.wholeOwnText(), style))
                }

                return styledSegments.plus(
                    input.children()
                    .flatMap { decode(it) }
                )
            }

            private fun isBold(input: Element): Boolean {
                return input.tagName() == "b" || input.tagName() == "strong"
                        || input.getStyle("font-weight").let { it == "bold" || (it.toIntOrNull() ?: 0) > 400 }
            }

            private fun isItalicized(input: Element): Boolean {
                return input.tagName() == "i" || input.tagName() == "em" || input.getStyle("font-style").let { it == "italic" || it == "oblique" }
            }

            private fun isStrikethrough(input: Element): Boolean {
                return input.tagName() == "del" || input.tagName() == "s" || input.getStyle("text-decoration") == "line-through"
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

            val document = Jsoup.parse(input.replace("\r", "").replace("\n", ""))
                .apply { outputSettings().prettyPrint(false) }

            if (document.wholeText().isEmpty()) {
                return listOf()
            }

            val textElements = document.select(SELECTED_TAGS)
            val elementsNotContainedInAnother = textElements.filter { textElements.intersect(it.parents()).isEmpty() }

            return elementsNotContainedInAnother
                .fold(listOf<Element>()) { acc, element ->
                    // Necessary to make sure non-block elements are combined into one.
                    if (element.isBlock) {
                        acc + element
                    } else {
                        val sameParentElements = elementsNotContainedInAnother.filter { !it.isBlock && it.parent() === element.parent() }

                        if (sameParentElements.size <= 1) {
                            acc + element
                        } else if (!acc.any { it.children().any { child -> sameParentElements.contains(child) } }) {
                            acc + Element("p").also { parent ->
                                sameParentElements.forEach { parent.appendChild(it) }
                            }
                        } else {
                            acc
                        }
                    }
                }
                .let { if (it.isEmpty()) listOf(document.body()) else it }
                .map { paragraph.decode(it) }
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

/** Gets the specified style attribute or an empty [String] if it's not defined on this element. */
fun Element.getStyle(key: String): String {
    return this.getStyles().getOrDefault(key, "")
}

/** Gets all style attributes defined within this element. */
fun Element.getStyles(): Map<String, String> {
    return this.attr("style")
        .split(';')
        .filter { it.contains(":") } // protects against semicolons at the end of a style list
        .associate {
            it.split(':')
                .let { keyValue -> keyValue[0].trim() to keyValue[1].trim() }
        }
}

/** Gets this element's whole own text, i.e. including all whitespace but excluding child node text. */
fun Element.wholeOwnText(): String {
    return this.textNodes().fold("") { acc, textNode -> acc + textNode.wholeText }
}
