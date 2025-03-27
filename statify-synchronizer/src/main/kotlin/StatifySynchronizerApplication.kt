package org.danila

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@ComponentScan("config", "event", "org.danila")
@SpringBootApplication
class StatifySynchronizerApplication

fun main(args: Array<String>) {
    runApplication<StatifySynchronizerApplication>(*args)
}