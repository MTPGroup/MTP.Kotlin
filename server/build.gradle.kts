plugins {
    kotlin("jvm")
    application
    alias(serverLibs.plugins.kotlinSerialization)
    alias(serverLibs.plugins.ktor)
}

group = "tech.hanasaki"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

//ktor {
//    openApi {
//        enabled = true
//        codeInferenceEnabled = true
//        onlyCommented = false
//    }
//}

kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}

dependencies {
    implementation(platform(serverLibs.koin.bom))
    implementation(serverLibs.bundles.koin)
    implementation(serverLibs.bundles.ktor)
    implementation(serverLibs.bundles.kotlinx)
    implementation(serverLibs.bundles.exposed)
    implementation(serverLibs.bundles.database)
    implementation(serverLibs.bundles.service)

    testImplementation(serverLibs.bundles.test)
}
