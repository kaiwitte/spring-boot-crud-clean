plugins {
	java
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.14.0"
}

group = "com.witteconsulting"
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
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

openApiGenerate {
	generatorName.set("spring")
	inputSpec.set("$rootDir/src/main/resources/cruddemo.yaml")
	outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)
	apiPackage.set("com.witteconsulting.cruddemo.api")
	modelPackage.set("com.witteconsulting.cruddemo.model")
	modelNameSuffix.set("Dto")
	generateApiTests.set(false)
	generateModelTests.set(false)
	configOptions.set(
		mapOf(
			"delegatePattern" to "true",
			"useSpringBoot3" to "true"
		)
	)
}

// Define a task to delete the unwanted generated test folder
val deleteOpenApiTests by tasks.registering(Delete::class) {
	delete(layout.buildDirectory.dir("generated/src/test").get().asFile.absolutePath)
}
tasks.named("compileTestJava") {
	dependsOn(deleteOpenApiTests)
}
tasks.named("deleteOpenApiTests") {
	mustRunAfter(tasks.named("openApiGenerate"))
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		showStandardStreams = true
		events("passed", "skipped", "failed")
	}
}
