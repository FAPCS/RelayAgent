import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    application

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.fapcs"
version = "0.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api("me.fapcs:Shared:0.0.0")

    api("io.javalin:javalin:5.3.2")

    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.19.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "16"
        }
    }

    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveClassifier.set("")
    }
}

application {
    mainClass.set("me.fapcs.relay_agent.RelayAgent")
}