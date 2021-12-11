import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("org.jetbrains.dokka") version "1.6.0"
    id("com.diffplug.spotless") version "6.0.0"

    kotlin("jvm") version "1.6.0" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.diffplug.spotless")

    tasks.withType<KotlinCompile> {
        dependsOn("spotlessApply")
    }

    tasks.dokkaHtml.configure {
        // intentionally hide inherited members, otherwise we will have a lot of functions inherited from JNA
        suppressInheritedMembers.set(true)
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
    group = "io.github.oxisto"
    version = "0.0.0-SNAPSHOT"
}

tasks.dokkaHtmlCollector.configure{
    // intentionally hide inherited members, otherwise we will have a lot of functions inherited from JNA
    suppressInheritedMembers.set(true)
}
