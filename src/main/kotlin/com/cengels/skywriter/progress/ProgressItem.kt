package com.cengels.skywriter.progress

import com.cengels.skywriter.persistence.CsvParser
import java.time.Duration
import java.time.LocalDateTime

@CsvParser.Order(["startDate", "endDate", "file", "words"])
/** Represents a writing session on one file. A writing session concludes when the configured number of minutes have passed between the last key input and the next one. */
data class ProgressItem(
    /** When the session began. */
    var startDate: LocalDateTime = LocalDateTime.now(),
    /** When the session ended or null if the session is ongoing. */
    var endDate: LocalDateTime? = null,
    /** The name of the file that the user edited during this session. */
    var file: String = "",
    /** How many new words were added during this session. */
    var words: Int = 0
) {
    /** The duration of this session. */
    val duration: Duration?
        get() = if (endDate != null) Duration.between(startDate, endDate) else null

    val wordsPerHour: Double
        get() = if (endDate != null) words.toDouble() / duration!!.seconds / 60 / 60 else 0.0
}