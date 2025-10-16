plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.1.21"
    id("com.google.devtools.ksp") version "2.1.20-1.0.32"
    id("com.gradleup.shadow") version "8.3.9"
    id("io.micronaut.application") version "4.6.0"
    id("io.micronaut.aot") version "4.6.0"
}

version = "0.1"
group = "com.eddgrant.lan2rfgatewaystats"

val micronautVersion by properties
val kotlinVersion by properties

repositories {
    mavenCentral()
}

dependencies {
    ksp("io.micronaut:micronaut-http-validation")

    //implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("ch.qos.logback:logback-classic")

    /**
     * Despite using the Micronaut CLI to select Log4J2, the configuration
     * doesn't seem to work and results in a no-op logger being configured,
     * so I've moved back to Logback for now.
     */
    /*implementation(platform("org.apache.logging.log4j:log4j-bom:2.24.3"))
    implementation("org.apache.logging.log4j:log4j-api")
    runtimeOnly("org.apache.logging.log4j:log4j-core")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")*/

    runtimeOnly("org.yaml:snakeyaml") // Required in order to support YAML Micronaut configuration. Consider using .properties format to reduce dependencies and image size.

    // Micronaut Serialisation
    ksp("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut.serde:micronaut-serde-jackson")

    implementation("com.influxdb:influxdb-client-kotlin:7.3.0")

    testImplementation("org.testcontainers:influxdb:1.21.3")

    annotationProcessor("io.micronaut.validation:micronaut-validation-processor")
    implementation("io.micronaut.validation:micronaut-validation")
}


application {
    mainClass = "com.eddgrant.lan2rfgatewaystats.ApplicationKt"
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

/**
 * Important: Mocking of non-interface beans is currently broken and undocumented
 * in Micronaut and requires this configuration in order to work.
 * https://github.com/micronaut-projects/micronaut-core/issues/3972
 */
allOpen {
    annotations("jakarta.inject.Singleton")
}

graalvmNative.toolchainDetection = true
micronaut {
    version("$micronautVersion")
    runtime("netty")
    testRuntime("kotest5")
    processing {
        module(project.name)
        group(project.group.toString())
        incremental(true)
        annotations("com.eddgrant.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}


tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    filter {
        excludeTestsMatching("*IntegrationTest")
    }
    ignoreFailures = true
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    useJUnitPlatform()
    filter {
        includeTestsMatching("*Integration*")
    }
    mustRunAfter(tasks.test)
}

tasks.check {
    dependsOn(integrationTestTask)
}