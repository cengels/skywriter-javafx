package com.cengels.skywriter.style

import tornadofx.c

/** The colors of SkyWriter. Most colors are separated into three levels: low level (for de-emphasis), regular level (the standard set of colors), and high level (for emphasis). */
object Colors {
    val PRIMARY = c("#5E46C2")
    object Background : LeveledColorSet, FunctionalColorSet {
        override val HIGH = c("#584C8D")
        override val REGULAR = c("#473C77")
        override val SELECTION = c("#342d52")
        override val LOW = c("#403668")
        override val HOVER = c("#3B3261")
        override val DISABLED = c("#413c54")
    }
    object Font : LeveledColorSet {
        val DEEP = c("#9e9e9f")
        override val LOW = c("#A8A8A9")
        override val REGULAR = c("#CACACA")
        override val HIGH = c("#FCFCFC")
    }
}