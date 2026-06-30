plugins {
    id("org.springframework.boot")
}

// Import Spring Cloud BOM for gateway dependency management
the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
    }
}

dependencies {
    // Spring Cloud Gateway (reactive — uses WebFlux, NOT servlet)
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    // JWT validation
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}
