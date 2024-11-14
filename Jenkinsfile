pipeline {
    agent any
    environment {
        GKE_CLUSTER = 'gurula'
        GKE_ZONE = 'asia-east1-b'
        GKE_PROJECT = 'gurula'
        ARTIFACT_REGISTRY = 'gcr.io'
        BUILD_ID = 'latest'
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/YFCKevin/gurula.git'
            }
        }

        stage('Build') {
            steps {
                sh "mvn -Dmaven.test.failure.ignore=true -DskipTests clean package -pl '!line-service,!line-front'"
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = [
                        [name: 'gateway', jar: 'gateway/target/gateway.jar'],
                        [name: 'member-service', jar: 'member-service/target/member-service.jar'],
                        [name: 'badminton', jar: 'badminton/target/badminton.jar'],
                        [name: 'badminton-front', jar: 'badminton-front/target/badminton-front.jar']
                    ]

                    services.each { service ->
                        echo "Building Docker image for ${service.name}"
                        sh "docker build -t ${ARTIFACT_REGISTRY}/${service.name}:${BUILD_ID} -f ${service.name}/Dockerfile ."
                    }
                }
            }
        }

        stage('Push to Artifact Registry') {
            steps {
                script {
                    echo "Checking Google service account email"
                    // 顯示當前使用的 Google 服務帳戶電子郵件
                    sh 'gcloud auth list'

                    echo "Configuring Docker auth for Artifact Registry"
                    sh 'sudo gcloud auth configure-docker ${ARTIFACT_REGISTRY}'

                    def services = [
                        [name: 'gateway'],
                        [name: 'member-service'],
                        [name: 'badminton'],
                        [name: 'badminton-front']
                    ]

                    services.each { service ->
                        echo "Pushing Docker image for ${service.name}"
                        sh "sudo docker push ${ARTIFACT_REGISTRY}/${service.name}:${BUILD_ID}"
                    }
                }
            }
        }

        stage('Deploy to GKE') {
            steps {
                script {
                    echo "Getting credentials for GKE cluster"
                    sh 'sudo gcloud container clusters get-credentials ${GKE_CLUSTER} --zone ${GKE_ZONE} --project ${GKE_PROJECT}'

                    def services = [
                        [name: 'gateway'],
                        [name: 'member-service'],
                        [name: 'badminton'],
                        [name: 'badminton-front']
                    ]

                    services.each { service ->
                        echo "Deploying ${service.name} to GKE"
                        sh """
                            set -e
                            sudo kubectl set image deployment/${service.name}-deployment ${service.name}-container=${ARTIFACT_REGISTRY}/${service.name}:${BUILD_ID} --record
                            sudo kubectl rollout status deployment/${service.name}-deployment
                        """
                    }
                }
            }
        }
    }
}
