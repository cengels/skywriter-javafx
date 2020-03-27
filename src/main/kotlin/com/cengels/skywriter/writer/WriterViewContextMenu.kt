package com.cengels.skywriter.writer

import com.cengels.skywriter.writer.area.WriterTextArea
import javafx.scene.control.ContextMenu
import tornadofx.*

fun WriterViewContextMenu(textArea: WriterTextArea, model: WriterViewModel): ContextMenu {
    return ContextMenu().apply {
        item("Cut") {
            this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
            this.action { textArea.cut() }
        }
        item("Copy") {
            this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
            this.action { textArea.copy() }
        }
        item("Paste").action { textArea.paste() }
        item("Paste Untracked").action {
            val wordCountBefore = textArea.wordCount
            textArea.paste()
            model.correct(textArea.wordCount - wordCountBefore)
        }
        item("Delete") {
            this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
            action { textArea.deleteText(textArea.selection) }
        }
        item("Delete Untracked") {
            this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
            action {
                val wordCountBefore = textArea.wordCount
                textArea.deleteText(textArea.selection)
                model.correct(textArea.wordCount - wordCountBefore)
            }
        }
    }
}