package com.cengels.skywriter.progress

import com.cengels.skywriter.SkyWriterApp
import com.cengels.skywriter.persistence.AppConfig
import com.cengels.skywriter.persistence.CsvParser
import com.cengels.skywriter.util.Disposable
import javafx.beans.property.SimpleIntegerProperty
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

/** Supplies methods used to manage [ProgressItem]s. */
class ProgressTracker(private var totalWords: Int, private var file: File? = null) : Disposable {
    private val csvFile: File
        get() = File("${SkyWriterApp.userDirectory}progress.csv")
    private val csvParser = CsvParser(ProgressItem::class, csvFile)
    private val _progress: MutableList<ProgressItem> = mutableListOf()
    private val scheduler = Executors.newScheduledThreadPool(3)
    private var scheduledReset: ScheduledFuture<*>? = null
    private var scheduledAutosave: ScheduledFuture<*>? = null

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
    var lastChange = LocalDateTime.MIN
    /** Gets the current progress item. If the user hasn't typed in a while, [current] will be null. */
    var current: ProgressItem? = null
        private set
    /** How many words should be added or subtracted from the word count of the [current] progress item before it is committed. */
    private var correction: Int = 0

    /** Starts a new progress item. This will throw an exception if [current] is not null. */
    fun startNew(): ProgressItem {
        if (current != null) {
            throw IllegalStateException("Before starting a new progress item, current must be reset to null.")
        }

        return getNewOrLast().also {
            current = it
        }
    }

    /** Adds the difference between the total words and the new word count to the current progress item. If none exists or the file differs, instantiates a new one. */
    fun track(newTotalWords: Int) {
        if (newTotalWords == totalWords && current == null)
            return

        val item = current ?: startNew()

        item.words = (newTotalWords - correction) - totalWords

        current = item
    }

    /** Explicitly "untracks" the specified number of words, excluding them from the [current]'s word count. */
    fun correct(correction: Int) {
        val item = current ?: startNew()

        this.correction += correction
        item.words -= correction
        lastChange = LocalDateTime.now()
    }

    /** Manually sets the word count of [current]. */
    fun setWords(newWords: Int) {
        (current ?: startNew()).words = newWords
    }

    /** Sets [current].endDate to now and saves [current] to the file system and resets it. */
    fun commit() {
        scheduledReset?.cancel(true)
        val item = current ?: return
        val finalizedItem = finalize(item) ?: return

        csvParser.commitToFile(finalizedItem)

        // Make sure no other thread has modified current in the meantime.
        if (finalizedItem == current) {
            current = null
        }
    }

    /** Saves all new progress items to the file system. */
    fun save() {
        csvParser.commitToFile(_progress)
    }

    /** Loads all [ProgressItem]s stored on this file system into the application. */
    fun load() {
        _progress.clear()
        _progress.addAll(csvParser.readFromFile())
    }

    /** If the last progress item has not been ended longer than [AppConfig.progressTimeout] ago, gets it. Otherwise creates a new one and adds it to [progress]. */
    private fun getNewOrLast(): ProgressItem {
        scheduleAutosave()

        return _progress.lastOrNull().let {
            // if (it?.file == file?.name && it?.endDate?.isAfter(LocalDateTime.now().minusSeconds(AppConfig.progressTimeout.seconds)) == true) {
            if (it?.file == file?.name && it?.endDate?.isAfter(LocalDateTime.now().minusSeconds(10)) == true) {
                it.endDate = null
                totalWords -= it.words
                return@let it
            }

            return@let null
        } ?: ProgressItem(file = file?.name ?: "").also {
            _progress.add(it)
        }
    }

    private fun autosave() {
        val item = current ?: return
        val finalizedItem = finalize(item) ?: return

        csvParser.commitToFile(finalizedItem)

        scheduleAutosave()
    }

    private fun finalize(item: ProgressItem): ProgressItem? {
        if (item.words == 0) {
            current = null
            return null
        }

        item.endDate = lastChange

        totalWords += item.words + correction
        correction = 0

        return item
    }

    /** Schedules for the current progress item to be committed if more than [AppConfig.progressTimeout] passes without input. */
    fun scheduleReset() {
        scheduledReset?.cancel(true)
        // scheduledReset = scheduler.schedule({ current?.let { commit() }}, AppConfig.progressTimeout.toMillis(), TimeUnit.MILLISECONDS)
        scheduledReset = scheduler.schedule({ current?.let { commit() }}, 10, TimeUnit.SECONDS)
    }

    private fun scheduleAutosave() {
        scheduledAutosave?.cancel(true)
        scheduledAutosave = scheduler.schedule({ current?.let { autosave() }}, 5, TimeUnit.MINUTES)
    }

    override fun dispose() {
        scheduledReset?.cancel(true)
        scheduledReset = null
        scheduledAutosave?.cancel(true)
        scheduledAutosave = null

        scheduler.shutdownNow()
    }
}