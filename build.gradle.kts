/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java project to get you started.
 * For more details take a look at the Java Quickstart chapter in the Gradle
 * User Manual available at https://docs.gradle.org/5.5.1/userguide/tutorial_java_projects.html
 */

plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building a CLI application
    application
    id("org.openjfx.javafxplugin") version "0.0.8"
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    mavenCentral()
}

dependencies {
    // This dependency is used by the application.
    implementation("com.google.guava:guava:27.1-jre")

    // Apache POI - Java API To Access Microsoft Format Files
    // https://mvnrepository.com/artifact/org.apache.poi/poi
    compile("org.apache.poi:poi:4.1.0")
    compile("org.apache.poi:poi-ooxml:4.1.0")

    // MySQL Connector/J » 8.0.17
    // https://mvnrepository.com/artifact/mysql/mysql-connector-java/8.0.17
    // compile("mysql:mysql-connector-java:8.0.17")

    // MongoDB Java Driver » 3.11.0-rc0
    // https://mvnrepository.com/artifact/org.mongodb/mongo-java-driver
    compile("org.mongodb:mongo-java-driver:3.11.0-rc0")

    // Use JUnit test framework
    testImplementation("junit:junit:4.12")
}

application {
    // Define the main class for the application
    mainClassName = "KeywordSearchEngine.model.App"
}

javafx {
    modules("javafx.controls", "javafx.fxml")
}