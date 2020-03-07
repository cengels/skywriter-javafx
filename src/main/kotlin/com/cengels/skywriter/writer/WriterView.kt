package com.cengels.skywriter.writer

import com.cengels.skywriter.enum.Heading
import com.cengels.skywriter.persistence.AppConfig
import com.cengels.skywriter.persistence.KeyConfig
import com.cengels.skywriter.style.GeneralStylesheet
import com.cengels.skywriter.theming.ThemesManager
import com.cengels.skywriter.theming.ThemesView
import com.cengels.skywriter.util.*
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.stage.FileChooser
import javafx.stage.WindowEvent
import org.fxmisc.flowless.VirtualizedScrollPane
import tornadofx.*
import java.io.File
import java.time.LocalDateTime

class WriterView : View("Skywriter") {
    val model = WriterViewModel()
    lateinit var menuBar: MenuBar
    lateinit var statusBar: StackPane
    lateinit var findField: TextField
    lateinit var findBar: BorderPane

    init {
        this.updateTitle()
        model.fileProperty.onChange { this.updateTitle() }
        model.dirtyProperty.onChange { this.updateTitle() }
    }

    val textArea = WriterTextArea().also {
        it.richChanges().subscribe { change ->
            model.dirty = model.originalDocument != it.document.snapshot()
            model.progressTracker?.lastChange = LocalDateTime.now()
            model.progressTracker?.scheduleReset()
        }

        model.originalDocumentProperty.addListener { observable, oldValue, newValue ->
            model.dirty = newValue != it.document.snapshot()
        }

        model.findAndReplaceStateProperty.addListener { observable, oldValue, newValue ->
            if (newValue == WriterViewModel.FindAndReplace.None) {
                it.clearStyle(0, it.text.lastIndex, "search-highlighting")
            }
        }

        it.wordCountProperty.addListener { observable, oldValue, newValue ->
            model.updateProgress(it.wordCount)
        }

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
        AppConfig.lastOpenFile?.let { lastOpenFile ->
            File(lastOpenFile).apply {
                if (this.exists()) {
                    open(this)

                    AppConfig.lastCaretPosition?.let { lastCaretPosition ->
                        if (lastCaretPosition < textArea.text.length) {
                            textArea.moveTo(lastCaretPosition)
                        }
                    }
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
                AppConfig.lastCaretPosition = textArea.caretPosition
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
                    item("New", KeyConfig.File.new).action {
                        warnOnUnsavedChanges { return@action }

                        textArea.reset()
                        textArea.replaceText("")
                        model.reset(textArea.document)
                    }
                    item("Open...", KeyConfig.File.open).action {
                        openLoadDialog()
                    }
                    separator()
                    item("Save", KeyConfig.File.save) {
                        enableWhen(model.dirtyProperty)
                        action { save() }
                    }
                    item("Save As...", KeyConfig.File.saveAs).action {
                        openSaveDialog()
                    }
                    item("Rename...", KeyConfig.File.rename) {
                        enableWhen(model.fileExistsProperty)
                        action { rename() }
                    }
                    separator()
                    item("Fullscreen", KeyConfig.Navigation.fullscreen).action { primaryStage.isFullScreen = !primaryStage.isFullScreen }
                    item("Preferences...", KeyConfig.Navigation.preferences).isDisable = true
                    item("Quit", KeyConfig.Navigation.quit).action {
                        close()
                    }
                }

                menu("Edit") {
                    item("Undo", KeyConfig.Edit.undo) {
                        enableWhen { textArea.undoAvailableProperty() }
                        action { textArea.undo() }
                    }
                    item("Redo", KeyConfig.Edit.redo) {
                        enableWhen { textArea.redoAvailableProperty() }
                        action { textArea.redo() }
                    }
                    separator()
                    item("Cut", KeyConfig.Edit.cut) {
                        this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                        action { textArea.cut() }
                    }
                    item("Copy", KeyConfig.Edit.copy) {
                        this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                        action { textArea.copy() }
                    }
                    item("Paste", KeyConfig.Edit.paste).action { textArea.paste() }
                    item("Paste Unformatted", KeyConfig.Edit.pasteUnformatted)
                    item("Paste Untracked", KeyConfig.Edit.pasteUntracked).action {
                        val wordCountBefore = textArea.wordCount
                        textArea.paste()
                        model.correct(textArea.wordCount - wordCountBefore)
                    }
                    item("Delete") {
                        this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                        action { textArea.deleteText(textArea.selection) }
                    }
                    item("Delete Untracked", KeyConfig.Edit.deleteUntracked) {
                        this.enableWhen(textArea.selectionProperty().booleanBinding { selection -> selection!!.length > 0 })
                        action  {
                            val wordCountBefore = textArea.wordCount
                            textArea.deleteText(textArea.selection)
                            model.correct(textArea.wordCount - wordCountBefore)
                        }
                    }
                    separator()
                    item("Select Word", KeyConfig.Selection.selectWord).action { textArea.selectWord() }
                    item("Select Paragraph", KeyConfig.Selection.selectParagraph).action { textArea.selectParagraph() }
                    item("Select All", KeyConfig.Selection.selectAll).action { textArea.selectAll() }
                    separator()
                    item("Find", KeyConfig.Edit.find).action { openFind() }
                    item("Find and replace", KeyConfig.Edit.findAndReplace).action { openFindAndReplace() }
                }

                menu("Formatting") {
                    item("Bold", KeyConfig.Formatting.bold).action { textArea.activateStyle("bold") }
                    item("Italic", KeyConfig.Formatting.italics).action { textArea.activateStyle("italic") }
                    item("Strikethrough", KeyConfig.Formatting.strikethrough).action { textArea.activateStyle("strikethrough") }
                    separator()
                    item("No Heading", KeyConfig.Formatting.headingNone).action { textArea.setHeading(null) }
                    item("Heading 1", KeyConfig.Formatting.heading1).action { textArea.setHeading(Heading.H1) }
                    item("Heading 2", KeyConfig.Formatting.heading2).action { textArea.setHeading(Heading.H2) }
                    item("Heading 3", KeyConfig.Formatting.heading3).action { textArea.setHeading(Heading.H3) }
                    item("Heading 4", KeyConfig.Formatting.heading4).action { textArea.setHeading(Heading.H4) }
                    item("Heading 5", KeyConfig.Formatting.heading5).action { textArea.setHeading(Heading.H5) }
                    item("Heading 6", KeyConfig.Formatting.heading6).action { textArea.setHeading(Heading.H6) }
                }

                menu("Tools") {
                    item("Appearance...", KeyConfig.Navigation.appearance).action { ThemesView(ThemesManager).openModal() }
                    item("Progress...", KeyConfig.Navigation.progress).isDisable = true
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
                    primaryStage.fullScreenProperty().onChangeAndNow { isFullscreen ->
                        if (isFullscreen == true) {
                            scrollPane.getChildList()?.filterIsInstance<ScrollBar>()?.forEach { scrollbar ->
                                scrollbar.style = "-fx-opacity: 0;"
                                scrollbar.onHover {
                                    if (it) {
                                        scrollbar.style = "-fx-opacity: 1;"
                                    } else {
                                        scrollbar.style = "-fx-opacity: 0;"
                                    }
                                }
                            }
                        }
                    }
                }, 1, 1)
            }
        }

