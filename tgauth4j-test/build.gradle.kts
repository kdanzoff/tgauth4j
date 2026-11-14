// Test helpers: initData signed the way Telegram does, MockMvc header.
plugins {
    id("tgauth4j.library-conventions")
}

dependencies {
    implementation(libs.jackson.databind)
    compileOnly(libs.spring.test)
    compileOnly(libs.jakarta.servlet.api)
}
