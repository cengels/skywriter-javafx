package com.cengels.skywriter.writer

import com.cengels.skywriter.persistence.MarkdownParser
import com.cengels.skywriter.progress.ProgressTracker
import com.cengels.skywriter.util.countWords
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import org.fxmisc.richtext.model.SegmentOps
import org.fxmisc.richtext.model.StyledDocument
import tornadofx.booleanBinding
import java.io.File
import tornadofx.getValue
import tornadofx.setValue

class WriterViewModel {
    val fileProperty = SimpleObjectProperty<File?>()
    var file: File? by fileProperty

    val fileExistsProperty = fileProperty.booleanBinding { it != null }

    val dirtyProperty = SimpleBooleanProperty(false)
    /** Whether the document has been modified since its last save. */
    var dirty by dirtyProperty

    val showStatusBarProperty = SimpleBooleanProperty(false)
    var showStatusBar by showStatusBarProperty
    val showMenuBarProperty = SimpleBooleanProperty(false)
    var showMenuBar by showMenuBarProperty

    var progressTracker: ProgressTracker? = null
    val wordsTodayProperty: SimpleIntegerProperty = SimpleIntegerProperty(getTodaysWords())
    val wordsToday by wordsTodayProperty

    private fun getTodaysWords(): Int {
        return progressTracker?.progressToday?.sumBy { it.words } ?: 0
    }

    fun save(document: StyledDocument<MutableCollection<String>, String, MutableCollection<String>>) {
        if (file == null) {
            throw NullPointerException("File cannot be null when attempting to save a document.")
        }

        MarkdownParser(document).save(file!!)
        dirty = false
    }

    fun load(document: StyledDocument<MutableCollection<String>, String, MutableCollection<String>>, segmentOps: SegmentOps<String, MutableCollection<String>>):  StyledDocument<MutableCollection<String>, String, MutableCollection<String>> {
        val file = this.file ?: throw NullPointerException("File cannot be null when attempting to save a document.")

        return MarkdownParser(document).load(file, segmentOps).also {
            dirty = false
            newProgressTracker(it.text.countWords(), file)
        }
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

    fun setWords(newTotalWords: Int) {
        progressTracker?.setWords(newTotalWords - wordsToday + (progressTracker?.current?.words ?: 0))
        wordsTodayProperty.set(getTodaysWords())
    }
}