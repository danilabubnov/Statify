package org.danila

import io.github.cdimascio.dotenv.DotenvBuilder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@ComponentScan("config", "org.danila")
@SpringBootApplication
class StatifyCoreApplication

fun main(args: Array<String>) {
    DotenvBuilder().systemProperties().load()
    runApplication<StatifyCoreApplication>(*args)
}