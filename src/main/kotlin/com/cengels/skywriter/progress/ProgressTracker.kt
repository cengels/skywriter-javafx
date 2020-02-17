package com.cengels.skywriter.progress

import com.cengels.skywriter.SkyWriterApp
import com.cengels.skywriter.persistence.AppConfig
import com.cengels.skywriter.persistence.CsvParser
import com.cengels.skywriter.util.Disposable
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.concurrent.schedule

/** Supplies methods used to manage [ProgressItem]s. */
class ProgressTracker(private var totalWords: Int, private var file: File? = null) : Disposable {
    private val csvParser = CsvParser(ProgressItem::class)
    private val csvFile: File
        get() = File("${SkyWriterApp.userDirectory}progress.csv")
    private val _progress: MutableList<ProgressItem> = mutableListOf()
    private var scheduledReset: TimerTask? = null

    /** All past progress items. */
    val progress: Collection<ProgressItem>
        get() = _progress
    /** All progress items on the current day, according to [AppConfig.progressResetTime]. */
    val progressToday: Collection<ProgressItem>
        get() = _progress.takeLastWhile {
            if (LocalTime.now().isBefore(AppConfig.progressResetTime)) {
                // between midnight and progressResetTime
                it.startDate.isAfter(AppConfig.progressResetTime.atDate(LocalDate.now().minusDays(1)))
            } else {
                // between progressResetTime and midnight
                it.startDate.isAfter(AppConfig.progressResetTime.atDate(LocalDate.now()))
            }
        }
    /** Gets the current progress item. If the user hasn't typed in a while, [current] will be null. */
    var current: ProgressItem? = null
        private set

    /** Starts a new progress item. This will throw an exception if [current] is not null. */
    fun startNew(): ProgressItem {
        if (current != null) {
            throw IllegalStateException("Before starting a new progress item, current must be reset to null.")
        }

        return ProgressItem(file = file?.name ?: "").also {
            current = it
            _progress.add(it)
            schedule()
        }
    }

    private fun schedule() {
        scheduledReset = Timer("scheduledReset", true).schedule(AppConfig.progressTimeout.toMillis()) {
            current?.let { commit() }
        }
    }

    /** Adds the difference between the total words and the new word count to the current progress item. If none exists or the file differs, instantiates a new one. */
    fun track(newTotalWords: Int) {
        scheduledReset?.cancel()
        val item = current ?: startNew()

        item.wordsAdded = (newTotalWords + item.wordsDeleted) - totalWords

        current = item
        schedule()
    }

    /**  */
    fun trackDeletion(deletedWords: Int) {
        scheduledReset?.cancel()
        val item = current ?: startNew()

        item.wordsDeleted += deletedWords
        schedule()
    }

    /** Sets [current].endDate to now and saves [current] to the file system and resets it. */
    fun commit() {
        scheduledReset?.cancel()
        val item = current ?: throw NullPointerException("Cannot commit a null progress item.")

        if (item.wordsAdded == 0 && item.wordsDeleted == 0) {
            current = null
            return
        }

        item.endDate = LocalDateTime.now()

        if (item.wordsAdded < 0) {
            item.wordsDeleted -= item.wordsAdded
            item.wordsAdded = 0
        }

        csvParser.appendToFile(csvFile, item)
        totalWords += item.wordsAdded - item.wordsDeleted

        // Make sure no other thread has modified current in the meantime.
        if (item == current) {
            current = null
        }
    }

    /** Saves all new progress items to the file system. */
    fun save() {
        csvParser.commitToFile(csvFile, _progress)
    }

    /** Loads all [ProgressItem]s stored on this file system into the application. */
    fun load() {
        _progress.clear()
        _progress.addAll(csvParser.readFromFile(csvFile))
    }

    override fun dispose() {
        scheduledReset?.cancel()
        scheduledReset = null
    }
}