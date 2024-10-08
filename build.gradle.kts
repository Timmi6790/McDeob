import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    java
    application
    id("io.freefair.lombok") version "8.10"
    id("com.diffplug.spotless") version "6.25.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.sentry.jvm.gradle") version "4.11.0"
}

group = "com.shanebeestudios"
// x-release-please-start-version
version = "2.6.0"
// x-release-please-end
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
    implementation("mx.kenzie:mirror:5.0.5")
    implementation("io.github.lxgaming:reconstruct-common:1.3.26")
    implementation("org.vineflower:vineflower:1.10.1")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.google.dagger:dagger:2.52")
    annotationProcessor("com.google.dagger:dagger-compiler:2.52")
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
        plugin("com.github.johnrengelman.shadow")
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