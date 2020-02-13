package com.cengels.skywriter.util

import javafx.util.StringConverter

/** A converter that gets an enum's name and capitalizes it. */
class EnumConverter<E : Enum<E>> : StringConverter<E>() {
    override fun toString(obj: E?): String? {
        return obj?.name?.toLowerCase()?.capitalize()
    }

    override fun fromString(string: String?): E? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}