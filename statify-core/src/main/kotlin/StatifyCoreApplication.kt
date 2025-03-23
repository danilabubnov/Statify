package org.danila

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@ComponentScan("config", "org.danila")
@SpringBootApplication
class StatifyCoreApplication

fun main(args: Array<String>) {
    runApplication<StatifyCoreApplication>(*args)
}