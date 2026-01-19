plugins {
    kotlin("jvm")
    alias(backendLibs.plugins.kotlin.serialization)
    alias(backendLibs.plugins.kotlin.spring)
    alias(backendLibs.plugins.spring.boot)
    alias(backendLibs.plugins.spring.dependencyManagement)
}
val springModulithVersion by extra("1.4.6")

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
    implementation(platform(backendLibs.spring.modulith.bom))

    implementation(backendLibs.bundles.spring.boot)
    implementation(backendLibs.bundles.spring.modulith)
    implementation(backendLibs.bundles.kotlin)
    implementation(backendLibs.bundles.flyway)
    implementation(backendLibs.bundles.reactor)
    implementation(backendLibs.postgresql)
    implementation(backendLibs.jackson.module.kotlin)
    implementation(backendLibs.java.jwt)
    implementation(backendLibs.aws.sdk.s3)

    developmentOnly(backendLibs.bundles.spring.boot.dev)

    runtimeOnly(backendLibs.spring.modulith.actuator)
    runtimeOnly(backendLibs.spring.modulith.observability)

    annotationProcessor(backendLibs.spring.boot.configuration.processor)

    testImplementation(backendLibs.bundles.test)
    testRuntimeOnly(backendLibs.junit.platform.launcher)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:$springModulithVersion")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
