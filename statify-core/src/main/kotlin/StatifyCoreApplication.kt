package org.danila

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StatifyCoreApplication

fun main(args: Array<String>) {
    runApplication<StatifyCoreApplication>(*args)
}