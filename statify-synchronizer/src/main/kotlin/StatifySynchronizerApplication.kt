package org.danila

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@ComponentScan("config", "event", "utils", "logging", "org.danila")
@SpringBootApplication
@EnableR2dbcRepositories(basePackages = ["org.danila.repository"])
@EnableTransactionManagement
@EnableScheduling
class StatifySynchronizerApplication

fun main(args: Array<String>) {
    runApplication<StatifySynchronizerApplication>(*args)
}