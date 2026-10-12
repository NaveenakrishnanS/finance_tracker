package com.financetracker.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests enforcing the layered architecture rules for the Finance Tracker application.
 */
class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .importPackages("com.financetracker");
    }

    /**
     * Controllers must not directly depend on repository classes.
     */
    @Test
    void controllersMustNotDependOnRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("..repository..");
        rule.check(importedClasses);
    }

    /**
     * Services must not depend on HTTP types.
     */
    @Test
    void servicesMustNotDependOnHttpTypes() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta.servlet..", "org.springframework.web.servlet..");
        rule.check(importedClasses);
    }

    /**
     * All RestController classes must reside in the controller package.
     */
    @Test
    void restControllersMustResideInControllerPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(
                        org.springframework.web.bind.annotation.RestController.class)
                .should().resideInAPackage("..controller..");
        rule.check(importedClasses);
    }

    /**
     * All Service-annotated classes must reside in the service package.
     */
    @Test
    void servicesMustResideInServicePackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(
                        org.springframework.stereotype.Service.class)
                .should().resideInAPackage("..service..");
        rule.check(importedClasses);
    }

    /**
     * All JpaRepository extensions must reside in the repository package.
     */
    @Test
    void repositoriesMustResideInRepositoryPackage() {
        ArchRule rule = classes()
                .that().areAssignableTo(
                        org.springframework.data.jpa.repository.JpaRepository.class)
                .should().resideInAPackage("..repository..");
        rule.check(importedClasses);
    }

    /**
     * All Entity-annotated classes must reside in the model package.
     */
    @Test
    void entitiesMustResideInModelPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage("..model..");
        rule.check(importedClasses);
    }
}
