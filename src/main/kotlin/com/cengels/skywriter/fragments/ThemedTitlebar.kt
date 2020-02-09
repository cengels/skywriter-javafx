package com.cengels.skywriter.fragments

import tornadofx.*

class ThemedTitlebar : Fragment("title bar") {
    override val root = borderpane {
        addClass("title-bar")

        left {
            label(modalStage?.title ?: "Untitled Stage")
        }

        right {
            hbox {
                button("_").action { modalStage!!.isIconified = true }
                button("#").action { modalStage!!.isMaximized = !modalStage!!.isMaximized }
                button("X").action { close() }
            }
        }
    }
}