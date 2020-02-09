package com.cengels.skywriter.fragments

import javafx.scene.Parent
import tornadofx.*

/** Represents a [View] that uses its own themed title bar rather than the default JavaFX one. */
abstract class ThemedView(title: String) : View(title) {
    abstract val content: Parent

    override val root = borderpane {
        addClass("themed-view")

        top {
            this += ThemedTitlebar()
        }

        bottom {
            this.useMaxHeight = true
        }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()

        root.bottom {
            this += content
        }
    }
}