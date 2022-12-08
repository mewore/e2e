plugins {
    id("java")
    id("jacoco")
    id("com.github.spotbugs")
    id("maven-publish")
}

group = "moe.mewore.e2e"
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    compileOnly("org.projectlombok:lombok:1.18.24")
    compileOnly("org.checkerframework:checker-qual:3.25.0")

    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")
    testCompileOnly("org.projectlombok:lombok:1.18.24")
    testCompileOnly("org.checkerframework:checker-qual:3.25.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testRuntimeOnly("com.google.code.findbugs:jsr305:3.0.2")
}

tasks.spotbugsMain {
    reports.create("html")
    excludeFilter.fileValue(projectDir.toPath().resolve("spotbugs-exclude.xml").toFile())
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

jacoco {
    toolVersion = "0.8.6"
}

tasks.jacocoTestReport {
    dependsOn.add(tasks.test)
    reports.xml.required.set(true)
}

project.publishing.publications {
    create<MavenPublication>("e2eApi") {
        artifactId = "e2e-api"
        artifact(tasks.jar)
        pom {
            withXml {
                val root = asNode()
                root.appendNode("name", "e2e-api")
                root.appendNode("description", "A bare bones tool for E2E testing of HTTP applications")
                root.appendNode("url", "https://github.com/mewore/e2e")
            }
        }
    }
}
