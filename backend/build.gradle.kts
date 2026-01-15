plugins {
    kotlin("jvm")
    alias(backendLibs.plugins.kotlin.serialization)
    alias(backendLibs.plugins.kotlin.spring)
    alias(backendLibs.plugins.spring.boot)
    alias(backendLibs.plugins.spring.dependencyManagement)
}

group = "tech.hanasaki"
version = "0.0.1-SNAPSHOT"
description = "backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation(platform(backendLibs.spring.boot.bom))

    implementation(backendLibs.spring.boot.starter.actuator)
    implementation(backendLibs.spring.boot.starter.data.jdbc)
    implementation(backendLibs.spring.boot.starter.mail)
    implementation(backendLibs.spring.boot.starter.oauth2.resource.server)
    implementation(backendLibs.spring.boot.starter.security)
    implementation(backendLibs.spring.boot.starter.validation)
    implementation(backendLibs.spring.boot.starter.web)
    implementation(backendLibs.jackson.module.kotlin)
    implementation(backendLibs.kotlinx.serialization.json)
    implementation(backendLibs.flyway.core)
    implementation(backendLibs.flyway.database.postgresql)
    implementation(backendLibs.kotlin.reflect)
    implementation(backendLibs.kotlinx.datetime)
    implementation(backendLibs.kotlinx.coroutines.core)
    implementation(backendLibs.java.jwt)
    implementation(backendLibs.bcrypt)
    implementation(backendLibs.aws.sdk.s3)

    developmentOnly(backendLibs.spring.boot.devtools)
    developmentOnly(backendLibs.spring.boot.docker.compose)

    runtimeOnly(backendLibs.postgresql)

    annotationProcessor(backendLibs.spring.boot.configuration.processor)

    testImplementation(backendLibs.spring.boot.starter.test)
    testImplementation(backendLibs.kotlin.test.junit5)
    testImplementation(backendLibs.spring.security.test)
    testRuntimeOnly(backendLibs.junit.platform.launcher)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
