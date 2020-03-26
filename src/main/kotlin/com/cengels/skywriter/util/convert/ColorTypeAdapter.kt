package com.cengels.skywriter.util.convert

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import javafx.scene.paint.Color
import tornadofx.css
import tornadofx.fiveDigits
import java.lang.reflect.Type

class ColorTypeAdapter : TypeAdapter<Color>(), JsonSerializer<Color>, JsonDeserializer<Color> {
    override fun write(out: JsonWriter?, value: Color?) {
        out?.value(convert(value))
    }

    override fun read(`in`: JsonReader?): Color {
        return convert(`in`?.nextString())
    }

    override fun serialize(src: Color?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
       return JsonPrimitive(convert(src))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Color {
        if (json?.isJsonPrimitive == true) {
            return convert(json.asString)
        }

        return Color.WHITE
    }

    private fun convert(color: Color?): String {
        return (color ?: Color.WHITE).css
    }

    private fun convert(string: String?): Color {
        if (string == null) {
            return Color.WHITE
        }

        val values = string.slice(5..string.lastIndex).split(", ")
        val components = values.slice(0..2).map { it.toDouble() / 255.0 }
        val opacity = fiveDigits.parse(values.last()).toDouble()

        return Color(components[0], components[1], components[2], opacity)
    }
}