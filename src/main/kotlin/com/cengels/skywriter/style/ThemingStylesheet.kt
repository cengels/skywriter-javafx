package com.cengels.skywriter.style

import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class ThemingStylesheet : Stylesheet() {
    companion object {
        val themesGrid by cssclass()
        val gridPane by cssclass()
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
    }
}