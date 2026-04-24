plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "demo"

include("common")
include("flight-booking")
include("flight-searching")
