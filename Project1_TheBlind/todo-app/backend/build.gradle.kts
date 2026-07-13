plugins {
	java
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.theblind"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	testImplementation("io.rest-assured:rest-assured:6.0.0")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
	implementation("org.xerial:sqlite-jdbc:3.53.2.0")
	implementation("org.hibernate.orm:hibernate-community-dialects:8.0.0.Alpha1")
	implementation("io.jsonwebtoken:jjwt-api:0.13.0")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
	testImplementation("com.h2database:h2:2.4.240")
	// Spring Starter Security Dependencies
    implementation("org.springframework.boot:spring-boot-starter-security")
	testImplementation("org.springframework.security:spring-security-test")

	// E2E testing: Cucumber + Selenium + WebDriverManager
	testImplementation("io.cucumber:cucumber-java:7.20.1")
	testImplementation("io.cucumber:cucumber-junit-platform-engine:7.20.1")
	testImplementation("io.cucumber:cucumber-spring:7.20.1")
	testImplementation("org.seleniumhq.selenium:selenium-java:4.25.0")
	testImplementation("io.github.bonigarcia:webdrivermanager:5.9.2")
	testImplementation("org.junit.platform:junit-platform-suite:1.11.4")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// ── Run only child_tasks.feature: ./gradlew childTaskTest ──
// Generates a junit-platform.properties with tag filter, prepends to classpath

val childTaskTestResourceDir = layout.buildDirectory.dir("childTaskTestResources")

val generateChildTaskProps by tasks.registering {
	doLast {
		val dir = childTaskTestResourceDir.get().asFile
		dir.mkdirs()
		val props = dir.resolve("junit-platform.properties")
		props.writeText(
			"cucumber.filter.tags=@child-tasks\n" +
			"cucumber.glue=com.theblind.todo.e2e.steps,com.theblind.todo.e2e\n" +
			"cucumber.features=classpath:features\n" +
			"cucumber.plugin=pretty\n"
		)
	}
}

tasks.register<Test>("childTaskTest") {
	description = "Runs only child task Cucumber feature"
	group = "verification"

	dependsOn(generateChildTaskProps)

	// Point to test source set's compiled classes and classpath
	testClassesDirs = sourceSets["test"].output.classesDirs
	classpath = files(childTaskTestResourceDir) + sourceSets["test"].runtimeClasspath

	useJUnitPlatform {
		includeEngines("cucumber")
	}

	systemProperty("e2e.baseUrl", "http://localhost:4200")
}
