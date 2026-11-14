// Aides de test : initData signés comme par Telegram, en-tête MockMvc.
plugins {
    id("tgauth4j.library-conventions")
}

dependencies {
    implementation(libs.jackson.databind)
    compileOnly(libs.spring.test)
    compileOnly(libs.jakarta.servlet.api)
}
