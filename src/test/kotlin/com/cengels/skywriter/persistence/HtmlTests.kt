package com.cengels.skywriter.persistence

import com.cengels.skywriter.persistence.codec.HtmlCodecs
import com.cengels.skywriter.persistence.codec.MarkdownCodecs
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private const val GOOGLE_DOCS_TEST_DATA = "<meta charset=\"utf-8\"><h1 dir=\"ltr\" style=\"line-height:1.38;margin-top:20pt;margin-bottom:6pt;\" id=\"docs-internal-guid-20de8f52-7fff-210c-73e2-9895ee524bdf\"><span style=\"font-size:20pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:400;font-style:normal;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">Heading 1</span></h1><h2 dir=\"ltr\" style=\"line-height:1.38;margin-top:18pt;margin-bottom:6pt;\"><span style=\"font-size:16pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:400;font-style:normal;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">Heading 2</span></h2><h3 dir=\"ltr\" style=\"line-height:1.38;margin-top:16pt;margin-bottom:4pt;\"><span style=\"font-size:13.999999999999998pt;font-family:Arial;color:#434343;background-color:transparent;font-weight:400;font-style:normal;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">Heading 3</span></h3><p dir=\"ltr\" style=\"line-height:1.38;margin-top:0pt;margin-bottom:0pt;\"><span style=\"font-size:11pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:400;font-style:normal;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">And we also have </span><span style=\"font-size:11pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:700;font-style:normal;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">bold</span><span style=\"font-size:11pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:400;font-style:normal;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">, </span><span style=\"font-size:11pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:400;font-style:italic;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">italics</span><span style=\"font-size:11pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:400;font-style:normal;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">, </span><span style=\"font-size:11pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:700;font-style:italic;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">both</span><span style=\"font-size:11pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:400;font-style:normal;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">, and </span><span style=\"font-size:11pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:400;font-style:normal;font-variant:normal;text-decoration:line-through;-webkit-text-decoration-skip:none;text-decoration-skip-ink:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">strikethrough</span><span style=\"font-size:11pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:400;font-style:normal;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">.</span></p><p dir=\"ltr\" style=\"line-height:1.38;margin-top:0pt;margin-bottom:0pt;\"><span style=\"font-size:11pt;font-family:Arial;color:#000000;background-color:transparent;font-weight:400;font-style:normal;font-variant:normal;text-decoration:none;vertical-align:baseline;white-space:pre;white-space:pre-wrap;\">This is a paragraph.</span></p>"

private const val ENCODER_INPUT = "#This is a test\n\\#This should not be a heading.\n**Bold**, *italics*, **_both_**, ~strikethrough~."

class HtmlTests : FreeSpec({
    "HTML codecs should" - {
        "encode" - {
            // TODO: Write these tests, preferably in another program (IDEA just breaks in this file performance-wise)
            // "a simple Markdown document" {
            //     ByteArrayOutputStream().let {
            //         MarkdownCodecs.DOCUMENT_CODEC.decode(BufferedReader(ByteArrayInputStream(ENCODER_INPUT.toByteArray())))
            //         val html = HtmlCodecs.DOCUMENT_CODEC.encode(BufferedWriter(it), ENCODER_INPUT)
            //     }
            // }
        }
        "decode" - {
            "a simple Google Docs document" {
                val paragraphs = HtmlCodecs.DOCUMENT_CODEC.decode(GOOGLE_DOCS_TEST_DATA)

                paragraphs.size shouldBe 5
                paragraphs[0].paragraphStyle.shouldContainExactly("h1")
                paragraphs[0].text shouldBe "Heading 1"
                paragraphs[1].paragraphStyle.shouldContainExactly("h2")
                paragraphs[1].text shouldBe "Heading 2"
                paragraphs[2].paragraphStyle.shouldContainExactly("h3")
                paragraphs[2].text shouldBe "Heading 3"
                paragraphs[3].let { paragraph ->
                    paragraph.paragraphStyle.size shouldBe 0
                    paragraph.styledSegments.size shouldBe 9
                    paragraph.styledSegments[0].let {
                        it.segment shouldBe "And we also have "
                        it.style.size shouldBe 0
                    }
                    paragraph.styledSegments[1].style.shouldContainExactly("bold")
                    paragraph.styledSegments[3].style.shouldContainExactly("italic")
                    paragraph.styledSegments[5].style.shouldContainExactly("bold", "italic")
                    paragraph.styledSegments[7].style.shouldContainExactly("strikethrough")
                }
                paragraphs[4].text shouldBe "This is a paragraph."
            }
        }
    }
})