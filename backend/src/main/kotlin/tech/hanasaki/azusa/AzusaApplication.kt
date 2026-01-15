package tech.hanasaki.azusa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["tech.hanasaki"])
@ConfigurationPropertiesScan("tech.hanasaki")
class AzusaApplication

fun main(args: Array<String>) {
    runApplication<AzusaApplication>(*args)
}
