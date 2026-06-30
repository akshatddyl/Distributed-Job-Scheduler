plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":shared"))

    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
}
