plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.sonarqube") version "3.3"
    id("io.freefair.lombok") version "6.3.0"
    checkstyle
    jacoco
}

allprojects {
    group = "org.kryonite"
    version = "0.1.0"

    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "org.sonarqube")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "checkstyle")
    apply(plugin = "jacoco")

    repositories {
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
    }

    dependencies {
        val junitVersion = "5.8.2"
        val paperVersion = "1.18.1-R0.1-SNAPSHOT"

        compileOnly("io.papermc.paper:paper-api:$paperVersion")

        testImplementation("io.papermc.paper:paper-api:$paperVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
        testImplementation("org.mockito:mockito-junit-jupiter:4.2.0")
    }

    tasks.test {
        finalizedBy("jacocoTestReport")
        useJUnitPlatform()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        withJavadocJar()
    }

    tasks.jacocoTestReport {
        reports {
            xml.required.set(true)
        }
    }

    checkstyle {
        toolVersion = "9.2.1"
        config = project.resources.text.fromUri("https://kryonite.org/checkstyle.xml")
    }

    sonarqube {
        properties {
            property("sonar.projectKey", "kryoniteorg_kryo-inventory-sync")
            property("sonar.organization", "kryoniteorg")
            property("sonar.host.url", "https://sonarcloud.io")
        }
    }
}
