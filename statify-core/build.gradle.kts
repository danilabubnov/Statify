plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.allopen)
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(project(":statify-utils"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.client)
    implementation(libs.spring.boot.starter.actuator)

    implementation(libs.spring.integration.kafka)
    implementation(libs.spring.security.crypto)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    implementation(libs.postgresql)

    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation(libs.spring.boot.starter.test) {
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
    testImplementation(libs.spring.security.test)
    testImplementation(libs.assertk)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.datafaker)
    testImplementation(libs.h2)
}