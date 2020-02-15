package com.cengels.skywriter.enum

enum class ImageSizingType {
    /** Resizes the image so it fills the entire container, even if parts of the image will be cut off as a result. */
    COVER,
    /** Resizes the image so it fills at least one dimension of the container. */
    CONTAIN,
    /** Does not resize the image and places it in the center of the container. */
    CENTER,
    /** Does not resize the image and tiles it so it repeats horizontally and vertically. */
    TILE
}