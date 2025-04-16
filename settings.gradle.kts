pluginManagement {
    val springBootVersion: String by settings
    val kotlinVersion: String by settings
    val springDepMgmtVersion: String by settings
    val assertkVersion: String by settings
    val mockitoKotlinVersion: String by settings
    val kafkaVersion: String by settings
    val postgresqlVersion: String by settings
    val springDocOpenApiVersion: String by settings
    val jjwtVersion: String by settings
    val dataFakerVersion: String by settings
    val h2Version: String by settings
    val springSecurityCryptoVersion: String by settings
    val retrofitVersion: String by settings
    val r2dbcPostgresVersion: String by settings
    val reactorKafkaVersion: String by settings
    val retrofitLoggingVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDepMgmtVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "statify"

include("statify-core")
include("statify-utils")
include("statify-synchronizer")