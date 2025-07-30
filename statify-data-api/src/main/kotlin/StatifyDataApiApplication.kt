package org.danila

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@ComponentScan("utils", "org.danila")
@SpringBootApplication
class StatifyDataApiApplication

fun main(args: Array<String>) {
    runApplication<StatifyDataApiApplication>(*args)
}