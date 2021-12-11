import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.dokka") version "1.6.0" apply false
    id("com.diffplug.spotless") version "6.0.0"

    kotlin("jvm") version "1.6.0" apply false
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    tasks.withType<KotlinCompile> {
        dependsOn("spotlessApply")
    }

    spotless {
        kotlin {
            ktfmt().kotlinlangStyle()
        }
    }
}

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "org.jetbrains.dokka")

    group = "io.github.oxisto"
    version = "0.0.0-SNAPSHOT"
}
