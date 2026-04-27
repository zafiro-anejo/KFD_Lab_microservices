plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "microservices"

include("service1")
include("service2")
include("gateway")