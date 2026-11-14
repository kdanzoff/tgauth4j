pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "tgauth4j"

include(
    "tgauth4j-core",
    "tgauth4j-spring-security",
    "tgauth4j-spring-boot-starter",
    "tgauth4j-test",
)
