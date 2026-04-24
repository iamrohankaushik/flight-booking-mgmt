import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "2.1.0" apply false
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.jooq.jooq-codegen-gradle") version "3.19.11" apply false
}

allprojects {
    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")

    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.1")
        }
    }

    dependencies {
        add("implementation", "org.jetbrains.kotlin:kotlin-reflect")
        add("implementation", "com.fasterxml.jackson.module:jackson-module-kotlin")
        add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit5")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
}
