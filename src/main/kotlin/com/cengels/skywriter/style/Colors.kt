package com.cengels.skywriter.style

import javafx.scene.paint.Color
import tornadofx.c

/** The colors of SkyWriter. Most colors are separated into three levels: low level (for de-emphasis), regular level (the standard set of colors), and high level (for emphasis). */
object Colors {
    object Primary {
        val REGULAR = c("#584C8D")
        val HIGH = c("#5E46C2")
    }
    object Background : LeveledColorSet, FunctionalColorSet {
        override val HIGH get() = throw NotImplementedError()
        override val REGULAR = c("#473C77")
        override val SELECTION = c("#5E46C2")
        override val LOW = c("#403668")
        override val HOVER = c("#3B3261")
        override val DISABLED = c("#413c54")
    }
    object Font : LeveledColorSet {
        override val LOW = c("#A8A8A9")
        override val REGULAR = c("#CACACA")
        override val HIGH = c("#FCFCFC")
    }
}