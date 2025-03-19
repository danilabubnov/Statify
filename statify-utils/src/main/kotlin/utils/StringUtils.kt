package utils

fun String.trimToNull() = this.trim().let { if (it.isBlank()) null else it }