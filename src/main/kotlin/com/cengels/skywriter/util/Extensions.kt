package com.cengels.skywriter.util

import com.cengels.skywriter.persistence.codec.isEscaped
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.max

/** Checks if the [LocalDateTime] lies between now and now - duration. */
fun LocalDateTime.isWithin(duration: Duration): Boolean {
    return this.isAfter(LocalDateTime.now().minusSeconds(duration.seconds))
}

/** Checks if the iterable contains any of the given elements. */
fun <T> Iterable<T>.containsAny(otherCollection: Iterable<T>): Boolean {
    return this.any { otherCollection.contains(it) }
}

/** Checks if the iterable contains any of the given elements. */
fun <T> Iterable<T>.containsAny(vararg elements: T): Boolean {
    return this.any { elements.contains(it) }
}

/** The length of the range. */
val IntRange.length: Int
    get() = this.last - this.first

/**
 * Returns a new instance of this list with the given element added only if it was not already contained within the list.
 *
 * Note that this method always returns a new [List], regardless of whether the element was already within the list or not.
 **/
fun <T> Collection<T>.plusDistinct(element: T): List<T> {
    if (!this.contains(element)) {
        return this.plus(element)
    }

    return this.toList()
}

/** Toggle the given element in the collection, i.e. removes if it already existed and adds it otherwise. Returns the original collection. */
fun <TC : MutableCollection<T>, T> TC.toggle(element: T): TC {
    if (this.contains(element)) {
        this.remove(element)
    } else {
        this.add(element)
    }

    return this
}

/**
 * Returns a new instance of this list with all instances of the given element removed only if it was not already contained within the list.
 *
 * Note that this method always returns a new [List], regardless of whether the element was already within the list or not.
 **/
fun <T> Collection<T>.minusAll(element: T): List<T> {
    return this.filterNot { it === element }
}

/** If the [replacementString] contains either a `$` or `\` followed by a group index, substitute it by the corresponding matched group. If there is no group of that index, leaves the match alone and interprets it as a literal string. If no tokens are found, returns the replacement string as-is. Note that `$0` or `\0` inserts the entire matched value. */
fun MatchResult.replace(replacementString: String): String {
    var result: String = replacementString

    if (!result.containsAny('$', '\\')) {
        return result
    }

    for (i in this.groups.indices) {
        result = result.replace("\$$i", this.groupValues[i]).replace("\\$i", this.groupValues[i])
    }

    return result
}

/** Checks whether this object is equal to any of the specified objects. Uses structural equality. */
fun <T> T.isAnyOf(vararg comparables: T): Boolean {
    return comparables.any { it == this }
}

val <A, R> java.util.function.Function<A, R>.kotlinFunction: (A) -> R
    get() = { this.apply(it) }

val <A, R> ((A) -> R).javaFunction: java.util.function.Function<A, R>
    get() = java.util.function.Function<A, R> { a -> this(a) }
val <A, B, R> ((A, B) -> R).javaBiFunction: java.util.function.BiFunction<A, B, R>
    get() = java.util.function.BiFunction<A, B, R> { a, b -> this(a, b) }
val <A> ((A) -> Boolean).javaPredicate: java.util.function.Predicate<A>
    get() = java.util.function.Predicate<A> { a -> this(a) }
