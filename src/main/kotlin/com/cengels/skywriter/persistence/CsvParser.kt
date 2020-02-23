package com.cengels.skywriter.persistence

import com.sun.org.apache.xml.internal.serialize.LineSeparator
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.cast
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.jvm.jvmErasure

/**
 * Serializes and deserializes CSV. Has convenience methods for writing to and reading from a file as well.
 * @param klass The kotlin class of the specified generic type.
 * @param <T> The serialized/deserialized object type. All non-inherited member properties will be serialized/deserialized. Null values will result in an empty CSV section. Beware that the order of declaration will determine the serialization and deserialization order, so moving member properties around after the first serialized object is strongly discouraged.
 */
class CsvParser<T : Any>(private val klass: KClass<T>, val file: File? = null) {
    private val vars: Collection<KMutableProperty1<T, Any?>> by lazy<Collection<KMutableProperty1<T, Any?>>> {
        return@lazy klass.declaredMemberProperties.filterIsInstance<KMutableProperty1<T, Any?>>().run {
            return@run klass.findAnnotation<Order>().let {
                if (it != null) {
                    return@let this.sortedBy { element -> it.memberNames.indexOf(element.name) }
                }

                return@let this
            }
        }
    }
    private var cachedElements: Collection<T> = listOf()

    init {
        if (klass.visibility != KVisibility.PUBLIC) {
            throw IllegalArgumentException("T must be a public class.")
        }
    }

    /** Serializes the values of all non-inherited member properties of the passed object into a comma-separated list of values. */
    fun serialize(obj: T): String {
        return vars.joinToString(",") { it.get(obj)?.toString() ?: "" }
    }

    /** Serializes the values of all non-inherited member properties of the specified objects into comma-separated lists of values where each list is separated by a newline token. */
    fun serialize(collection: Collection<T>): String {
        return collection.joinToString("\n") { serialize(it) }
    }

    /** Deserializes the passed comma-separated list of values into an object of type [T]. If there are more properties than values (i.e. the class has been modified in post), extra properties will be ignored. If there are newline tokens in the passed string, only the first line will be converted. */
    fun deserialize(string: String): T {
        if (string.isBlank() || !string.contains(',')) {
            throw IllegalArgumentException("String must contain a comma-separated list of values.")
        }

        if (klass.primaryConstructor == null || klass.primaryConstructor!!.isAbstract || klass.primaryConstructor!!.parameters.any { !it.isOptional }) {
            throw IllegalArgumentException("T must have a callable parameterless constructor.")
        }

        return klass.primaryConstructor!!.callBy(mapOf()).apply {
            vars.zip(string.substringBefore(LineSeparator.Unix).substringBefore(LineSeparator.Macintosh).split(','))
                .forEach {
                    it.first.set(this, convert(it.second, it.first.returnType.jvmErasure) ?: it.first.javaSetter!!.defaultValue)
                }
        }
    }

    /** Deserializes the passed lines into objects of type [T]. */
    fun deserializeLines(lines: String): List<T> {
        val lineSeparator: String = this.getLineSeparator(lines)

        return lines.split(lineSeparator).map { deserialize(it) }
    }

    /** Serializes the specified object into a comma-separated list of values and adds it to the end of the specified file. */
    fun appendToFile(obj: T) {
        val file = this.file ?: throw UnsupportedOperationException("To execute file operations, you need to specify a file in the constructor.")
        file.appendText("${if (file.exists()) System.getProperty("line.separator") else ""}${serialize(obj)}")
    }

    /** Serializes the specified objects into comma-separated lists of values and appends them to the end of the specified file. */
    fun appendToFile(objects: Collection<T>) {
        writeToFile(objects, true)
    }

    /** Appends the items to the end of the specified file or rewrites the whole file if any items match the ones that have previously been loaded (it is assumed that they have changed and must be rewritten). For performance reasons, prefer to use this method over [appendToFile] or [writeToFile]. */
    fun commitToFile(objects: Collection<T>) {
        if (objects.any { obj -> cachedElements.any { obj === it } }) {
            writeToFile(mergeCached(objects), false)
        } else {
            writeToFile(objects, true)
        }
    }

    /** Serializes an object and either updates or appends it to the specified file, depending on whether it already existed. */
    fun commitToFile(obj: T) {
        // contains fails here, most likely because the hash was mutated after the item was first stored
        if (cachedElements.any { it === obj }) {
            writeToFile(mergeCached(listOf(obj)), false)
        } else {
            writeToFile(listOf(obj), true)
        }
    }

    /** Serializes the specified objects into comma-separated lists of values and writes them to the specified file, replacing all its contents. */
    fun writeToFile(objects: Collection<T>) {
        writeToFile(objects, false)
    }

    /** Reads all lines from the specified file and deserializes them into objects. */
    fun readFromFile(): List<T> {
        val file = this.file ?: throw UnsupportedOperationException("To execute file operations, you need to specify a file in the constructor.")
        return file.readLines().filter { it.isNotBlank() }.map { deserialize(it) }.also {
            cachedElements = it
        }
    }

    private fun writeToFile(objects: Collection<T>, append: Boolean) {
        if (objects.isEmpty()) {
            return
        }

        val file = this.file ?: throw UnsupportedOperationException("To execute file operations, you need to specify a file in the constructor.")

        val sameListAsInput = objects === cachedElements

        if (!sameListAsInput) {
            mergeCached(objects)
        }

        BufferedWriter(FileWriter(file, !sameListAsInput && append)).apply {
            objects.forEach {
                this.write(serialize(it))
                this.newLine()
            }

            this.close()
        }
    }

    private fun mergeCached(with: Collection<T>): Collection<T> {
        cachedElements = cachedElements.union(with)

        return cachedElements
    }

    private fun <T2 : Any> convert(from: String?, into: KClass<T2>): T2? {
        if (from == null) {
            return null
        }

        return when (into) {
            Int::class -> from.toInt() as T2
            String::class -> from as T2
            Double::class -> from.toDouble() as T2
            Float::class -> from.toFloat() as T2
            Long::class -> from.toLong() as T2
            LocalDateTime::class -> LocalDateTime.parse(from) as T2
            LocalDate::class -> LocalDate.parse(from) as T2
            LocalTime::class -> LocalTime.parse(from) as T2
            Date::class -> Date.parse(from) as T2
            else -> throw IllegalArgumentException("Failed to parse value of type ${into.qualifiedName}.")
        }
    }

    /** Gets the line separator found in this string. This is not necessarily the same as the system's line.separator property. */
    private fun getLineSeparator(lines: String): String {
        if (lines.contains(LineSeparator.Windows)) {
            return LineSeparator.Windows
        } else if (lines.contains(LineSeparator.Unix)) {
            return LineSeparator.Unix
        }

        return LineSeparator.Macintosh
    }

    @Target(AnnotationTarget.CLASS)
    @MustBeDocumented
    /** Determines the order of the properties during CSV serialization. Especially useful to guarantee compatibility with older versions of the class. */
    annotation class Order(val memberNames: Array<String>)
}