plugins {
	java
	id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.wdyt"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Lombok
	compileOnly("org.projectlombok:lombok:1.18.30")
	annotationProcessor("org.projectlombok:lombok:1.18.30")

	// Database
	runtimeOnly("com.mysql:mysql-connector-j")

	// Apple StoreKit
	implementation("com.apple.itunes.storekit:app-store-server-library:3.3.0")

	// API Documentation
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

	// AWS SDK with BOM for version consistency
	implementation(platform("software.amazon.awssdk:bom:2.30.38"))
	implementation("software.amazon.awssdk:secretsmanager")
	implementation("software.amazon.awssdk:auth")
	implementation("software.amazon.awssdk:sns")
	implementation("software.amazon.awssdk:cloudwatchlogs")
	implementation("software.amazon.awssdk:s3")

	// Spring Boot starters
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-logging")
	implementation("org.springframework.boot:spring-boot-starter-web")

	// JSON Web Tokens
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	// ShedLock for distributed task scheduling
	implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.2.0")
	implementation("net.javacrumbs.shedlock:shedlock-spring:6.2.0")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
