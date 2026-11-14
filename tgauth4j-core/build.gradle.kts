// initData verification, without Spring.
plugins {
    id("tgauth4j.library-conventions")
}

dependencies {
    // Reads the JSON `user` field of initData.
    implementation(libs.jackson.databind)

    testImplementation(project(":tgauth4j-test"))
}
