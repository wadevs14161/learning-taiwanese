// Define common variables for the pipeline
def project_id = 'bustling-flux-461615-e6' // Your GCP Project ID
def artifact_registry_location = 'europe-west2'
def artifact_registry_repo = 'my-container-repo'
def image_name = 'spring-app'
def cloud_run_service_name = 'learning-taiwanese-spring-boot'
def cloud_run_region = 'europe-west2'
def cloud_run_memory = '512Mi' // Ensure this matches your successful deployment

pipeline {
    agent any // Use any available Jenkins agent (your 'my-jenkins' container)

    environment {
        // Construct the full image path for Artifact Registry
        FULL_IMAGE_PATH = "${artifact_registry_location}-docker.pkg.dev/${project_id}/${artifact_registry_repo}/${image_name}"
    }

    stages {
        stage('Checkout Code') {
            steps {
                git 'https://github.com/wadevs14161/learning-taiwanese.git'
            }
        }

        stage('Build Spring Boot App') {
            steps {
                sh 'mvn clean package -DskipTests' // Build the JAR, skip tests for faster build
            }
        }

        stage('Docker Login & Build Image') {
            steps {
                script {
                    // Use the GCP Service Account Key securely (ID defined in Jenkins Credentials)
                    withCredentials([file(credentialsId: 'gcp-key', variable: 'GCP_KEY_FILE')]) {
                        // Authenticate gcloud CLI with the service account
                        sh "gcloud auth activate-service-account --key-file=$GCP_KEY_FILE --project=${project_id}"

                        // Configure Docker to authenticate with Artifact Registry
                        sh "gcloud auth configure-docker ${artifact_registry_location}-docker.pkg.dev"

                        // Build the Docker image
                        // Use Jenkins BUILD_NUMBER for a unique and traceable tag
                        sh "docker build -t ${image_name}:${env.BUILD_NUMBER} ."
                    }
                }
            }
        }

        stage('Push to Artifact Registry') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'gcp-key', variable: 'GCP_KEY_FILE')]) {
                        // Re-authenticate just in case (good practice in separate stages)
                        sh "gcloud auth activate-service-account --key-file=$GCP_KEY_FILE --project=${project_id}"
                        sh "gcloud auth configure-docker ${artifact_registry_location}-docker.pkg.dev"

                        // Tag the image with the full Artifact Registry path
                        sh "docker tag ${image_name}:${env.BUILD_NUMBER} ${FULL_IMAGE_PATH}:${env.BUILD_NUMBER}"

                        // Push the image to Artifact Registry
                        sh "docker push ${FULL_IMAGE_PATH}:${env.BUILD_NUMBER}"
                    }
                }
            }
        }

        stage('Deploy to Cloud Run') {
            steps {
                script {
                    withCredentials([file(credentialsId: 'gcp-key', variable: 'GCP_KEY_FILE')]) {
                        // Re-authenticate gcloud CLI
                        sh "gcloud auth activate-service-account --key-file=$GCP_KEY_FILE --project=${project_id}"

                        // Deploy the image to Cloud Run
                        sh """
                        gcloud run deploy ${cloud_run_service_name} \\
                            --image ${FULL_IMAGE_PATH}:${env.BUILD_NUMBER} \\
                            --region ${cloud_run_region} \\
                            --platform managed \\
                            --port 8080 \\
                            --concurrency 10 \\
                            --min-instances 1 \\
                            --max-instances 5 \\
                            --allow-unauthenticated \\
                            --memory ${cloud_run_memory}
                        """
                    }
                }
            }
        }
    }
    post {
        always {
            // Clean up workspace after build
            cleanWs()
        }
    }
}