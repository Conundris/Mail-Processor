pipeline {
    agent any
    tools {
        maven 'maven'
        jdk 'Java 8'
    }
    stages {
        stage('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
                sh 'mvn --version'
                sh 'java -version'

                sh 'git checkout ' + stripOrigin("${params.branch}")
                sh 'git branch'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        stage('Testing') {
            steps {
                sh 'mvn verify'
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml'
                }
            }
        }
        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv("SonarQube") {
                    sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.4.0.905:sonar'
                }
            }
        }
        stage('SonarQube Gatekeeper') {
            steps {
                script {
                    def qualityGate = waitForQualityGate()
                    if (qualityGate.status != "OK") {
                        error "Pipeline aborted due to quality gate coverage failure: ${qualityGate.status}"
                    }
                }
            }
        }
        stage('Publish JaCoCo Reports') {
            steps {
                script {
                    step([$class: 'JacocoPublisher', execPattern: '**/target/coverage-reports/*.exec'])
                }
            }
        }
        stage('Deploy Snapshots') {
            when {
                expression {
                    return params.BRANCH == "origin/develop"
                }
            }
            steps {
                script {
                    def server = Artifactory.server "JFrog"
                    def buildInfo = Artifactory.newBuildInfo()
                    def rtMaven = Artifactory.newMavenBuild()
                    rtMaven.tool = 'maven'
                    rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
                    rtMaven.resolver releaseRepo: 'libs-release', snapshotRepo: 'libs-snapshot', server: server
                    rtMaven.run pom: 'pom.xml', goals: 'install', buildInfo: buildInfo
                    publishBuildInfo server: server, buildInfo: buildInfo
                }
            }
        }
        stage('Release') {
            when {
                expression {
                    return params.branch == "origin/master"
                }
            }
            steps {
                script {
                    sshagent(credentials: ['9378bb87-21c9-4525-bab3-b5ccb5155811']) {
                        echo 'Deploying\nPush Docker Image to Registry'
                        sh 'mvn -B -Prelease -Darguments=\'-DskipTests=true -Dmaven.javadoc.skip=true\' release:clean release:prepare release:perform'
                    }
                }
            }
        }
        stage('Promote') {
            when {
                expression {
                    return params.promote == true
                }
            }
            steps {
                script {
                    sshagent(credentials: ['9378bb87-21c9-4525-bab3-b5ccb5155811']) {

                        sh 'git checkout ' + stripOrigin("${params.branch}")
                        sh 'git fetch --all'

                        TAGS = sh(script: "git tag -l --sort=-creatordate | head -n10", returnStdout: true).trim()

                        CHOSEN_RELEASE = input id: 'Tag',
                                message: 'Choose a version to promote',
                                parameters: [
                                        choice(choices: "${TAGS}",
                                                description: "Please choose one.",
                                                name: "CHOOSE_RELEASE")
                                ]

                        sh "echo ${CHOSEN_RELEASE}"
                        sh '#!/bin/bash\n' +
                                'ssh ec2-user@pbm << EOF\n' +
                                "  export release_version=${CHOSEN_RELEASE}" +
                                '  cd /home/ec2-user\n' +
                                '  docker-compose stop\n' +
                                '  docker-compose pull\n' +
                                '  docker-compose up -d\n' +
                                'EOF'
                    }
                }
            }
        }
    }
}

def static stripOrigin(String branch) {
    if (branch.startsWith("origin")) {
        return branch.substring(branch.indexOf('/') + 1, branch.length())
    }
    return branch
}

