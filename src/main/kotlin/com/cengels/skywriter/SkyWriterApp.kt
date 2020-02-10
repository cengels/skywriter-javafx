package com.cengels.skywriter

import com.cengels.skywriter.persistence.AppConfig
import com.cengels.skywriter.style.FormattingStylesheet
import com.cengels.skywriter.style.GeneralStylesheet
import com.cengels.skywriter.style.WriterStylesheet
import com.cengels.skywriter.writer.WriterView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.Stage
import javafx.stage.WindowEvent
import tornadofx.*

class SkyWriterApp : App(WriterView::class, WriterStylesheet::class, FormattingStylesheet::class) {
    init {
        reloadStylesheetsOnFocus()
        FX.layoutDebuggerShortcut = KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN, KeyCombination.CONTROL_DOWN)
    }

    override fun start(stage: Stage) {
        stage.minWidth = 300.0
        stage.minHeight = 200.0

        AppConfig.initialize(config)
        AppConfig.restoreStage(stage)

        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST) {
            AppConfig.storeStage(stage)
        }

        super.start(stage)
    }
}