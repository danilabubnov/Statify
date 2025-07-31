@file:Suppress("UnstableApiUsage")

import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)      apply false
    alias(libs.plugins.kotlin.spring)   apply false
    alias(libs.plugins.kotlin.jpa)      apply false
    alias(libs.plugins.kotlin.allopen)  apply false
    alias(libs.plugins.spring.boot)     apply false
    alias(libs.plugins.spring.dep.mgmt) apply false
    alias(libs.plugins.dgs.codegen)     apply false
}

subprojects {

    group = "org.danila"
    version = "1.0-SNAPSHOT"

    repositories { mavenCentral() }

    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.set(listOf("-Xjsr305=strict"))
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    tasks.withType<Test>().configureEach { useJUnitPlatform() }

}