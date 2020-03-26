package com.cengels.skywriter.style

import javafx.scene.paint.Color

interface FunctionalColorSet {
    val REGULAR: Color
    val SELECTION: Color
    val HOVER: Color
    val DISABLED: Color
}

interface LeveledColorSet {
    val LOW: Color
    val REGULAR: Color
    val HIGH: Color
}