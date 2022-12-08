plugins {
    id("java")
    id("com.github.spotbugs")
}

version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    compileOnly("org.projectlombok:lombok:1.18.24")
    compileOnly("org.checkerframework:checker-qual:3.25.0")
    runtimeOnly("com.google.code.findbugs:jsr305:3.0.2")
    implementation(project(":e2e-api"))
}
