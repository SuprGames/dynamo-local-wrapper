import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.0"
    id("io.spring.dependency-management") version ("1.0.9.RELEASE")
    `maven-publish`
}

group = "io.suprgames"

description = "DynamoDB Test Provider"

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven("http://dynamodb-local.s3-website-us-west-2.amazonaws.com/release")
}

dependencyManagement {
    imports {
        mavenBom("software.amazon.awssdk:bom:2.13.18")
    }
}

dependencies {
    api("software.amazon.awssdk:dynamodb")
    api("com.amazonaws:DynamoDBLocal:1.13.1")
    api("com.amazonaws:aws-lambda-java-core:1.2.1")
    api("com.amazonaws:aws-lambda-java-log4j2:1.2.0")
    api("org.slf4j:slf4j-simple:1.7.30")
    api("com.github.stefanbirkner:system-rules:1.19.0")
    testImplementation(kotlin("test-junit"))
}

publishing {
    publications {
        create<MavenPublication>("kotlin") {
            groupId = "io.suprgames"
            artifactId = "dynamodb-local-wrapper"
            if (!System.getenv("NEW_VERSION").isNullOrBlank()) {
                version = System.getenv("NEW_VERSION")
            }
            from(components["kotlin"])

        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/suprgames/dynamodb-test-provider")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
