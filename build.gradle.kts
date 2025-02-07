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
	// Add Lombok dependency
	compileOnly("org.projectlombok:lombok:1.18.30")
	annotationProcessor("org.projectlombok:lombok:1.18.30")
	runtimeOnly ("com.mysql:mysql-connector-j")
	implementation("com.apple.itunes.storekit:app-store-server-library:3.3.0")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
	implementation("software.amazon.awssdk:secretsmanager:2.29.29")
	implementation("software.amazon.awssdk:auth:2.29.29")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("software.amazon.awssdk:s3:2.29.26")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.2.0")
	implementation("net.javacrumbs.shedlock:shedlock-spring:6.2.0")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
