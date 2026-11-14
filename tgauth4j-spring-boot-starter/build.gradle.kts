// Autoconfiguration Spring Boot : propriétés tgauth4j.*, vérificateur et jeton du bot.
plugins {
    id("tgauth4j.library-conventions")
}

dependencies {
    api(project(":tgauth4j-spring-security"))
    implementation(libs.spring.boot.autoconfigure)
    compileOnly(libs.jakarta.servlet.api)

    testImplementation(project(":tgauth4j-test"))
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.spring.test)
    testImplementation(libs.jakarta.servlet.api)
    testImplementation(libs.archunit.junit5)
}
