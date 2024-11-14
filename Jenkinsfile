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
                    withCredentials([string(credentialsId: 'gcp-credentials', variable: 'GCP_API_KEY'),
                                     string(credentialsId: 'gcp-email', variable: 'GCP_EMAIL')]) {
                        echo "Activating service account"
                        sh """
                            echo '${GCP_API_KEY}' | gcloud auth activate-service-account ${GCP_EMAIL} --key-file=-
                        """

                        echo "Configuring Docker auth for Artifact Registry"
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
                    withCredentials([string(credentialsId: 'gcp-credentials-json', variable: 'GCP_API_KEY'),
                                     string(credentialsId: 'gcp-email', variable: 'GCP_EMAIL')]) {
                        echo "Activating service account"
                        sh """
                            echo '${GCP_API_KEY}' | gcloud auth activate-service-account ${GCP_EMAIL} --key-file=-
                        """

                        echo "Getting credentials for GKE cluster"
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
                                set -e
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
