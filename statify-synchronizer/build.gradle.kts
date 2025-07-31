plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.allopen)
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(project(":statify-utils"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.r2dbc)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.redis.reactive)

    implementation(libs.r2dbc.postgresql)

    implementation(libs.reactor.kafka)
    implementation(libs.spring.integration.kafka)

    implementation(libs.lettuce.core)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.jackson)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.resilience4j.springboot3)
    implementation(libs.resilience4j.kotlin)
    implementation(libs.resilience4j.retry)
    implementation(libs.resilience4j.circuitbreaker)

    implementation(libs.caffeine)
    implementation(libs.jackson.datatype.hibernate5.jakarta)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)

    implementation(libs.micrometer.registry.prometheus)

    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation(libs.spring.boot.starter.test) {
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
    testImplementation(libs.assertk)
    testImplementation(libs.mockito.kotlin)
}