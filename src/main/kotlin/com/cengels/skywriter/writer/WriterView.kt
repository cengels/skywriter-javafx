package com.cengels.skywriter.writer

import javafx.application.Platform
import javafx.scene.text.TextFlow
import org.fxmisc.richtext.StyleClassedTextArea
import org.fxmisc.richtext.StyledTextArea
import org.fxmisc.richtext.TextExt
import tornadofx.*
import kotlin.concurrent.thread

class WriterView: View() {
    val model = WriterViewModel()
    val textArea = WriterTextArea().apply {
        this.insertText(0, "test")
        this.plainTextChanges().subscribe { change ->
            // TODO
        }

        contextmenu {
            item("Cut")
            item("Copy")
            item("Paste")
            item("Delete")
        }
    }

    override val root = vbox {
        borderpane {
            top {
                menubar {
                    menu("File") {
                        item("New", "Ctrl+N")
                        item("Open...", "Ctrl+O")
                        separator()
                        item("Save", "Ctrl+S") {
                            enableWhen(model.dirty)
                        }
                        item("Save As...", "Ctrl+Shift+S")
                        item("Rename...", "Ctrl+R")
                        separator()
                        item("Preferences...", "Ctrl+P")
                        item("Quit", "Ctrl+Alt+F4")
                    }

                    menu("Edit") {
                        item("Undo", "Ctrl+Z")
                        item("Redo", "Ctrl+Y")
                        separator()
                        item("Cut", "Ctrl+X")
                        item("Copy", "Ctrl+C")
                        item("Paste", "Ctrl+V")
                        item("Paste Unformatted", "Ctrl+Shift+V")
                        separator()
                        item("Select Word", "Ctrl+W").action { textArea.selectWord() }
                        item("Select Sentence")
                        item("Select Paragraph", "Ctrl+Shift+W").action { textArea.selectParagraph() }
                        item("Select All", "Ctrl+A").action { textArea.selectAll() }
                    }

                    menu("Formatting") {
                        item("Bold", "Ctrl+B").action { textArea.updateSelection("bold") }
                        item("Italic", "Ctrl+I").action { textArea.updateSelection("italic") }
                    }
                }
            }

            center {
//                scrollpane {
                    this += textArea

//                    textarea {
//                        bind(model.text)
//                    }
//                }
            }

            bottom {
                progressbar {
                    thread {
                        for (i in 1..100) {
                            Platform.runLater { progress = i.toDouble() / 100.0 }
                            Thread.sleep(100)
                        }
                    }
                }
            }
        }
    }
}