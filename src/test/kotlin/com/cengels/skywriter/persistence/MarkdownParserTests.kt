package com.cengels.skywriter.persistence

import io.kotlintest.*
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.specs.FreeSpec
import io.kotlintest.specs.WordSpec

class MarkdownParserTests : FreeSpec({
    "MarkdownParser.SEGMENT_CODEC.decode should" - {
        "leave unformatted strings" - {
            "the same" {
                val output = MarkdownParser.SEGMENT_CODEC.decode("This is a minor test involving an unformatted string.")

                output.size shouldBe 1
                output.single().segment shouldBe "This is a minor test involving an unformatted string."
                output.single().style.size shouldBe 0
            }
        }
        "properly decode simple formatting" - {
            "when the entire string is italicized" {
                val output = MarkdownParser.SEGMENT_CODEC.decode("*This test involves an italicized string.*")

                output.size shouldBe 1
                output.single().segment shouldBe "This test involves an italicized string."
                output.single().style.shouldContainExactly("italic")
            }
            "when the beginning is italicized" {
                val output = MarkdownParser.SEGMENT_CODEC.decode("*This test involves* an italicized string.")

                output.size shouldBe 2
                output[0].segment shouldBe "This test involves"
                output[0].style.shouldContainExactly("italic")
                output[1].segment shouldBe " an italicized string."
                output[1].style.size shouldBe 0
            }
            "when the middle is italicized" {
                val output = MarkdownParser.SEGMENT_CODEC.decode("This test *involves *an italicized string.")

                output.size shouldBe 3
                output[0].segment shouldBe "This test "
                output[0].style.size shouldBe 0
                output[1].segment shouldBe "involves "
                output[1].style.shouldContainExactly("italic")
                output[2].segment shouldBe "an italicized string."
                output[2].style.size shouldBe 0
            }
            "when the end is italicized" {
                val output = MarkdownParser.SEGMENT_CODEC.decode("This test involves *an italicized string.*")

                output.size shouldBe 2
                output[0].segment shouldBe "This test involves "
                output[1].segment shouldBe "an italicized string."
                output[0].style.size shouldBe 0
                output[1].style.shouldContainExactly("italic")
            }
            "when there are underscores" {
                val output = MarkdownParser.SEGMENT_CODEC.decode("This test involves _an italicized string._")

                output.size shouldBe 2
                output[0].segment shouldBe "This test involves "
                output[1].segment shouldBe "an italicized string."
                output[0].style.size shouldBe 0
                output[1].style.shouldContainExactly("italic")
            }
            "properly decode bold" {
                val output = MarkdownParser.SEGMENT_CODEC.decode("This test involves **a bold string.**")

                output.size shouldBe 2
                output[0].segment shouldBe "This test involves "
                output[0].style.size shouldBe 0
                output[1].segment shouldBe "a bold string."
                output[1].style.shouldContainExactly("bold")
            }
        }
        "properly decode nested formatting" - {
            "with bold within italics surrounded by spaces" {
                val output = MarkdownParser.SEGMENT_CODEC.decode("This test involves *nested **bold** within italics.*")

                output.size shouldBe 4
                output[0].segment shouldBe "This test involves "
                output[0].style.size shouldBe 0
                output[1].segment shouldBe "nested "
                output[1].style.shouldContainExactly("italic")
                output[2].segment shouldBe "bold"
                output[2].style.shouldContainExactlyInAnyOrder("italic", "bold")
                output[3].segment shouldBe " within italics."
                output[3].style.shouldContainExactly("italic")
            }
            "with bold within italics" {
                val output = MarkdownParser.SEGMENT_CODEC.decode("This test involves *nested **bold***.")

                output.size shouldBe 4
                output[0].segment shouldBe "This test involves "
                output[0].style.size shouldBe 0
                output[1].segment shouldBe "nested "
                output[1].style.shouldContainExactly("italic")
                output[2].segment shouldBe "bold"
                output[2].style.shouldContainExactlyInAnyOrder("italic", "bold")
                output[3].segment shouldBe "."
                output[3].style.size shouldBe 0
            }
            "with italics within bold" {
                val output = MarkdownParser.SEGMENT_CODEC.decode("This test __involves _italics_.__")

                output.size shouldBe 4
                output[0].segment shouldBe "This test "
                output[0].style.size shouldBe 0
                output[1].segment shouldBe "involves "
                output[1].style.shouldContainExactly("bold")
                output[2].segment shouldBe "italics"
                output[2].style.shouldContainExactlyInAnyOrder("italic", "bold")
                output[3].segment shouldBe "."
                output[3].style.shouldContainExactly("bold")
            }
        }
    }
})