buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.postgresql:postgresql:42.7.2")
        classpath("org.flywaydb:flyway-database-postgresql:10.21.0")
    }
}

plugins {
    id("org.jooq.jooq-codegen-gradle")
    id("org.flywaydb.flyway") version "10.21.0"
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-jooq")
    api("org.jooq:jooq")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Ensure the generator has the extensions
    jooqCodegen("org.jooq:jooq-meta-extensions:3.19.15")
}

jooq {
    configuration {
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                properties {
                    property {
                        key = "scripts"
                        value = "src/main/resources/db/migration/V1__Initial_Schema.sql"
                    }
                    property {
                        key = "sort"
                        value = "semantic"
                    }
                }
            }
            target {
                packageName = "com.example.demo.jooq"
                directory = "build/generated-sources/jooq"
            }
        }
    }
}

sourceSets {
    main {
        kotlin {
            srcDir("build/generated-sources/jooq")
        }
    }
}

flyway {
    url = "jdbc:postgresql://localhost:5432/flight_booking"
    user = "postgres"
    password = "postgres"
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}
