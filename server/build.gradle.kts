import io.ktor.plugin.features.*

plugins {
    alias(serverLibs.plugins.kotlinJvm)
    alias(serverLibs.plugins.kotlinSerialization)
    alias(serverLibs.plugins.ktor)
    application
}

group = "tech.hanasaki"
version = "0.1.6"

application {
    mainClass.set("tech.hanasaki.azusa.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=$isDevelopment",
        "--enable-native-access=ALL-UNNAMED",
    )
}

ktor {
    openApi {
        enabled = true
        codeInferenceEnabled = false
    }
    docker {
        jreVersion.set(JavaVersion.VERSION_25)
        localImageName.set("azusa")
        imageTag.set(version.toString())
        externalRegistry.set(
            DockerImageRegistry.dockerHub(
                appName = provider { "azusa" },
                username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
                password = providers.environmentVariable("DOCKER_HUB_PASSWORD"),
            )
        )
    }
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("io.ktor.utils.io.ExperimentalKtorApi")
    }
}

dependencies {
    implementation(platform(serverLibs.koin.bom))
    implementation(platform(serverLibs.langchain4j.bom))
    implementation(serverLibs.bundles.koin)
    implementation(serverLibs.bundles.ktor)
    implementation(serverLibs.bundles.kotlinx)
    implementation(serverLibs.bundles.exposed)
    implementation(serverLibs.bundles.database)
    implementation(serverLibs.bundles.service)
    implementation(serverLibs.bundles.logging)
    implementation(serverLibs.bundles.agent)

    runtimeOnly(libs.netty.resolver.dns.classes.macos)
    runtimeOnly(libs.netty.resolver.dns.native.macos)
    runtimeOnly(libs.netty.resolver.dns.native.macos)

    testImplementation(platform(serverLibs.testcontainers.bom))
    testImplementation(kotlin("test-junit5"))
    testImplementation(serverLibs.bundles.test)

    configurations.all {
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    shadowJar {
        mergeServiceFiles {
            include("META-INF/services/**")
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }
}
