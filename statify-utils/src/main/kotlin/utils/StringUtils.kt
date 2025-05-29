package utils

fun String.trimToNull() = this.trim().ifBlank { null }