# 🇹🇼 Learning Taiwanese Game 🎮

A simple interactive game built with Spring Boot to help users learn basic Taiwanese vocabulary and phrases. This project also demonstrates a robust Continuous Integration/Continuous Deployment (CI/CD) pipeline using Jenkins to automate containerization, push to Google Cloud Artifact Registry, and deploy to Google Cloud Run.

## 🌟 Features

* **Interactive Game:** Engage with a simple game designed for learning Taiwanese.
* **Spring Boot Backend:** Robust and scalable backend for game logic.
* **Containerization with Docker:** Package the application into a lightweight, portable Docker image.
* **Automated CI/CD with Jenkins:**
    * Automatically pulls code from GitHub.
    * Builds the Spring Boot application using Maven.
    * Builds the Docker image for the application.
    * Pushes the Docker image to Google Cloud Artifact Registry.
    * Deploys the new image to Google Cloud Run.
* **Google Cloud Platform (GCP) Integration:** Leverages Artifact Registry for container image hosting and Cloud Run for serverless deployment.

## 🚀 Technologies Used

* **Backend:** Java 17, Spring Boot, Maven
* **Containerization:** Docker
* **CI/CD:** Jenkins
* **Version Control:** Git, GitHub
* **Cloud Platform:** Google Cloud Platform (GCP)
    * **Artifact Registry:** For Docker image storage
    * **Cloud Run:** For serverless application deployment

## 📋 Prerequisites

Before you begin, ensure you have the following installed and configured:

### Local Development
* **Java 17 JDK:** [Download & Install](https://www.oracle.com/java/technologies/downloads/) (or use a package manager like `sdkman` or `brew install openjdk@17`)
* **Maven:** [Download & Install](https://maven.apache.org/download.cgi)
* **Docker Desktop:** [Download & Install](https://www.docker.com/products/docker-desktop/) (includes Docker Engine and Docker Compose)
* **Git:** [Download & Install](https://git-scm.com/downloads)

### Google Cloud Platform (GCP)
* **GCP Account:** With an active billing account.
* **GCP Project:** Your project ID is `bustling-flux-461615-e6`.
* **Google Cloud SDK (`gcloud` CLI):** [Install](https://cloud.google.com/sdk/docs/install)
    * Authenticate: `gcloud auth login` and `gcloud config set project bustling-flux-461615-e6`
* **Artifact Registry Repository:**
    * Create a Docker repository (if you haven't already) in `europe-west2`.
    * Example command: `gcloud artifacts repositories create my-container-repo --repository-format=docker --location=europe-west2 --description="Docker repository for learning-taiwanese app"`
* **Cloud Run Service:** You'll need a Cloud Run service ready to deploy to.
    * Example initial deployment (manual, for setup):
        ```bash
        gcloud run deploy learning-taiwanese-spring-boot \
          --image gcr.io/cloudrun/hello \
          --region europe-west2 \
          --platform managed \
          --allow-unauthenticated \
          --port 8080
        ```
* **GCP Service Account for Jenkins:**
    * Create a service account (e.g., `jenkins-ci-cd-sa`).
    * Grant it `Artifact Registry Writer` (or `Admin`), `Cloud Run Admin`, and `Service Account User` roles.
    * Generate and download a JSON key for this service account. Keep this file secure!

## 💻 Getting Started (Local Development)

To run the Spring Boot application locally:

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/wadevs14161/learning-taiwanese.git](https://github.com/wadevs14161/learning-taiwanese.git)
    cd learning-taiwanese
    ```
2.  **Build the application:**
    ```bash
    mvn clean package -DskipTests
    ```
3.  **Run the application:**
    ```bash
    java -jar target/your-game-app.jar # Replace with your actual JAR name, e.g., learning-taiwanese-0.0.1-SNAPSHOT.jar
    ```
4.  The application should start on `http://localhost:8080`.

## 🐳 Dockerization

To build and run the Docker image locally:

1.  **Build the application JAR (if not already done):**
    ```bash
    mvn clean package -DskipTests
    ```
2.  **Build the Docker image:**
    ```bash
    docker build -t learning-taiwanese-game:latest .
    ```
3.  **Run the Docker container:**
    ```bash
    docker run -p 8080:8080 learning-taiwanese-game:latest
    ```
4.  Access the application in your browser at `http://localhost:8080`.

## 🚀 CI/CD Pipeline Setup (Jenkins & GCP Cloud Run)

This section details how to set up the automated pipeline for deployment to GCP Cloud Run using Jenkins.

### 1. Jenkins Environment Setup

Your Jenkins instance will run locally using Docker Compose, leveraging a Docker-in-Docker setup.

#### `docker-compose.yml`

Create a `docker-compose.yml` file in a dedicated directory (e.g., `install-jenkins-docker`) with the following content:

```yaml
services:
  jenkins-docker:
    image: docker:dind
    container_name: jenkins-docker
    privileged: true
    environment:
      - DOCKER_TLS_CERTDIR=/certs
    volumes:
      - jenkins-docker-certs:/certs/client
      # - jenkins-data:/var/jenkins_home # Removed for clarity; should only be on my-jenkins
    ports:
      - "2376:2376"
    networks:
      jenkins:
        aliases:
          - docker
    command: --storage-driver overlay2

  my-jenkins:
    image: my-jenkins
    build:
      context: .
    container_name: my-jenkins
    restart: on-failure
    environment:
      - DOCKER_HOST=tcp://docker:2376
      - DOCKER_CERT_PATH=/certs/client
      - DOCKER_TLS_VERIFY=1
    volumes:
      - jenkins-data:/var/jenkins_home
      - jenkins-docker-certs:/certs/client:ro
    ports:
      - "8080:8080"
      - "50000:50000"
    networks:
      - jenkins

networks:
  jenkins:
    driver: bridge

volumes:
  jenkins-docker-certs:
  jenkins-data:
