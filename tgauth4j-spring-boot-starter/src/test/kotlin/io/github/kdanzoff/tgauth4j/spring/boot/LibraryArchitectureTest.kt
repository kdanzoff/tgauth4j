package io.github.kdanzoff.tgauth4j.spring.boot

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

/** Dépendances entre les modules de la bibliothèque. */
class LibraryArchitectureTest {

    private val classes = ClassFileImporter().withImportOption(ImportOption.DoNotIncludeTests()).importPackages(ROOT)

    @Test
    fun `the core depends neither on Spring nor on the servlet API`() {
        noClasses().that().resideInAPackage("$ROOT.core..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.servlet..")
            .check(classes)
    }

    @Test
    fun `the Spring Security integration does not depend on Spring Boot`() {
        noClasses().that().resideInAPackage("$ROOT.spring.security..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework.boot..", "$ROOT.spring.boot..")
            .check(classes)
    }

    @Test
    fun `the library code does not depend on its test helpers`() {
        noClasses().that().resideOutsideOfPackage("$ROOT.test..")
            .should().dependOnClassesThat().resideInAPackage("$ROOT.test..")
            .check(classes)
    }

    private companion object {
        const val ROOT = "io.github.kdanzoff.tgauth4j"
    }
}
