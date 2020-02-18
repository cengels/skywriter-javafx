package com.cengels.skywriter.progress

import com.cengels.skywriter.SkyWriterApp
import com.cengels.skywriter.persistence.AppConfig
import com.cengels.skywriter.persistence.CsvParser
import com.cengels.skywriter.util.Disposable
import java.io.File
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.concurrent.schedule

private const val FIVE_MINUTES_IN_MS: Long = 300000

/** Supplies methods used to manage [ProgressItem]s. */
class ProgressTracker(private var totalWords: Int, private var file: File? = null) : Disposable {
    private val csvParser = CsvParser(ProgressItem::class)
    private val csvFile: File
        get() = File("${SkyWriterApp.userDirectory}progress.csv")
    private val _progress: MutableList<ProgressItem> = mutableListOf()
    private var scheduledReset: TimerTask? = null
    private var scheduledAutosave: TimerTask? = null

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

        scheduledReset?.cancel()

        return getNewOrLast().also {
            current = it
            scheduleReset()
        }
    }

    /** Adds the difference between the total words and the new word count to the current progress item. If none exists or the file differs, instantiates a new one. */
    fun track(newTotalWords: Int) {
        scheduledReset?.cancel()
        val item = current ?: startNew()

        item.wordsAdded = (newTotalWords + item.wordsDeleted) - totalWords

        current = item
        scheduleReset()
    }

    /** Explicitly tracks the specified word count as deleted words and excludes them from the words added. */
    fun trackDeletion(deletedWords: Int) {
        scheduledReset?.cancel()
        val item = current ?: startNew()

        item.wordsDeleted += deletedWords
        scheduleReset()
    }

    /** Sets [current].endDate to now and saves [current] to the file system and resets it. */
    fun commit() {
        scheduledReset?.cancel()
        val item = current ?: throw NullPointerException("Cannot commit a null progress item.")
        val finalizedItem = finalize(item) ?: return

        csvParser.commitToFile(csvFile, finalizedItem)

        // Make sure no other thread has modified current in the meantime.
        if (finalizedItem == current) {
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

    /** If the last progress item has not been ended longer than [AppConfig.progressTimeout] ago, gets it. Otherwise creates a new one and adds it to [progress]. */
    private fun getNewOrLast(): ProgressItem {
        scheduleAutosave()

        return _progress.lastOrNull().let {
            if (it?.file == file?.name && it?.endDate?.isAfter(LocalDateTime.now().minusSeconds(AppConfig.progressTimeout.seconds)) == true) {
                it.endDate = null
                totalWords -= it.wordsAdded + it.wordsDeleted
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

        csvParser.commitToFile(csvFile, finalizedItem)

        scheduleAutosave()
    }

    private fun finalize(item: ProgressItem): ProgressItem? {
        if (item.wordsAdded == 0 && item.wordsDeleted == 0) {
            current = null
            return null
        }

        item.endDate = LocalDateTime.now()

        if (item.wordsAdded < 0) {
            item.wordsDeleted -= item.wordsAdded
            item.wordsAdded = 0
        }

        totalWords += item.wordsAdded - item.wordsDeleted

        return item
    }

    /** Schedules for the current progress item to be committed if more than [AppConfig.progressTimeout] passes without input. */
    private fun scheduleReset() {
        scheduledReset?.cancel()
        scheduledReset = Timer("scheduledReset", true).schedule(AppConfig.progressTimeout.toMillis()) {
            current?.let { commit() }
        }
    }

    private fun scheduleAutosave() {
        scheduledAutosave?.cancel()
        scheduledAutosave = Timer("scheduledAutosave", true).schedule(FIVE_MINUTES_IN_MS) {
            current?.let { autosave() }
        }
    }
}