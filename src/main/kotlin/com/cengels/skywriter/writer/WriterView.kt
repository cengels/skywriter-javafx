package com.cengels.skywriter.writer

import com.cengels.skywriter.enum.Heading
import com.cengels.skywriter.persistence.AppConfig
import com.cengels.skywriter.persistence.MarkdownParser
import com.cengels.skywriter.style.WriterStylesheet
import com.cengels.skywriter.theming.ThemesView
import com.sun.org.apache.xml.internal.serialize.LineSeparator
import javafx.scene.control.ButtonType
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import tornadofx.*
import java.io.File


class WriterView : View("Skywriter") {
    val model = WriterViewModel()
    val textArea = WriterTextArea().also {
        it.addClass(WriterStylesheet.textArea)

        it.richChanges().subscribe { change ->
            model.dirty = true
        }

        it.isWrapText = true
        it.useMaxHeight = true

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

    init {
        AppConfig.lastOpenFile.apply {
            if (this != null) {
                File(this).apply {
                    if (this.exists()) {
                        open(this)
                    }
                }
            }
        }

        this.updateTitle()
        model.fileProperty.onChange { this.updateTitle() }
        model.dirtyProperty.onChange { this.updateTitle() }
    }

    override fun onDock() {
        currentWindow!!.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST) {
            warnOnUnsavedChanges { it.consume() }

            if (!it.isConsumed && model.file != null) {
                AppConfig.lastOpenFile = model.file!!.absolutePath
                AppConfig.save()
            }
        }
    }

    override val root = borderpane {
        setPrefSize(800.0, 600.0)
        top {
            useMaxWidth = true
            menubar {
                menu("File") {
                    item("New", "Ctrl+N").action {
                        warnOnUnsavedChanges { return@action }

                        textArea.replaceText("")
                        model.file = null
                        model.dirty = false
                    }
                    item("Open...", "Ctrl+O").action {
                        openLoadDialog()
                    }
                    separator()
                    item("Save", "Ctrl+S") {
                        enableWhen(model.dirtyProperty)
                        action { save() }
                    }
                    item("Save As...", "Ctrl+Shift+S").action {
                        openSaveDialog()
                    }
                    item("Rename...", "Ctrl+R") {
                        enableWhen(model.fileExistsProperty)
                        action { rename() }
                    }
                    separator()
                    item("Preferences...", "Ctrl+P")
                    item("Appearance...").action { find<ThemesView>().openModal() }
                    item("Quit", "Ctrl+Alt+F4").action {
                        close()
                    }
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
                    item("Bold", "Ctrl+B").action { textArea.activateStyle("bold") }
                    item("Italic", "Ctrl+I").action { textArea.activateStyle("italic") }
                    item("Strikethrough").action { textArea.activateStyle("strikethrough") }
                    separator()
                    item("No Heading").action { textArea.setHeading(null) }
                    item("Heading 1").action { textArea.setHeading(Heading.H1) }
                    item("Heading 2").action { textArea.setHeading(Heading.H2) }
                    item("Heading 3").action { textArea.setHeading(Heading.H3) }
                    item("Heading 4").action { textArea.setHeading(Heading.H4) }
                    item("Heading 5").action { textArea.setHeading(Heading.H5) }
                    item("Heading 6").action { textArea.setHeading(Heading.H6) }
                }
            }
        }

        center {
            gridpane {
                this.useMaxWidth = true
                this.useMaxHeight = true
                this.columnConstraints.addAll(
                    ColumnConstraints().apply { this.percentWidth = 15.0 },
                    ColumnConstraints().apply { this.percentWidth = 70.0 },
                    ColumnConstraints().apply { this.percentWidth = 15.0 }
                )
                this.rowConstraints.add(RowConstraints().apply { this.percentHeight = 100.0 })

                vbox {
                    addClass(WriterStylesheet.textAreaBackground)
                    this.useMaxWidth = true
                    this.useMaxHeight = true
                    gridpaneConstraints {
                        columnRowIndex(0, 0)
                        columnSpan = 3
                    }
                }

                this.add(textArea, 1, 0)
            }
        }

        bottom {
            useMaxWidth = true
        }
    }

    private fun updateTitle() {
        this.title = if (model.file != null) "Skywriter • ${model.file!!.name}" else "Skywriter • Untitled"

        if (model.dirty) {
            this.title += "*"
        }
    }

    private fun save() {
        if (model.file == null) {
            return openSaveDialog()
        }

        MarkdownParser(textArea.document).save(model.file!!)
        model.dirty = false
    }

    private fun open(file: File) {
        model.file = file
        MarkdownParser(textArea.document).load(file, textArea.segOps).also {
            textArea.replace(it)
            model.dirty = false
        }
    }

    private fun openSaveDialog() {
        val initialDir = if (model.file != null) model.file!!.parent else System.getProperty("user.dir")

        chooseFile(
            "Save As...",
            arrayOf(FileChooser.ExtensionFilter("Markdown", "*.md")),
            File(initialDir),
            FileChooserMode.Save).apply {
            if (this.isNotEmpty()) {
                model.file = this.single()
                MarkdownParser(textArea.document).save(this.single())
                model.dirty = false
            }
        }
    }

    private fun openLoadDialog() {
        val initialDir = if (model.file != null) model.file!!.parent else System.getProperty("user.dir")

        chooseFile(
            "Open...",
            arrayOf(FileChooser.ExtensionFilter("Markdown", "*.md")),
            File(initialDir),
            FileChooserMode.Single).apply {
            if (this.isNotEmpty()) {
                warnOnUnsavedChanges { return@apply }

                open(this.single())
            }
        }
    }

    private fun rename() {
        chooseFile(
            "Rename...",
            arrayOf(FileChooser.ExtensionFilter("Markdown", "*.md")),
            File(model.file!!.parent),
            FileChooserMode.Save).apply {
            if (this.isNotEmpty()) {
                if (model.dirty) {
                    save()
                }

                val newFile: File = this.single()
                if (newFile.exists()) {
                    newFile.delete()
                }

                model.file!!.renameTo(newFile)
                updateTitle()
            }
        }
    }

    /** Warns the user of unsaved changes and prompts them to save. */
    private inline fun warnOnUnsavedChanges(onCancel: () -> Unit) {
        if (model.dirty) {
            warning(
                "Warning",
                "You have unsaved changes. Would you like to save them?",
                ButtonType.YES,
                ButtonType.NO,
                ButtonType.CANCEL
            ) {
                when (this.result) {
                    ButtonType.YES -> save()
                    ButtonType.CANCEL -> onCancel()
                }
            }
        }
    }
}