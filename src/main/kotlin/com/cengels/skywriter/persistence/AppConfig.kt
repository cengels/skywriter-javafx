package com.cengels.skywriter.persistence

import javafx.stage.Stage
import tornadofx.*
import java.io.File
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
    var activeTheme by StringConfigProperty()

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

    class DoubleConfigProperty : ConfigProperty<Double?>() {
        override operator fun getValue(thisRef: AppConfig, property: KProperty<*>): Double? {
            return config.double(property.name)
        }
    }

    class BooleanConfigProperty : ConfigProperty<Boolean?>() {
        override operator fun getValue(thisRef: AppConfig, property: KProperty<*>): Boolean? {
            return config.boolean(property.name)
        }
    }

    class StringConfigProperty : ConfigProperty<String?>() {
        override operator fun getValue(thisRef: AppConfig, property: KProperty<*>): String? {
            return config.string(property.name)
        }
    }
}