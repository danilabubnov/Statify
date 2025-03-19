package org.danila

import io.github.cdimascio.dotenv.DotenvBuilder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication

@EntityScan(basePackages = ["org.danila.model"])
@SpringBootApplication
class StatifyCoreApplication

fun main(args: Array<String>) {
    DotenvBuilder().systemProperties().load()
    runApplication<StatifyCoreApplication>(*args)
}