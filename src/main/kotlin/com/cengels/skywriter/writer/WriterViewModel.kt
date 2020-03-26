package com.cengels.skywriter.writer

import com.cengels.skywriter.persistence.MarkdownParser
import com.cengels.skywriter.progress.ProgressTracker
import com.cengels.skywriter.util.EditableStyleClassedDocument
import com.cengels.skywriter.util.ReadOnlyStyleClassedDocument
import com.cengels.skywriter.util.StyleClassedDocument
import javafx.beans.property.*
import org.fxmisc.richtext.model.*
import tornadofx.booleanBinding
import java.io.File
import tornadofx.getValue
import tornadofx.setValue

class WriterViewModel {
    val fileProperty = SimpleObjectProperty<File?>()
    var file: File? by fileProperty

    val fileExistsProperty = fileProperty.booleanBinding { it != null }

    var dirtyProperty: BooleanProperty = SimpleBooleanProperty(false)
    /** Whether the document has been modified since its last save. */
    var dirty: Boolean by dirtyProperty

    val showStatusBarProperty = SimpleBooleanProperty(false)
    var showStatusBar by showStatusBarProperty
    val showMenuBarProperty = SimpleBooleanProperty(false)
    var showMenuBar by showMenuBarProperty

    var progressTracker: ProgressTracker? = null
    val originalDocumentProperty = SimpleObjectProperty<ReadOnlyStyleClassedDocument?>()
    var originalDocument by originalDocumentProperty
    val wordsTodayProperty: SimpleIntegerProperty = SimpleIntegerProperty(getTodaysWords())
    val wordsToday by wordsTodayProperty

    val findAndReplaceStateProperty = SimpleObjectProperty(FindAndReplace.None)
    var findAndReplaceState: FindAndReplace by findAndReplaceStateProperty

    private fun getTodaysWords(): Int {
        return progressTracker?.progressToday?.sumBy { it.words } ?: 0
    }

    fun save(document: EditableStyleClassedDocument) {
        if (file == null) {
            throw NullPointerException("File cannot be null when attempting to save a document.")
        }

        MarkdownParser.save(file!!, document.snapshot())
        originalDocument = document.snapshot()
    }

    fun load(segmentOps: SegmentOps<String, MutableCollection<String>>): StyleClassedDocument {
        val file = this.file ?: throw NullPointerException("File cannot be null when attempting to save a document.")

        findAndReplaceState = FindAndReplace.None

        return MarkdownParser.load(file, segmentOps)
    }

    fun reset(document: EditableStyleClassedDocument) {
        originalDocument = document.snapshot()
        findAndReplaceState = FindAndReplace.None
        file = null
        newProgressTracker(0)
    }

    /** Updates the progress tracker with the new file. */
    fun newProgressTracker(startingWords: Int, file: File? = null) {
        progressTracker?.apply {
            this.commit()
            this.dispose()
        }

        progressTracker = ProgressTracker(startingWords, file).apply {
            this.load()
        }

        wordsTodayProperty.set(getTodaysWords())
    }

    /** Updates the current progress item with the current words. */
    fun updateProgress(newTotalWords: Int) {
        progressTracker?.track(newTotalWords)
        wordsTodayProperty.set(getTodaysWords())
    }

    fun correct(correction: Int) {
        progressTracker?.correct(correction)
        wordsTodayProperty.set(getTodaysWords())
    }

    fun setWords(newWordsToday: Int) {
        val newWordsCurrent = newWordsToday - wordsToday + (progressTracker?.current?.words ?: 0)
        progressTracker?.setWords(newWordsCurrent)
        wordsTodayProperty.set(newWordsToday)
    }

    enum class FindAndReplace {
        Find,
        Replace,
        None
    }
}