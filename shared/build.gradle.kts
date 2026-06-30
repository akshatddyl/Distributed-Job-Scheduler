// The shared module is a plain Java library — it does NOT boot as a Spring app.
// Uses `api` configuration so consumers transitively get web/jpa/validation.
plugins {
    `java-library`
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-validation")
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("shared")
}
