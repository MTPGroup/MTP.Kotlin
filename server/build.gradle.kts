plugins {
    kotlin("jvm")
    application
    alias(libs.plugins.kotlinSerialization)
}

group = "tech.hanasaki"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation(platform(serverLibs.koin.bom))
    implementation(serverLibs.bundles.koin)
    implementation(serverLibs.bundles.ktor)
    implementation(serverLibs.bundles.exposed)
    implementation(serverLibs.bundles.database)
    implementation(serverLibs.bundles.service)

    testImplementation(serverLibs.bundles.test)
}
