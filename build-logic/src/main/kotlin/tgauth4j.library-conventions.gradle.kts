// tgauth4j module conventions: Kotlin explicit API, Java 17 bytecode (Spring Boot 4 minimum),
// Maven Central publication (jar, sources, Javadoc, signed POM) under io.github.kdanzoff.
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-library`
    id("org.jetbrains.dokka-javadoc")
    id("com.vanniktech.maven.publish")
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

mavenPublishing {
    configure(KotlinJvm(javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationJavadoc"), sourcesJar = true))
    // Upload to the Central Portal; the deployment is released by hand on central.sonatype.com.
    publishToMavenCentral()
    // Signed only with a key (signingInMemoryKey / signingInMemoryKeyPassword, CI secrets), so that
    // publishToMavenLocal works without one; Maven Central refuses unsigned artifacts.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) signAllPublications()
    coordinates(project.group.toString(), project.name, project.version.toString())
    pom {
        name = project.name
        description = "Telegram Mini Apps and Login Widget authentication for the JVM: init data verification, " +
            "Spring Security integration and Spring Boot starter."
        url = "https://github.com/kdanzoff/tgauth4j"
        inceptionYear = "2026"
        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "kdanzoff"
                name = "kdanzoff"
                url = "https://github.com/kdanzoff"
            }
        }
        scm {
            url = "https://github.com/kdanzoff/tgauth4j"
            connection = "scm:git:https://github.com/kdanzoff/tgauth4j.git"
            developerConnection = "scm:git:ssh://git@github.com/kdanzoff/tgauth4j.git"
        }
        issueManagement {
            system = "GitHub"
            url = "https://github.com/kdanzoff/tgauth4j/issues"
        }
    }
}

// Writes the versions resolved from the Spring Boot BOM in the POM, so that Maven users get explicit versions.
publishing {
    publications.withType<MavenPublication>().configureEach {
        versionMapping {
            allVariants { fromResolutionResult() }
        }
    }
}
