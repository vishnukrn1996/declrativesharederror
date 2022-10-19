pipeline {
    agent {
  label 'nodes'
}
    parameters {
  choice choices: ['development', 'stage', 'master'], name: 'BranchName'
}
        tools {
   maven 'maven3.8.6'
      }
      options {
  buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5')
}
triggers {
  pollSCM '* * * * * '
}
      stages {
        stage('checkout') {
            steps {
                sendSlackNotifications('STARTED')
                git branch: "${params.BranchName}", credentialsId: 'dd130e80-2481-4bb9-a12e-18d7edf464d8', url: 'https://github.com/vishnukrn1996/maven-web-application.git'
            }
        }
        stage('build') {
            steps {
                sh "mvn clean package"
            }
        }
        stage('sonarqube') {
            steps {
                sh "mvn clean sonar:sonar"
            }
        }
            stage('nexus repo') {
                steps {
                    sh "mvn clean deploy"
                }
            }
            stage('deploy tomcat') {
                steps {
                sshagent(['a8009aed-6127-4a69-bbde-17c35fdcbedd']) {
                   sh "scp -o StrictHostKeyChecking=no target/maven-web-application.war ec2-user@3.110.32.173:/opt/apache-tomcat-9.0.67/webapps/"
                   }
               }
            }
    }//stages
    post {
  aborted {
    sendSlackNotifications(currentBuild.result)
  }
  success {
    sendSlackNotifications(currentBuild.result)
  }
  failure {
    sendSlackNotifications(currentBuild.result)
  }
    }
}//pipeline


