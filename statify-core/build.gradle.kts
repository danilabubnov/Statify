import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "org.danila"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

val springBootVersion: String by project

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
    }
}

repositories {
    mavenCentral()
}

val kafkaVersion = providers.gradleProperty("kafkaVersion").get()
val springDocOpenApiVersion = providers.gradleProperty("springDocOpenApiVersion").get()
val postgresqlVersion = providers.gradleProperty("postgresqlVersion").get()
val dotenvKotlinVersion = providers.gradleProperty("dotenvKotlinVersion").get()
val jjwtVersion = providers.gradleProperty("jjwtVersion").get()
val assertkVersion = providers.gradleProperty("assertkVersion").get()
val mockitoKotlinVersion = providers.gradleProperty("mockitoKotlinVersion").get()
val dataFakerVersion = providers.gradleProperty("dataFakerVersion").get()
val h2Version = providers.gradleProperty("h2Version").get()

dependencies {
    implementation(project(":statify-utils"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.integration:spring-integration-kafka:$kafkaVersion")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springDocOpenApiVersion}")

    implementation("org.postgresql:postgresql:$postgresqlVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-hibernate5-jakarta")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("io.github.cdimascio:dotenv-kotlin:${dotenvKotlinVersion}")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("io.jsonwebtoken:jjwt-api:${jjwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jjwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jjwtVersion}")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertkVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("net.datafaker:datafaker:${dataFakerVersion}")
    testImplementation("com.h2database:h2:${h2Version}")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict"))
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.test {
    useJUnitPlatform()
}