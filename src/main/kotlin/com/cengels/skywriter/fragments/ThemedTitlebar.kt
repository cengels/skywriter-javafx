package com.cengels.skywriter.fragments

import tornadofx.*

class ThemedTitlebar(val viewTitle: String, val showMinimize: Boolean = false, val showMaximize: Boolean = true, val showClose: Boolean = true) : Fragment() {
    override val root = borderpane {
        addClass("title-bar")

        left {
            label(viewTitle)
        }

        right {
            hbox {
                if (showMinimize) {
                    button("_").action { currentStage?.isIconified = true }
                }
                if (showMaximize) {
                    button("#").action { currentStage?.isMaximized = !currentStage!!.isMaximized }
                }
                if (showClose) {
                    button("X").action { close() }
                }
            }
        }
    }
}