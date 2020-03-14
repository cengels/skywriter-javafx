package com.cengels.skywriter.fragments

import javafx.scene.Parent
import javafx.stage.Modality
import javafx.stage.StageStyle
import tornadofx.*

/** Represents a [View] that uses its own themed title bar rather than the default JavaFX one. */
abstract class ThemedView(title: String) : View(title) {
    abstract val content: Parent

    override val root = borderpane {
        addClass("themed-view")

        top {
            this += ThemedTitlebar(title)
        }
    }

    override fun onDock() {
        super.onDock()

        root.bottom = content
    }

    fun openModal() {
        super.openModal(
            stageStyle = StageStyle.TRANSPARENT,
            modality = Modality.APPLICATION_MODAL,
            escapeClosesWindow = true,
            owner = currentWindow,
            block = false,
            resizable = true
        )

        modalStage?.apply {
            Resizable(this)
        }
    }
}