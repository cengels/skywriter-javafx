package com.cengels.skywriter.writer

import javafx.application.Platform
import tornadofx.*
import kotlin.concurrent.thread

class WriterView: View() {
    val model = WriterViewModel()

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
                        item("Select Word", "Ctrl+W")
                        item("Select Sentence")
                        item("Select Paragraph", "Ctrl+Shift+W")
                        item("Select All", "Ctrl+A")
                    }
                }
            }

            center {
                scrollpane {
                    textarea {
                        bind(model.text)

                        contextmenu {
                            item("Cut")
                            item("Copy")
                            item("Paste")
                            item("Delete")
                        }
                    }
                }
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