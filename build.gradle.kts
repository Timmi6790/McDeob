import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    java
    application
    id("io.freefair.lombok") version "8.14.2"
    id("com.diffplug.spotless") version "7.2.1"
    id("com.gradleup.shadow") version "9.1.0"
    id("io.sentry.jvm.gradle") version "5.11.0"
}

group = "com.shanebeestudios"
project.version = file("version.txt").readText().trim().replace("\n", "")
description = "McDeob"

application {
    mainClass = "com.shanebeestudios.mcdeop.McDeob"
}

repositories {
    mavenCentral()

    maven("https://jitpack.io")
    maven("https://repo.kenzie.mx/releases")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    implementation(project(":common", "shadow"))
    implementation(project(":launchermeta", "shadow"))
    implementation(libs.mirror)
    implementation(libs.reconstruct.common)
    implementation(libs.vineflower)
    implementation(libs.jopt.simple)
    implementation(libs.slf4j.simple)
    implementation(libs.okhttp)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)
}

tasks {
    shadowJar {
        manifest.attributes["Implementation-Version"] = project.version
    }
}

allprojects {
    apply {
        plugin("java")
        plugin("com.diffplug.spotless")
        plugin("io.freefair.lombok")
        plugin("com.gradleup.shadow")
    }

    java.sourceCompatibility = JavaVersion.VERSION_21

    repositories {
        mavenCentral()
    }

    spotless {
        java {
            // Use the default importOrder configuration
            importOrder()
            removeUnusedImports()

            // Cleanthat will refactor your code, but it may break your style: apply it before your formatter
            cleanthat()

            palantirJavaFormat()

            formatAnnotations() // fixes formatting of type annotations
        }

        kotlinGradle {
            ktlint()
        }

        yaml {
            target("*.yaml")
            jackson()
        }
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        withType<Javadoc> {
            options.encoding = "UTF-8"
        }

        withType<ShadowJar> {
            // https://github.com/johnrengelman/shadow/issues/857
            // archiveClassifier.set("")

            // dependsOn("distTar", "distZip")
        }
    }
}