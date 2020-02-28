package com.cengels.skywriter.persistence

import com.cengels.skywriter.SkyWriterApp
import javafx.scene.input.KeyCombination
import javafx.stage.Stage
import tornadofx.*
import java.nio.charset.Charset
import java.nio.file.Path
import java.time.Duration
import java.time.LocalTime
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties

/** Provides type-safe properties corresponding to this application's app.config. */
object KeyConfig : Configurable {
    class Formatting {
        companion object {
            var bold by KeyConfigProperty(KeyCombination.valueOf("Ctrl+B"))
            var italics by KeyConfigProperty(KeyCombination.valueOf("Ctrl+I"))
            var heading1 by KeyConfigProperty(KeyCombination.valueOf("Ctrl+1"))
            var heading2 by KeyConfigProperty(KeyCombination.valueOf("Ctrl+2"))
            var heading3 by KeyConfigProperty(KeyCombination.valueOf("Ctrl+3"))
            var heading4 by KeyConfigProperty(KeyCombination.valueOf("Ctrl+4"))
            var heading5 by KeyConfigProperty(KeyCombination.valueOf("Ctrl+5"))
            var heading6 by KeyConfigProperty(KeyCombination.valueOf("Ctrl+6"))
        }
    }
    class File {
        companion object {
            var save by KeyConfigProperty(KeyCombination.valueOf("Ctrl+S"))
            var saveAs by KeyConfigProperty(KeyCombination.valueOf("Ctrl+Shift+S"))
            var open by KeyConfigProperty(KeyCombination.valueOf("Ctrl+O"))
        }
    }
    class Navigation {
        companion object {
            var preferences by KeyConfigProperty(KeyCombination.valueOf("Ctrl+P"))
            var fullscreen by KeyConfigProperty(KeyCombination.valueOf("F12"))
        }
    }

    fun getProperties(): Map<String, KeyCombination> {
        return KeyConfig.Formatting::class.companionObject?.declaredMemberProperties?.associate { it.name to it.getter.call(KeyConfig.Formatting.Companion) as KeyCombination }!!
            .plus(KeyConfig.File::class.companionObject?.declaredMemberProperties?.associate { it.name to it.getter.call(KeyConfig.File.Companion) as KeyCombination }!!)
            .plus(KeyConfig.Navigation::class.companionObject?.declaredMemberProperties?.associate { it.name to it.getter.call(KeyConfig.Navigation.Companion) as KeyCombination }!!)
    }

    /** Serializes all config properties that were set since the last save. */
    fun save() {
        this.config.save()
    }

    class KeyConfigProperty<in T>(val default: KeyCombination) : ReadWriteProperty<T, KeyCombination> {
        override operator fun setValue(thisRef: T, property: KProperty<*>, value: KeyCombination) {
            config.set(property.name to value)
        }

        override operator fun getValue(thisRef: T, property: KProperty<*>): KeyCombination {
            return KeyCombination.valueOf(config.string(property.name)) ?: default
        }
    }

    override val config: ConfigProperties by lazy { loadConfig() }
    override val configCharset: Charset
        get() = Charsets.UTF_8
    override val configPath: Path
        get() = SkyWriterApp.applicationDirectory.resolve("keymap.settings")
}