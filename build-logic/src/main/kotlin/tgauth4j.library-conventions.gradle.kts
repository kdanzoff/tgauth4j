// Conventions des modules de tgauth4j : Kotlin à API explicite, bytecode Java 17 (minimum de Spring Boot 4),
// publication Maven (jar, sources, POM) sous io.github.kdanzoff.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-library`
    `maven-publish`
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(25)
    explicitApi()
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        freeCompilerArgs.addAll("-Xjdk-release=17", "-Xjsr305=strict")
    }
}

java {
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}

dependencies {
    val springBootBom = platform(libs.findLibrary("spring-boot-dependencies").get())
    "implementation"(springBootBom)
    "compileOnly"(springBootBom)
    "testImplementation"(springBootBom)

    "testImplementation"(libs.findLibrary("junit-jupiter").get())
    "testImplementation"(libs.findLibrary("assertj-core").get())
    "testRuntimeOnly"(libs.findLibrary("junit-platform-launcher").get())
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name = project.name
                description = "Telegram Mini Apps and Login Widget authentication for the JVM: init data verification, " +
                    "Spring Security integration and Spring Boot starter."
                url = "https://github.com/kdanzoff/tgauth4j"
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
                scm {
                    url = "https://github.com/kdanzoff/tgauth4j"
                    connection = "scm:git:https://github.com/kdanzoff/tgauth4j.git"
                }
            }
        }
    }
}
