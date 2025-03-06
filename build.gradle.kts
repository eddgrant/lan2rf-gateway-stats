plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.1.0"
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.5"
    id("io.micronaut.aot") version "4.4.5"
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


