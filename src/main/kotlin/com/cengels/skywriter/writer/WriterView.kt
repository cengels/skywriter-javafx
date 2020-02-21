package com.cengels.skywriter.writer

import com.cengels.skywriter.enum.Heading
import com.cengels.skywriter.persistence.AppConfig
import com.cengels.skywriter.theming.ThemesManager
import com.cengels.skywriter.theming.ThemesView
import com.cengels.skywriter.util.*
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.stage.FileChooser
import javafx.stage.WindowEvent
import org.fxmisc.flowless.VirtualizedScrollPane
import tornadofx.*
import java.io.File

class WriterView : View("Skywriter") {
    val model = WriterViewModel()
    lateinit var menuBar: MenuBar
    lateinit var statusBar: StackPane

    init {
        this.updateTitle()
        model.fileProperty.onChange { this.updateTitle() }
        model.dirtyProperty.onChange { this.updateTitle() }
    }

    val textArea = WriterTextArea().also {
        it.richChanges().subscribe { change ->
            if (isDocked) {
                model.dirty = true
            }
        }

        it.wordCountProperty.addListener { observable, oldValue, newValue ->
            model.updateProgress(it.wordCount)
        }

        it.setOnKeyReleased { event ->
            when (event.code) {
                KeyCode.END -> it.moveTo(it.text.lastIndex)
                KeyCode.HOME -> it.moveTo(0)
                else -> return@setOnKeyReleased
            }
        }

        it.caretPositionProperty().addListener { _, _, _ ->
            it.requestFollowCaret()
        }

        it.isWrapText = true
        it.useMaxHeight = true
        it.paddingHorizontalProperty.bind(ThemesManager.selectedThemeProperty.doubleBinding { it!!.paddingHorizontal.toDouble() })
        it.paddingVerticalProperty.bind(ThemesManager.selectedThemeProperty.doubleBinding { it!!.paddingVertical.toDouble() })
        it.backgroundProperty().bind(ThemesManager.selectedThemeProperty.objectBinding { it!!.documentBackground.toBackground() })

        ThemesManager.selectedThemeProperty.onChangeAndNow { theme ->
            it.style {
                fontSize = theme!!.fontSize.pt
                fontFamily = theme.fontFamily
            }
        }

        it.contextMenu = contextmenu {
            item("Cut") {
                this.enableWhen(it.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                this.action { it.cut() }
            }
            item("Copy") {
                this.enableWhen(it.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                this.action { it.copy() }
            }
            item("Paste").action { it.paste() }
            item("Paste Untracked").action {
                val wordCountBefore = it.wordCount
                it.paste()
                model.correct(it.wordCount - wordCountBefore)
            }
            item("Delete") {
                this.enableWhen(it.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                action { it.deleteText(it.selection) }
            }
            item("Delete Untracked") {
                this.enableWhen(it.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                action {
                    val wordCountBefore = it.wordCount
                    it.deleteText(it.selection)
                    model.correct(it.wordCount - wordCountBefore)
                }
            }
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
    }

    override val root = borderpane {
        setPrefSize(800.0, 600.0)
        initializeStyle()

        setOnMouseMoved { event ->
            if (primaryStage.isFullScreen) {
                model.showMenuBar = event.sceneY <= menuBar.layoutY + menuBar.height || menuBar.menus.any { it.isShowing } == true
                model.showStatusBar = event.sceneY >= statusBar.layoutY
            }
        }

        top {
            useMaxWidth = true
            menuBar = menubar {
                managedWhen(visibleProperty())
                hiddenWhen(primaryStage.fullScreenProperty().and(model.showMenuBarProperty.not()))
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
                    item("Fullscreen", "F11").action { primaryStage.isFullScreen = !primaryStage.isFullScreen }
                    item("Preferences...", "Ctrl+P").isDisable = true
                    item("Quit", "Ctrl+Alt+F4").action {
                        close()
                    }
                }

                menu("Edit") {
                    item("Undo", "Ctrl+Z") {
                        enableWhen { textArea.undoAvailableProperty() }
                        action { textArea.undo() }
                    }
                    item("Redo", "Ctrl+Y") {
                        enableWhen { textArea.redoAvailableProperty() }
                        action { textArea.redo() }
                    }
                    separator()
                    item("Cut", "Ctrl+X") {
                        this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                        action { textArea.cut() }
                    }
                    item("Copy", "Ctrl+C") {
                        this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                        action { textArea.copy() }
                    }
                    item("Paste", "Ctrl+V").action { textArea.paste() }
                    item("Paste Unformatted", "Ctrl+Shift+V")
                    item("Paste Untracked").action {
                        val wordCountBefore = textArea.wordCount
                        textArea.paste()
                        model.correct(textArea.wordCount - wordCountBefore)
                    }
                    item("Delete") {
                        this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                        action { textArea.deleteText(textArea.selection) }
                    }
                    item("Delete Untracked", "Shift+Delete") {
                        this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                        action  {
                            val wordCountBefore = textArea.wordCount
                            textArea.deleteText(textArea.selection)
                            model.correct(textArea.wordCount - wordCountBefore)
                        }
                    }
                    separator()
                    item("Select Word", "Ctrl+W").action { textArea.selectWord() }
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
                    item("Appearance...").action { ThemesView(ThemesManager).openModal() }
                    item("Progress...").isDisable = true
                }


                menus.forEach { menu -> menu.showingProperty().addListener { observable, oldValue, newValue ->
                    if (primaryStage.isFullScreen && !menuBar.isHover && menuBar.menus.none { it.isShowing }) {
                        model.showMenuBar = false
                    }
                } }
            }
        }

        center {
            gridpane {
                this.backgroundProperty().bind(ThemesManager.selectedThemeProperty.objectBinding { getBackgroundFor(it!!.windowBackground, it.backgroundImage, it.backgroundImageSizingType)  })
                this.useMaxWidth = true
                this.useMaxHeight = true
                this.columnConstraints.addAll(
                    ColumnConstraints().apply { this.hgrow = Priority.ALWAYS },
                    ColumnConstraints().apply {
                        this.percentWidthProperty().bind(ThemesManager.selectedThemeProperty.doubleBinding { if (it!!.documentWidth <= 1.0) it.documentWidth * 100.0 else -1.0 })
                        this.prefWidthProperty().bind(ThemesManager.selectedThemeProperty.doubleBinding { if (it!!.documentWidth > 1.0) it.documentWidth else -1.0 })
                    },
                    ColumnConstraints().apply { this.hgrow = Priority.ALWAYS }
                )
                this.rowConstraints.addAll(
                    RowConstraints().apply { this.vgrow = Priority.ALWAYS },
                    RowConstraints().apply {
                        this.percentHeightProperty().bind(ThemesManager.selectedThemeProperty.doubleBinding { if (it!!.documentHeight <= 1.0) it.documentHeight * 100.0 else -1.0 })
                        this.prefHeightProperty().bind(ThemesManager.selectedThemeProperty.doubleBinding { if (it!!.documentHeight > 1.0) it.documentHeight else -1.0 })
                    },
                    RowConstraints().apply { this.vgrow = Priority.ALWAYS }
                )

                this.add(VirtualizedScrollPane(textArea, ScrollPane.ScrollBarPolicy.NEVER, ScrollPane.ScrollBarPolicy.AS_NEEDED).also { scrollPane ->
                    scrollPane.vbarPolicyProperty().bind(primaryStage.fullScreenProperty().objectBinding { if (it == true) ScrollPane.ScrollBarPolicy.NEVER else ScrollPane.ScrollBarPolicy.AS_NEEDED })
                }, 1, 1)
            }
        }

        bottom {
            useMaxWidth = true

            statusBar = stackpane {
                addClass("status-bar")
                managedWhen(visibleProperty())
                hiddenWhen(primaryStage.fullScreenProperty().and(model.showStatusBarProperty.not()))

                hbox {
                    isPickOnBounds = false
                    useMaxWidth = false
                    alignment = Pos.CENTER
                    spacing = 9.0
                    label(model.wordsTodayProperty.stringBinding {
                        "${model.wordsToday} added today"
                    }) {
                        addClass("clickable")
                        setOnMouseClicked { popup { popup ->
                            label("Enter a new word count")

                            numberfield(model.wordsToday) {
                                this.focusedProperty().addListener { observable, oldValue, newValue ->
                                    model.setWords(getDefaultConverter<Int>()!!.fromString(this.text))
                                }
                                this.setOnAction { popup.hide() }
                            }
                        } }
                    }
                }

                hbox {
                    isPickOnBounds = false
                    alignment = Pos.CENTER_RIGHT
                    spacing = 9.0
                    label(textArea.wordCountProperty.stringBinding {
                        "${it ?: 0} words"
                    })
                    label(textArea.wordCountProperty.stringBinding {
                        "${(it?.toInt() ?: 0) / 250} pages"
                    })
                    label(textArea.textProperty().stringBinding {
                        "${textArea.paragraphs.size} paragraphs"
                    })
                    label(textArea.textProperty().stringBinding {
                        "${it?.length} characters"
                    })
                }
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
        textArea.undoManager.forgetHistory()
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

    private fun openProgressView() {

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
                model.newProgressTracker(textArea.wordCount, newFile)
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