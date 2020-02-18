package com.cengels.skywriter.writer

import com.cengels.skywriter.enum.Heading
import com.cengels.skywriter.persistence.AppConfig
import com.cengels.skywriter.persistence.MarkdownParser
import com.cengels.skywriter.theming.ThemesManager
import com.cengels.skywriter.theming.ThemesView
import com.cengels.skywriter.util.convert.ColorConverter
import com.cengels.skywriter.util.countWords
import com.cengels.skywriter.util.getBackgroundFor
import com.cengels.skywriter.util.onChangeAndNow
import com.cengels.skywriter.util.toBackground
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import javafx.stage.FileChooser
import javafx.stage.WindowEvent
import tornadofx.*
import java.io.File


class WriterView : View("Skywriter") {
    val model = WriterViewModel()
    val themesManager = ThemesManager()

    init {
        this.updateTitle()
        themesManager.load()
        themesManager.selectedTheme = themesManager.themes.find { it.name == AppConfig.activeTheme } ?: ThemesManager.DEFAULT
        model.fileProperty.onChange { this.updateTitle() }
        model.dirtyProperty.onChange { this.updateTitle() }
    }

    val textArea = WriterTextArea().also {
        it.richChanges().subscribe { change ->
            model.dirty = true
        }

        it.plainTextChanges().subscribe { _ -> model.updateProgress(it.countWords()) }

        it.isWrapText = true
        it.useMaxHeight = true
        it.paddingHorizontalProperty.bind(themesManager.selectedThemeProperty.doubleBinding { it!!.paddingHorizontal.toDouble() })
        it.paddingVerticalProperty.bind(themesManager.selectedThemeProperty.doubleBinding { it!!.paddingVertical.toDouble() })
        it.backgroundProperty().bind(themesManager.selectedThemeProperty.objectBinding { it!!.documentBackground.toBackground() })

        themesManager.selectedThemeProperty.onChangeAndNow { theme ->
            it.style {
                fontSize = theme!!.fontSize.pt
                fontFamily = theme.fontFamily
            }
        }

        contextmenu {
            item("Cut").action { it.cut() }
            item("Copy").action { it.copy() }
            item("Paste").action { it.paste() }
            item("Delete").action { it.deleteText(it.selection) }
            item("Delete Untracked").action { model.updateProgressWithDeletion(it.countSelectedWords()); it.deleteText(it.selection) }
        }

        // Doesn't work.
//        shortcut("Shift+Enter") {
//            it.insertText(it.caretPosition, LineSeparator.Windows)
//        }
    }

    init {
        if (AppConfig.lastOpenFile != null) {
            File(AppConfig.lastOpenFile!!).apply {
                if (this.exists()) {
                    open(this)
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()

        primaryStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST) {
            warnOnUnsavedChanges { it.consume() }

            if (!it.isConsumed && model.file != null) {
                AppConfig.lastOpenFile = model.file!!.absolutePath
                AppConfig.save()
            }

            model.progressTracker?.commit()
            model.progressTracker?.dispose()
        }

        currentStage!!.scene.stylesheets.add(WriterView::class.java.getResource("dynamic.css").toExternalForm())
    }

    override val root = borderpane {
        setPrefSize(800.0, 600.0)

        themesManager.selectedThemeProperty.onChangeAndNow { theme ->
            style = theme?.toStylesheet() ?: ""
        }

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
                    item("Preferences...", "Ctrl+P").isDisable = true
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
                    item("Delete").action { textArea.deleteText(textArea.selection) }
                    item("Delete Untracked", "Shift+Delete").action { model.updateProgressWithDeletion(textArea.countSelectedWords()); textArea.deleteText(textArea.selection) }
                    separator()
                    item("Select Word", "Ctrl+W").action { textArea.selectWord() }
                    item("Select Sentence").isDisable = true
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

                menu("Tools") {
                    item("Appearance...").action { ThemesView(themesManager).openModal() }
                    item("Progress...").isDisable = true
                }
            }
        }

        center {
            gridpane {
                this.backgroundProperty().bind(themesManager.selectedThemeProperty.objectBinding { getBackgroundFor(it!!.windowBackground, it.backgroundImage, it.backgroundImageSizingType)  })
                this.useMaxWidth = true
                this.useMaxHeight = true
                this.columnConstraints.addAll(
                    ColumnConstraints().apply { this.hgrow = Priority.ALWAYS },
                    ColumnConstraints().apply {
                        this.percentWidthProperty().bind(themesManager.selectedThemeProperty.doubleBinding { if (it!!.documentWidth <= 1.0) it.documentWidth * 100.0 else -1.0 })
                        this.prefWidthProperty().bind(themesManager.selectedThemeProperty.doubleBinding { if (it!!.documentWidth > 1.0) it.documentWidth else -1.0 })
                    },
                    ColumnConstraints().apply { this.hgrow = Priority.ALWAYS }
                )
                this.rowConstraints.addAll(
                    RowConstraints().apply { this.vgrow = Priority.ALWAYS },
                    RowConstraints().apply {
                        this.percentHeightProperty().bind(themesManager.selectedThemeProperty.doubleBinding { if (it!!.documentHeight <= 1.0) it.documentHeight * 100.0 else -1.0 })
                        this.prefHeightProperty().bind(themesManager.selectedThemeProperty.doubleBinding { if (it!!.documentHeight > 1.0) it.documentHeight else -1.0 })
                    },
                    RowConstraints().apply { this.vgrow = Priority.ALWAYS }
                )

                this.add(textArea, 1, 1)
            }
        }

        bottom {
            useMaxWidth = true

            hbox {
                addClass("status-bar")
                alignment = Pos.CENTER
                paddingVertical = 5.0
                label(textArea.textProperty().stringBinding {
                    val totalWords = it?.countWords() ?: 0
                    val todaysWords = model.progressTracker?.progressToday?.sumBy { it.wordsAdded }
                    val todaysDeletedWords = model.progressTracker?.progressToday?.sumBy { it.wordsDeleted }

                    var string = "$totalWords total"

                    if (todaysWords != null) {
                        string += " • $todaysWords added today"
                    }

                    if (todaysDeletedWords != null) {
                        string += " • $todaysDeletedWords deleted today"
                    }

                    return@stringBinding string
                })
            }
        }
    }

    private fun updateTitle() {
        this.title = "Skywriter • ${if (model.file != null) model.file!!.name else "Untitled"}"

        if (model.dirty) {
            this.title += "*"
        }
    }

    private fun save() {
        if (model.file == null) {
            return openSaveDialog()
        }

        model.save(textArea.document)
    }

    private fun open(file: File) {
        model.file = file
        model.load(textArea.document, textArea.segOps).also {
            textArea.replace(it)
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
                save()
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
                model.newProgressTracker(textArea.countWords(), newFile)
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