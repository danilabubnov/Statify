pluginManagement {
    val springBootVersion: String by settings
    val kotlinVersion: String by settings
    val springDepMgmtVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDepMgmtVersion
    }
}

rootProject.name = "statify"

include("statify-core")
include("statify-utils")