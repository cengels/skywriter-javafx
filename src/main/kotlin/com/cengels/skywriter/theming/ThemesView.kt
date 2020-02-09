package com.cengels.skywriter.theming

import com.cengels.skywriter.fragments.ThemedTitlebar
import com.cengels.skywriter.fragments.ThemedView
import javafx.scene.paint.Color
import tornadofx.*

class ThemesView : View("Themes") {
    override val root = borderpane {
        minHeight = 350.0
        minWidth = 500.0

        left {

        }
        right {
            vbox {
                button("Add")
                button("Edit")
                button("Remove")
            }
        }
    }
}