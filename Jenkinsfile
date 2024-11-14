pipeline {
    agent any
    environment {
        GOOGLE_CREDENTIALS = credentials('gcp-credentials')
        GKE_CLUSTER = 'gurula'  // GKE 集群名稱
        GKE_ZONE = 'asia-east1-b'  // GKE 集群區域
        GKE_PROJECT = 'gurula'  // GCP 專案 ID
        ARTIFACT_REGISTRY = 'gcr.io'  // Artifact Registry 存儲庫地址
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/YFCKevin/gurula.git'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    // build docker image
                    def services = [
                        [name: 'gateway', jar: 'gateway/target/gateway.jar'],
                        [name: 'member-service', jar: 'member-service/target/member-service.jar'],
                        [name: 'badminton', jar: 'badminton/target/badminton.jar'],
                        [name: 'badminton-front', jar: 'badminton-front/target/badminton-front.jar']
                    ]

                    services.each { service ->
                        echo "Building Docker image for ${service.name}"
                        sh "docker build -t ${ARTIFACT_REGISTRY}/${service.name}:${BUILD_ID} -f Dockerfile.${service.name} ."
                    }
                }
            }
        }

        stage('Push to Artifact Registry') {
            steps {
                script {
                    // Push Docker Images to Artifact Registry
                    withCredentials([file(credentialsId: 'gcp-credentials', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                        sh 'gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS'
                        sh 'gcloud auth configure-docker ${ARTIFACT_REGISTRY}'

                        def services = [
                            [name: 'gateway'],
                            [name: 'member-service'],
                            [name: 'badminton'],
                            [name: 'badminton-front']
                        ]

                        services.each { service ->
                            echo "Pushing Docker image for ${service.name}"
                            sh "docker push ${ARTIFACT_REGISTRY}/${service.name}:${BUILD_ID}"
                        }
                    }
                }
            }
        }

        stage('Deploy to GKE') {
            steps {
                script {
                    // 部署每個服務到 GKE
                    withCredentials([file(credentialsId: 'gcp-credentials', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
                        // 設置 GKE 認證
                        sh 'gcloud auth activate-service-account --key-file=$GOOGLE_APPLICATION_CREDENTIALS'
                        sh 'gcloud container clusters get-credentials ${GKE_CLUSTER} --zone ${GKE_ZONE} --project ${GKE_PROJECT}'

                        def services = [
                            [name: 'gateway'],
                            [name: 'member-service'],
                            [name: 'badminton'],
                            [name: 'badminton-front']
                        ]

                        services.each { service ->
                            echo "Deploying ${service.name} to GKE"
                            sh """
                                kubectl set image deployment/${service.name}-deployment ${service.name}-container=${ARTIFACT_REGISTRY}/${service.name}:${BUILD_ID} --record
                                kubectl rollout status deployment/${service.name}-deployment
                            """
                        }
                    }
                }
            }
        }
    }
}
