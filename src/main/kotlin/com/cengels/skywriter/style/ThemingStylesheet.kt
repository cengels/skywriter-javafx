package com.cengels.skywriter.style

import com.cengels.skywriter.SkyWriterApp
import tornadofx.*

class ThemingStylesheet : Stylesheet() {
    companion object {
        val themesGrid by cssclass()
        val gridPane by cssclass()
        val themeLabel by cssclass()
    }

    init {
        themesGrid contains datagridCell {
            backgroundColor += Colors.accentDarker
            padding = box(6.px)
            backgroundRadius += box(ThemedStylesheet.cornerRadius)
            borderRadius += box(ThemedStylesheet.cornerRadius)

            and(selectedClass) {
                backgroundColor += Colors.accentSelected
            }
        }

        themeLabel {
            padding = box(6.px, 0.px, 0.px, 0.px)
            font = SkyWriterApp.applicationFont
            fontSize = 9.pt
        }
    }
}