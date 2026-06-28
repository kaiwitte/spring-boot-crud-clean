import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.14.0"
    id("com.diffplug.spotless") version "8.4.0"
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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")

    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    // Important: lombok-mapstruct-binding is needed when using Lombok Builders with MapStruct
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

spotless {
    java {
        targetExclude("build/generated/**/*.java")
        palantirJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.3.1")
    }
    yaml {
        target("**/*.yaml", "**/*.yml")
        targetExclude("build/**/*.yaml", "build/**/*.yml")
        jackson()
    }
    format("markdown") {
        target("**/*.md")
        targetExclude(
            "build/**/*.md",
            "src/frontend/src/src/generated/**/*.md",
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set(file("src/main/resources/cruddemo.yaml").toURI().toString())
    outputDir.set(
        layout.buildDirectory
            .dir("generated")
            .get()
            .asFile.absolutePath,
    )
    apiPackage.set("com.witteconsulting.cruddemo.api")
    modelPackage.set("com.witteconsulting.cruddemo.model")
    modelNameSuffix.set("Dto")
    generateApiTests.set(false)
    generateModelTests.set(false)
    configOptions.set(
        mapOf(
            "delegatePattern" to "true",
            "useSpringBoot3" to "true",
        ),
    )
}

// UNREVIEWED_AI_CODE
val openApiGenerateTypeScript =
    tasks.register<GenerateTask>("openApiGenerateTypeScript") {
        generatorName.set("typescript-angular")
        inputSpec.set(file("src/main/resources/cruddemo.yaml").toURI().toString())
        // todo: more suitable output directory
        outputDir.set(file("src/frontend/src/src/generated").absolutePath)
        apiPackage.set("api")
        modelPackage.set("model")
        generateApiTests.set(false)
        generateModelTests.set(false)
        configOptions.set(
            mapOf(
                "ngVersion" to "21.2.0",
                "providedIn" to "root",
            ),
        )
        globalProperties.set(
            mapOf(
                "apiDocs" to "false",
                "modelDocs" to "false",
            ),
        )
    }

tasks.named("openApiGenerate") {
    dependsOn(openApiGenerateTypeScript)
}

// Add generated sources to source sets
sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/src/main/java"))
        }
    }
}

// Configure task dependencies
tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

tasks.register<Delete>("deleteOpenApiTests") {
    delete(layout.buildDirectory.dir("generated/src/test"))
    mustRunAfter("openApiGenerate")
}

tasks.named("compileTestJava") {
    dependsOn("deleteOpenApiTests")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
    }
}

springBoot {
    mainClass.set("com.witteconsulting.cruddemo.CruddemoApplication")
}
