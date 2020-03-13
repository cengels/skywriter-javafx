package com.cengels.skywriter.util

/** Runs the specified block of code and prints the execution time in the standard out, optionally supplying a name to identify the benchmark.  */
fun benchmark(name: String = "", callback: () -> Unit) {
    val startTime = System.nanoTime()
    callback()
    val endTime = System.nanoTime()
    val prefix = if (name.isNotEmpty()) "$name: " else ""
    println("${prefix}Execution took ${endTime - startTime} nanoseconds.")
}