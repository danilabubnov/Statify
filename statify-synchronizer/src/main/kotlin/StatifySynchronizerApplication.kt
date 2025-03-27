package org.danila

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StatifySynchronizerApplication

fun main(args: Array<String>) {
    runApplication<StatifySynchronizerApplication>(*args)
}