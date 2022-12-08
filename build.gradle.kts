plugins {
    id("com.dorongold.task-tree") version "2.1.0"
    id("java")
    id("jacoco")
    id("com.github.spotbugs") version "4.7.3"
    id("maven-publish")
}

java.sourceCompatibility = JavaVersion.VERSION_15
java.targetCompatibility = JavaVersion.VERSION_15

group = "moe.mewore.e2e"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    compileOnly("org.projectlombok:lombok:1.18.24")
    compileOnly("org.checkerframework:checker-qual:3.25.0")
    runtimeOnly("com.google.code.findbugs:jsr305:3.0.2")
    implementation(project(":e2e-api"))
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.19.0")

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

tasks.jar {
    manifest {
        attributes["Main-Class"] = "moe.mewore.e2e.Main"
    }

    val sourcesMain = sourceSets.main.get()
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } + sourcesMain.output)
    duplicatesStrategy = DuplicatesStrategy.WARN
}

project.publishing.publications {
    create<MavenPublication>("e2e") {
        artifact(tasks.jar)
        pom {
            withXml {
                val root = asNode()
                root.appendNode("name", "e2e")
                root.appendNode("description", "A bare bones tool for E2E testing of HTTP applications")
                root.appendNode("url", "https://github.com/mewore/e2e")
            }
        }
    }
}

tasks.create<JavaExec>("e2eTestSpringExampleJar") {
    outputs.upToDateWhen { true }
    val e2eJarTask = tasks.jar
    dependsOn.add(e2eJarTask)
    classpath = files(e2eJarTask)

    val bootJarTask = tasks.getByPath(":spring-example:bootJar") as Jar
    dependsOn.add(bootJarTask)
    environment["E2E: /value"] = "initial"

    args = listOf(files(bootJarTask).asPath, "--spring.profiles.active=common,e2e")
}

tasks.create<JavaExec>("e2eTestSpringExampleJarCustomE2e") {
    outputs.upToDateWhen { true }
    val e2eJarTask = tasks.jar
    dependsOn.add(e2eJarTask)
    classpath = files(e2eJarTask)

    val bootJarTask = tasks.getByPath(":spring-example:bootJar") as Jar
    dependsOn.add(bootJarTask)
    environment["E2E: /value"] = "initial"

    val customE2eJarTask = tasks.getByPath(":spring-example-e2e:jar") as Jar
    dependsOn.add(customE2eJarTask)
    environment["E2E_APP_OUTPUT_TRACKER"] = "moe.mewore.e2e.springexample.e2e.CustomApplicationOutputTracker"

    args = listOf("--custom-e2e", files(customE2eJarTask).asPath, files(bootJarTask).asPath, "--spring.profiles.active=common,e2e")
}

/**
 * Displays the execution times of the executed tasks, sorted by their start and with a "time window" view of their
 * execution periods.
 *
 * Based on the answers at https://stackoverflow.com/questions/13031538/track-execution-time-per-task-in-gradle-script
 */
class TaskExecutionTimePreview : TaskExecutionListener, BuildListener {
    private var startTime: Long = -1
    private val taskStarts: MutableMap<String, Long> = mutableMapOf()
    private val taskEnds: MutableMap<String, Long> = mutableMapOf()
    private val taskStates: MutableMap<String, TaskState> = mutableMapOf()

    private fun getTime(): Long {
        return TimeUnit.MILLISECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
    }

    override fun beforeExecute(task: Task) {
        taskStarts[task.path] = getTime()
    }

    override fun afterExecute(task: Task, taskState: TaskState) {
        if (!taskState.executed || taskState.skipped || taskState.upToDate) {
            taskStarts.remove(task.path)
        } else {
            taskEnds[task.path] = getTime()
            taskStates[task.path] = taskState
        }
    }

    override fun buildFinished(result: BuildResult) {
        println("Task time:")
        val minStart = taskStarts.values.minOrNull()
        val maxEnd = taskEnds.values.maxOrNull()
        if (minStart == null || maxEnd == null) {
            return
        }

        val length = kotlin.math.max(maxEnd - minStart, 1)
        val resolution = kotlin.math.max(20, kotlin.math.min(20 + (taskStarts.entries.size / 5) * 5, 80))
        val shades = listOf(" ", "░", "▒", "▓", "█")

        val lines = mutableListOf<String>()
        for (entry in taskStarts.entries.sortedBy { it.value }) {
            val task = entry.key
            val taskState = taskStates[task]
            val start = entry.value
            val end = taskEnds[task]
            if (end == null || end < start || taskState == null) {
                println("(WARNING: Task $task has an incorrect end time or state!)")
                continue
            }
            val startNormalized = (start.toDouble() - minStart) / length
            val endNormalized = (end.toDouble() - minStart) / length
            val characters = (0 until resolution).map { i ->
                val iNormalizedFrom = i.toDouble() / resolution
                val iNormalizedTo = (i + 1).toDouble() / resolution
                val coverage = (kotlin.math.min(iNormalizedTo, endNormalized) - kotlin.math.max(
                    iNormalizedFrom, startNormalized
                )) * resolution
                shades[if (coverage > 0) kotlin.math.min((coverage * shades.size).toInt(), shades.size - 1) else 0]
            }.toMutableList()
            if (characters.count { it == " " } == characters.size) {
                characters[kotlin.math.max(
                    0, kotlin.math.min(resolution - 1, ((startNormalized + endNormalized) / 2 * resolution).toInt())
                )] = shades[1]
            }

            var formattedTime = "${kotlin.math.round((end - start) / 100.0) / 10.0}"
            if (!formattedTime.contains(".")) {
                formattedTime += ".0"
            }
            val prefix = (if (end - start <= 50) "" else "$formattedTime s").padStart(12)
            val taskSign = if (taskState.failure == null) " " else "x"
            val line = "$prefix |${characters.joinToString("")}| $taskSign $task"
            lines.add(if (taskState.failure == null) line else "<span style=\"color: indianred\">$line</span>")
            println(line)
        }
        val reportDir = result.gradle?.rootProject?.buildDir?.resolve("reports/task-durations")
        if (reportDir != null && (reportDir.exists() || reportDir.mkdirs())) {
            val file = reportDir.resolve("index.html")
            file.writeText("<pre>" + lines.joinToString("\n") + "\n</pre>\n")
        }
    }

    override fun projectsEvaluated(gradle: Gradle) {
    }

    override fun projectsLoaded(gradle: Gradle) {
    }

    override fun settingsEvaluated(settings: Settings) {
    }
}

gradle.addListener(TaskExecutionTimePreview())
