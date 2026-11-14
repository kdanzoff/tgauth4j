// Intégration Spring Security : jeton, fournisseur, configurateur et DSL Kotlin telegram { }.
plugins {
    id("tgauth4j.library-conventions")
}

dependencies {
    api(project(":tgauth4j-core"))
    api(libs.spring.security.config)
    api(libs.spring.security.web)
    compileOnly(libs.jakarta.servlet.api)

    testImplementation(project(":tgauth4j-test"))
    testImplementation(libs.spring.webmvc)
    testImplementation(libs.spring.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.jakarta.servlet.api)
}
