package com.cengels.skywriter.fragments

import com.cengels.skywriter.style.ThemedStylesheet
import com.cengels.skywriter.util.screenBounds
import com.cengels.skywriter.util.setRadiusClip
import javafx.scene.Parent
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.StageStyle
import tornadofx.*
import kotlin.math.min

/** Represents a [View] that uses its own themed title bar rather than the default JavaFX one. */
abstract class ThemedView(title: String? = null, val stylesheet: Stylesheet? = null) : View(title) {
    /** The [ThemedView]'s content. The element specified here will be added to the [root] and represents the window's content, aside from the title bar. */
    abstract val content: Parent
    /** If true, any instances of this view opened as modals are resizable. */
    var resizable: Boolean = true
    private lateinit var themedViewContainer: VBox

    /** The root of this [ThemedView] will always be a [BorderPane] containing the titlebar and the [content]. */
    final override val root = stackpane {
        addClass(ThemedStylesheet.themedView)

        themedViewContainer = vbox(0) {
            addClass(ThemedStylesheet.themedViewContainer)

            this += ThemedTitlebar(title)
        }
    }

    override fun onDock() {
        super.onDock()

        themedViewContainer += content
    }

    /** Sets the window's initial size. */
    fun setWindowInitialSize(width: Number, height: Number) = currentStage?.apply {
        val screenBounds = this.screenBounds.let { if (it.height == 0.0 || it.width == 0.0) Screen.getPrimary().visualBounds else it }
        this.width = width.toDouble().coerceIn(this.minWidth..min(this.maxWidth, screenBounds.width))
        this.height = height.toDouble().coerceIn(this.minHeight..min(this.maxHeight, screenBounds.height))
    }

    override fun onBeforeShow() {
        super.onBeforeShow()

        if (stylesheet != null) {
            this.root.scene.stylesheets.add(this.stylesheet.externalForm)
        }

        this.root.scene.fill = Color.TRANSPARENT
        currentStage?.apply {
            themedViewContainer.setRadiusClip(ThemedStylesheet.cornerRadius.value)
        }
    }

    /**
     * Opens this view as a modal. Use [resizable] to control whether the modal should be resizable or not.
     * To specify minimum, maximum, or initial sizes of the new window, use the methods [setWindowMinSize],
     * [setWindowMaxSize], and [setWindowInitialSize] respectively.
     **/
    fun openModal(modality: Modality = Modality.APPLICATION_MODAL) {
        super.openModal(
            stageStyle = StageStyle.TRANSPARENT,
            modality = modality,
            escapeClosesWindow = true,
            owner = currentWindow,
            block = false,
            resizable = true
        )?.apply {
            if (resizable) Resizable(this, ThemedStylesheet.shadowRadius)

            if (minWidth != 0.0 && width == 0.0) width = minWidth
            if (minHeight != 0.0 && height == 0.0) height = minHeight
        }
    }
}