// initData verification, without Spring.
plugins {
    id("tgauth4j.library-conventions")
}

dependencies {
    // Reads the JSON `user` field of initData.
    implementation(libs.jackson.databind)
    // Catches parsing failures as a Result.
    implementation(libs.kotlin.result)
    // TRACE logs, for developers who want to follow a verification.
    implementation(libs.slf4j.api)

    testImplementation(project(":tgauth4j-test"))
}
