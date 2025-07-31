plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))

    implementation(libs.spring.boot.starter)

    api(libs.jackson.module.kotlin)
    api(libs.jackson.datatype.jsr310)
    api(libs.kotlin.logging.jvm)

    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation(libs.spring.boot.starter.test) {
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
    testImplementation(libs.assertk)
    testImplementation(libs.mockito.kotlin)
}