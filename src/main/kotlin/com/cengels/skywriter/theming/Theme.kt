package com.cengels.skywriter.theming

import com.cengels.skywriter.enum.ImageSizingType
import javafx.scene.text.TextAlignment
import java.awt.Color
import java.io.Serializable

/** A theme the user can apply to change the look of the main writing area. */
data class Theme (
    /** The name of the Theme. Must be unique. */
    var name: String = "",
    /** If this Theme is marked as default, it can neither be edited nor removed. */
    val default: Boolean = false,
    /** The size of the font in points. */
    var fontSize: Double = 12.0,
    /** The name of the font family to be used. */
    var fontFamily: String = "Times New Roman",
    /** Specifies a background image to be used behind the main text area. */
    var backgroundImage: String? = null,
    /** If an image is specified, this specifies its sizing type. */
    var backgroundImageSizingType: ImageSizingType = ImageSizingType.COVER,
    /**
     * Indicates the width of the main text area. If the value is between 0 and 1, the value is proportional to the
     * overall window width. If the value is over 1, assumes an absolute width in pixels.
     */
    var documentWidth: Double = 0.9,
    /**
     * Indicates the height of the main text area. If the value is between 0 and 1, the value is proportional to the
     * overall window height. If the value is over 1, assumes an absolute height in pixels.
     */
    var documentHeight: Double = 1.0,
    /** The vertical padding of the document container in pixels. */
    var paddingVertical: Int = 20,
    /** The horizontal padding of the document container in pixels. */
    var paddingHorizontal: Int = 50,
    /** The indent of the first line in pixels. */
    var firstLineIndent: Double = 0.0,
    /** The line height proportionally to the font's default line height. 1.0 = default. 2.0 = double line height. */
    var lineHeight: Double = 1.0,
    /** The color of text. */
    var fontColor: Color = Color.BLACK,
    /** The background color of the area behind the document. */
    var windowBackground: Color = Color.LIGHT_GRAY,
    /** The background color of the text area. */
    var documentBackground: Color = Color.WHITE,
    /** The text alignment of normal text in the document. */
    var textAlignment: TextAlignment = TextAlignment.LEFT
) : Cloneable, Serializable {
    public override fun clone(): Theme {
        return this.copy(default = false)
    }
}
