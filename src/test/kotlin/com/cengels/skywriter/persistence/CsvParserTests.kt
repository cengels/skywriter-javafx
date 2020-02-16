package com.cengels.skywriter.persistence

import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import java.time.LocalDateTime

data class TestCsvObject2(var date: LocalDateTime = LocalDateTime.now(), var string: String = "", var int: Int = 0, var double: Double = 0.0)
@CsvParser.Order(["date", "string", "int", "double"])
data class TestCsvObject(var date: LocalDateTime = LocalDateTime.now(), var string: String = "", var int: Int = 0, var double: Double = 0.0)
private val parser = CsvParser(TestCsvObject::class)

private fun date(year: Int, month: Int, day: Int): LocalDateTime {
    return LocalDateTime.of(year, month, day, 0, 0)
}

private fun dateS(year: Int, month: Int, day: Int): String {
    return LocalDateTime.of(year, month, day, 0, 0).toString()
}

class CsvParserTests : FreeSpec({
    "CsvParser should" - {
        "serialize" - {
            "a simple object alphabetically" {
                CsvParser(TestCsvObject2::class).serialize(TestCsvObject2(date(2020, 4, 12), "random", 512, 222.2)) shouldBe "${dateS(2020, 4, 12)},222.2,512,random"
            }
            "a simple object by declaration order" {
                parser.serialize(TestCsvObject(date(2020, 3, 12), "random", 512, 222.2)) shouldBe "${dateS(2020, 3, 12)},random,512,222.2"
            }
            "a collection of simple objects" {
                parser.serialize(listOf(
                    TestCsvObject(date(2020, 2, 12), "onetwo", 345, 678.9),
                    TestCsvObject(date(2019, 5, 27), "threefour", 56, 78.9),
                    TestCsvObject(date(2018, 7, 5), "fivesix", 7, 89.1)
                )) shouldBe
                        "${dateS(2020, 2, 12)},onetwo,345,678.9\n" +
                        "${dateS(2019, 5, 27)},threefour,56,78.9\n" +
                        "${dateS(2018, 7, 5)},fivesix,7,89.1"
            }
        }
        "deserialize" - {
            "a simple object alphabetically" {
                CsvParser(TestCsvObject2::class).deserialize("${dateS(2020, 1, 12)},222.2,512,random") shouldBe TestCsvObject2(date(2020, 1, 12), "random", 512, 222.2)
            }
            "a simple object by declaration order" {
                parser.deserialize("${dateS(2020, 1, 12)},random,512,222.2") shouldBe TestCsvObject(date(2020, 1, 12), "random", 512, 222.2)
            }
            "a collection of simple objects" {
                parser.deserializeLines("${dateS(2020, 2, 12)},onetwo,345,678.9\n" +
                        "${dateS(2019, 5, 27)},threefour,56,78.9\n" +
                        "${dateS(2018, 7, 5)},fivesix,7,89.1") shouldBe
                    listOf(
                        TestCsvObject(date(2020, 2, 12), "onetwo", 345, 678.9),
                        TestCsvObject(date(2019, 5, 27), "threefour", 56, 78.9),
                        TestCsvObject(date(2018, 7, 5), "fivesix", 7, 89.1)
                    )
            }
        }
    }
})