package com.cengels.skywriter.writer

import com.cengels.skywriter.enum.Heading
import com.cengels.skywriter.persistence.MarkdownParser
import com.sun.org.apache.xml.internal.serialize.LineSeparator
import javafx.scene.text.TextAlignment
import javafx.stage.FileChooser
import org.fxmisc.richtext.model.Codec
import org.fxmisc.richtext.model.ReadOnlyStyledDocument
import org.fxmisc.richtext.model.StyledDocument
import tornadofx.*
import java.io.File


class WriterView: View() {
    val model = WriterViewModel()
    val textArea = WriterTextArea().also {
        it.insertText(0, "This is a thing. This is another thing.")
        it.plainTextChanges().subscribe { change ->
            // TODO
        }

        it.isWrapText = true

        it.setStyleCodecs(MarkdownParser.PARAGRAPH_CODEC, MarkdownParser.SEGMENT_CODEC)

        contextmenu {
            item("Cut").action { it.cut() }
            item("Copy").action { it.copy() }
            item("Paste").action { it.paste() }
            item("Delete").action { it.deleteText(it.selection) }
        }

        shortcut("Shift+Enter") {
            it.insertText(it.caretPosition, LineSeparator.Windows)
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
                        item("Save As...", "Ctrl+Shift+S").action {
                            val initialDir = System.getProperty("user.dir")

                            chooseFile(
                                "Save As...",
                                arrayOf(FileChooser.ExtensionFilter("Markdown", ".md")),
                                File(initialDir),
                                FileChooserMode.Save).apply {
                                if (this.isNotEmpty()) {
                                    MarkdownParser(textArea.document).save(this.single())
                                }
                            }
                        }
                        item("Rename...", "Ctrl+R")
                        separator()
                        item("Preferences...", "Ctrl+P")
                        item("Quit", "Ctrl+Alt+F4")
                    }

                    menu("Edit") {
                        item("Undo", "Ctrl+Z").action { textArea.undo() }
                        item("Redo", "Ctrl+Y").action { textArea.redo() }
                        separator()
                        item("Cut", "Ctrl+X").action { textArea.cut() }
                        item("Copy", "Ctrl+C").action { textArea.copy() }
                        item("Paste", "Ctrl+V").action { textArea.paste() }
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
                        separator()
                        item("No Heading").action { textArea.setHeading(null) }
                        item("Heading 1").action { textArea.setHeading(Heading.H1) }
                        item("Heading 2").action { textArea.setHeading(Heading.H2) }
                        item("Heading 3").action { textArea.setHeading(Heading.H3) }
                        item("Heading 4").action { textArea.setHeading(Heading.H4) }
                        item("Heading 5").action { textArea.setHeading(Heading.H5) }
                        item("Heading 6").action { textArea.setHeading(Heading.H6) }
                        separator()
                        item("Align Left").action { textArea.setAlignment(TextAlignment.LEFT) }
                        item("Align Center").action { textArea.setAlignment(TextAlignment.CENTER) }
                        item("Align Right").action { textArea.setAlignment(TextAlignment.RIGHT) }
                        item("Align Justify").action { textArea.setAlignment(TextAlignment.JUSTIFY) }
                    }
                }
            }

            center {
                this += textArea
            }

            bottom {
                progressbar {

                }
            }
        }
    }
}