plugins {
    id("com.github.davidmc24.gradle.plugin.avro") version "1.3.0"

    kotlin("jvm") version "1.6.0"
}

group = "com.example.cloud.stream"
version = "1.0.0"

dependencies {
    implementation("org.apache.avro:avro:1.11.0")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}