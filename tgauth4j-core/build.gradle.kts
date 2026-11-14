// Vérification d'initData, sans Spring.
plugins {
    id("tgauth4j.library-conventions")
}

dependencies {
    // Lecture du champ JSON `user` d'initData.
    implementation(libs.jackson.databind)

    testImplementation(project(":tgauth4j-test"))
}
