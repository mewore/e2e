String root = 'e2e'

pipeline {
    agent any
    tools {
        jdk 'openjdk-15.0.2'
    }

    stages {
        stage('Prepare') {
            steps {
                git([
                    branch: env.BRANCH == null ? 'main' : env.BRANCH,
                    credentialsId: 'jenkins-ssh',
                    url: "git@github.com:mewore/${root}.git",
                ])
                sh 'java -version'
            }
        }
        stage('Build + Test') {
            steps {
                script {
                    tasksToRun = ['e2eTestSpringExampleJar e2eTestSpringExampleJarCustomE2e']
                    spotbugsCommands = []
                    for (javaModule in ['', 'e2e-api']) {
                        tasksToRun.add(javaModule + ':spotbugsMain')
                        tasksToRun.add(javaModule + ':test')
                        spotbugsCommands.add(copySpotbugsReportCmd(javaModule))
                    }

                    sh './gradlew --parallel ' + tasksToRun.join(' ') + ' --no-daemon && ' +
                        spotbugsCommands.join(' && ')
                }
            }
        }
        stage('Post-build') {
        parallel {
            stage('JaCoCo Report') {
                steps {
                    jacoco([
                        classPattern: '**/build/classes',
                        execPattern: '**/**.exec',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: [
                            '**/test/**/*.class',
                        ].join(','),

                        // 100% health at:
                        maximumBranchCoverage: '90',
                        maximumClassCoverage: '95',
                        maximumComplexityCoverage: '90',
                        maximumLineCoverage: '95',
                        maximumMethodCoverage: '95',
                        // 0% health at:
                        minimumBranchCoverage: '70',
                        minimumClassCoverage: '80',
                        minimumComplexityCoverage: '70',
                        minimumLineCoverage: '80',
                        minimumMethodCoverage: '80',
                    ])
                }
            }
            // There are no frontend tests at the moment
//             stage('Cobertura Report') {
//                 steps {
//                     cobertura([
//                         coberturaReportFile: '**/frontend/tests/coverage/cobertura-coverage.xml',
//                         conditionalCoverageTargets: '90, 50, 0',
//                         lineCoverageTargets: '95, 60, 0',
//                         methodCoverageTargets: '95, 60, 0',
//                         failUnhealthy: false,
//                         failUnstable: false,
//                         zoomCoverageChart: false,
//                     ])
//                 }
//             }
        } // parallel { ... }
        } // Post-build { ... }
    }

    post {
        always {
            archiveArtifacts([
                artifacts: [
                    [root, 'e2e-api'].collect({
                        (it == root ? '' : (it + '/')) + 'build/libs/**/*.jar'
                    }),
                    [root, 'e2e-api'].collect({
                        (it == root ? '' : (it + '/')) + "build/reports/spotbugs/spotbugs-${it}.html"
                    })
                ].flatten().join(','),
                fingerprint: true,
            ])
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: false,
                keepAll: true,
                reportDir: 'build/reports/task-durations',
                reportFiles: 'index.html',
                reportName: 'Task Durations',
                reportTitles: 'Task Durations'
            ])
        }
    }
}

def copySpotbugsReportCmd(module) {
    String dir = (module == root ? '' : (module + '/')) + 'build/reports/spotbugs'
    return "cp ${dir}/main.html ${dir}/spotbugs-${module}.html"
}