        bottom {
            useMaxWidth = true

            vbox {
                findBar = borderpane {
                    addClass("find-bar")
                    managedWhen(visibleProperty())
                    hiddenWhen(model.findAndReplaceStateProperty.isEqualTo(WriterViewModel.FindAndReplace.None))
                    paddingVertical = 10.0
                    paddingHorizontal = 20.0
                    this.setOnKeyReleased {
                        if (it.code == KeyCode.ESCAPE) {
                            model.findAndReplaceState = WriterViewModel.FindAndReplace.None
                        }
                    }

                    left {
                        vbox(5) {
                            hbox(10) {
                                hbox(10) {
                                    alignment = Pos.CENTER
                                    findField = textfield(textArea.searcher.findTermProperty) {
                                        isFocusTraversable = true
                                        prefWidth = 250.0
                                        promptText = "Find..."
                                        action { textArea.searcher.scrollToNext() }
                                    }
                                }

                                hbox(10) {
                                    alignment = Pos.CENTER
                                    hbox {
                                        button {
                                            addClass(GeneralStylesheet.plainButton)
                                            isFocusTraversable = false
                                            tooltip("Find previous occurrence")
                                            graphic = Group().apply {
                                                addClass("svg")
                                                line(5, 14, 5, 0) { strokeWidth = 2.0 }
                                                polyline(0, 7, 5, 0, 10, 7) { strokeWidth = 2.0 }
                                            }
                                            action { textArea.searcher.scrollToPrevious() }
                                        }
                                        button {
                                            addClass(GeneralStylesheet.plainButton)
                                            isFocusTraversable = false
                                            tooltip("Find next occurrence")
                                            graphic = Group().apply {
                                                addClass("svg")
                                                line(5, 0, 5, 14) { strokeWidth = 2.0 }
                                                polyline(10, 7, 5, 14, 0, 7) { strokeWidth = 2.0 }
                                            }
                                            action { textArea.searcher.scrollToNext() }
                                        }
                                    }
                                    label {
                                        prefWidth = 130.0
                                        textProperty().bind(textArea.searcher.countBinding.stringBinding {
                                            if (textArea.searcher.findTerm.isEmpty()) {
                                                "0 matches found"
                                            } else {
                                                "$it matches found"
                                            }
                                        })
                                    }
                                    checkbox("Whole words", textArea.searcher.findWholeWordsProperty) {
                                        isFocusTraversable = false
                                    }
                                    checkbox("Case-sensitive", textArea.searcher.caseSensitiveProperty) {
                                        isFocusTraversable = false
                                    }
                                }
                            }

                            hbox(15) {
                                managedWhen(visibleProperty())
                                hiddenWhen(model.findAndReplaceStateProperty.isNotEqualTo(WriterViewModel.FindAndReplace.Replace))

                                hbox(0) {
                                    alignment = Pos.CENTER
                                    textfield(textArea.searcher.replaceTermProperty) {
                                        isFocusTraversable = true
                                        prefWidth = 250.0
                                        promptText = "Replace with..."
                                        action { textArea.searcher.replaceCurrent() }
                                    }
                                }

                                hbox(10) {
                                    button("Replace") {
                                        addClass("text-button")
                                        isFocusTraversable = false
                                        enableWhen { textArea.searcher.countBinding.isNotEqualTo(0) }
                                        prefWidth = 90.0
                                        action { textArea.searcher.replaceCurrent() }
                                    }
                                    button("Replace all") {
                                        addClass("text-button")
                                        isFocusTraversable = false
                                        enableWhen { textArea.searcher.countBinding.booleanBinding {
                                            it != 0
                                        } }
                                        prefWidth = 90.0
                                        action { textArea.searcher.replaceAll() }
                                    }
                                }
                            }
                        }
                    }

                    right {
                        button {
                            addClass(GeneralStylesheet.plainButton)
                            action { model.findAndReplaceState = WriterViewModel.FindAndReplace.None }
                            graphic = svgpath("M0 0 9 9 M9 0 0 9") {
                                addClass("svg")
                                strokeWidth = 2.0
                            }
                        }
                    }
                }

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
                        label(textArea.wordCountProperty.stringBinding(textArea.selectionProperty()) {
                            "${it ?: 0} words" + if (textArea.selection.length > 0) " (${textArea.countSelectedWords()} selected)" else ""
                        })
                        label(textArea.wordCountProperty.stringBinding {
                            "${((it?.toInt() ?: 0) / 250) + 1} pages"
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
        model.load(textArea.segOps).also {
            model.progressTracker = null
            textArea.reset()
            textArea.replace(it)
            runAsync {} ui {
                model.newProgressTracker(textArea.wordCount, file)
                model.originalDocument = textArea.document.snapshot()
                textArea.requestCenterCaret()
            }
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

    private fun openFind() {
        if (model.findAndReplaceState == WriterViewModel.FindAndReplace.Find && findBar.isChildFocused()) {
            model.findAndReplaceState = WriterViewModel.FindAndReplace.None
        } else {
            model.findAndReplaceState = WriterViewModel.FindAndReplace.Find
        }

        findField.requestFocus()
    }

    private fun openFindAndReplace() {
        if (model.findAndReplaceState == WriterViewModel.FindAndReplace.Replace && findBar.isChildFocused()) {
            model.findAndReplaceState = WriterViewModel.FindAndReplace.None
        } else {
            model.findAndReplaceState = WriterViewModel.FindAndReplace.Replace
        }

        findField.requestFocus()
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