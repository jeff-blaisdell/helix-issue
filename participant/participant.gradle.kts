
val spockVersion: String by project
val springBootVersion: String by project

plugins {
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.apache.helix:helix-core:1.0.1")


    // spring
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.spockframework:spock-core:$spockVersion")
    testImplementation("org.spockframework:spock-spring:$spockVersion")
    testImplementation("javax.servlet:javax.servlet-api:3.1.0")
    testImplementation("commons-io:commons-io:2.6")
    testImplementation("cglib:cglib-nodep:3.2.8")
    testImplementation("org.objenesis:objenesis:3.0")
}

sourceSets {
    getByName("test").java.srcDirs("src/test/groovy")
    getByName("test").resources.srcDirs("src/test/groovy")
}

tasks {

    bootJar {
        mainClassName = "helix.ControllerApplication"
    }

    bootRun {
        jvmArgs = listOf("-Dspring.config.name=participant")
    }

    compileJava {
        options.compilerArgs.add("-Xlint:unchecked")
    }
}
