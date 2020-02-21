package com.cengels.skywriter.persistence

import javafx.stage.Stage
import tornadofx.*
import java.io.File
import java.time.Duration
import java.time.LocalTime
import javax.swing.filechooser.FileSystemView
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Provides type-safe properties corresponding to this application's app.config. */
object AppConfig {
    private lateinit var config: ConfigProperties
    var windowMaximized by BooleanConfigProperty()
    var windowHeight by DoubleConfigProperty()
    var windowWidth by DoubleConfigProperty()
    var windowX by DoubleConfigProperty()
    var windowY by DoubleConfigProperty()
    var lastOpenFile by StringConfigProperty()
    var lastCaretPosition by IntConfigProperty()
    var activeTheme by StringConfigProperty()
    var progressResetTime by TimeConfigProperty(LocalTime.of(6, 0))
    var progressTimeout by DurationConfigProperty(Duration.ofMinutes(5))
    private var commentTokensProperty by StringConfigProperty("[,]")
    var commentTokens: List<Pair<String, String>>
        get() = commentTokensProperty?.split(';')?.fold(listOf()) { acc, item ->
            val split = item.split(',')
            acc.plus(Pair(split.first(), split.getOrNull(1) ?: ""))
        } ?: listOf()
        set(value) { commentTokensProperty = value.joinToString(";") {
            if (it.second.isBlank()) {
                it.first
            } else {
                "${it.first},${it.second}"
            }
        } }

    fun initialize(config: ConfigProperties) {
        this.config = config
    }

    /** Serializes all config properties that were set since the last save. */
    fun save() {
        this.config.save()
    }

    /** Restores window properties from the config. */
    fun storeStage(stage: Stage) {
        AppConfig.windowMaximized = stage.isMaximized

        if (!stage.isMaximized) {
            AppConfig.windowHeight = stage.height
            AppConfig.windowWidth = stage.width
            AppConfig.windowX = stage.x
            AppConfig.windowY = stage.y
        }

        save()
    }

    /** Restores window properties from the config. */
    fun restoreStage(stage: Stage) {
        stage.isMaximized = AppConfig.windowMaximized ?: false
        stage.height = AppConfig.windowHeight ?: stage.height
        stage.width = AppConfig.windowWidth ?: stage.width
        stage.x = AppConfig.windowX ?: stage.x
        stage.y = AppConfig.windowY ?: stage.y
    }

    abstract class ConfigProperty<T> : ReadWriteProperty<AppConfig, T> {
        override operator fun setValue(thisRef: AppConfig, property: KProperty<*>, value: T) {
            config.set(property.name to value)
        }

        abstract override operator fun getValue(thisRef: AppConfig, property: KProperty<*>): T
    }

    class DoubleConfigProperty(private val default: Double? = null) : ConfigProperty<Double?>() {
        override operator fun getValue(thisRef: AppConfig, property: KProperty<*>): Double? {
            return config.double(property.name) ?: default
        }
    }

    class IntConfigProperty(private val default: Int? = null) : ConfigProperty<Int?>() {
        override operator fun getValue(thisRef: AppConfig, property: KProperty<*>): Int? {
            return config.int(property.name) ?: default
        }
    }

    class BooleanConfigProperty(private val default: Boolean? = null) : ConfigProperty<Boolean?>() {
        override operator fun getValue(thisRef: AppConfig, property: KProperty<*>): Boolean? {
            return config.boolean(property.name) ?: default
        }
    }

    class StringConfigProperty(private val default: String? = null) : ConfigProperty<String?>() {
        override operator fun getValue(thisRef: AppConfig, property: KProperty<*>): String? {
            return config.string(property.name) ?: default
        }
    }

    class TimeConfigProperty(private val default: LocalTime) : ConfigProperty<LocalTime>() {
        override operator fun getValue(thisRef: AppConfig, property: KProperty<*>): LocalTime {
            return config.string(property.name)?.let { LocalTime.parse(it) } ?: default
        }
    }

    class DurationConfigProperty(private val default: Duration) : ConfigProperty<Duration>() {
        override operator fun setValue(thisRef: AppConfig, property: KProperty<*>, value: Duration) {
            config.set(property.name to value.seconds)
        }

        override operator fun getValue(thisRef: AppConfig, property: KProperty<*>): Duration {
            return config.int(property.name)?.let { Duration.ofSeconds(it.toLong()) } ?: default
        }
    }
}