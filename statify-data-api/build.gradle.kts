import com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.dgs.codegen)
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform(libs.graphql.dgs.platform.dependencies))

    implementation(project(":statify-utils"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)

    implementation(libs.graphql.dgs.spring.graphql.starter)
    implementation(libs.graphql.dgs.extended.validation)

    implementation(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgres)

    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation(libs.spring.boot.starter.test) {
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
}

project(":statify-data-api") {
    tasks.named<GenerateJavaTask>("generateJava") {
        schemaPaths = mutableListOf("$projectDir/src/main/resources/schema")
        packageName = "org.danila.generated"
        generateClient = false
    }
}