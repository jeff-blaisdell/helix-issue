

// For Gradle itself (i.e. changes to how Gradle is able to perform the build).
buildscript {
    val springBootVersion: String by project

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
    }
}

apply(plugin = "idea")

allprojects {
    apply(plugin = "java")
}

// For the modules being built by Gradle.
subprojects {

    apply(plugin = "groovy")

    group = "helix"
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven { url = uri("http://dl.bintray.com/kotlin/kotlinx") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}
