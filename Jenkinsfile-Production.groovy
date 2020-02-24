pipeline {
    agent {
        dockerfile {
            filename 'docker/dockerfile-java'
            additionalBuildArgs '--build-arg JENKINS_USER_ID=`id -u jenkins` --build-arg JENKINS_GROUP_ID=`id -g jenkins`'
        }
    }

    environment {
        AWS_ACCESS_KEY_ID = credentials('AWS_ACCESS_KEY_ID')
        AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_ACCESS_KEY')
		DOCKER_PROJECT_NAME = "innovation-scoreboard-api"
		DOCKER_IMAGE = '755952719952.dkr.ecr.eu-west-1.amazonaws.com/innovation-scoreboard-api'
		DOCKER_TAG = "prod-$BUILD_NUMBER"

        POSTGRES_URL = "jdbc:postgresql://postgres-prod.co90ybcr8iim.eu-west-1.rds.amazonaws.com:5432/innovation_scoreboard"
        POSTGRES_USERNAME = credentials('innovation-scoreboard-api-prod-postgres-username')
        POSTGRES_PASSWORD = credentials('innovation-scoreboard-api-prod-postgres-password')

        ELASTICSEARCH_SCHEME = "https"
        ELASTICSEARCH_HOST = "05068ca494804a0b86352e68fadb3457.eu-west-1.aws.found.io"
        ELASTICSEARCH_PORT = "9243"
        ELASTICSEARCH_USERNAME = credentials('innovation-scoreboard-api-prod-elasticsearch-username')
        ELASTICSEARCH_PASSWORD = credentials('innovation-scoreboard-api-prod-elasticsearch-password')
        ELASTICSEARCH_NAMESPACE_PREFIX = "innovation-scoreboard-prod"

        S3_REGION = "eu-west-1"
        S3_BUCKET_NAME = "prod-innovation-scoreboard-api"
        S3_ACCESS_KEY = credentials('innovation-scoreboard-api-prod-s3-access-key')
        S3_SECRET_KEY = credentials('innovation-scoreboard-api-prod-s3-secret-key')

        SECURITY_ALLOWED_ORIGINS = "https://innovation.davinci.bz.it"
        KEYCLOAK_URL = "https://auth.opendatahub.bz.it/auth"
        KEYCLOAK_REALM = "NOI"
        KEYCLOAK_CLIENT_ID = "davinci-innovation-scoreboard-api"
        KEYCLOAK_CLIENT_SECRET = credentials('innovation-scoreboard-api-prod-keycloak-client-secret')
    }

	stages {
        stage('Configure') {
            steps {
                sh """
					rm -f .env
					cp .env.example .env
                	echo 'COMPOSE_PROJECT_NAME=${DOCKER_PROJECT_NAME}' >> .env
                	echo 'DOCKER_IMAGE=${DOCKER_IMAGE}' >> .env
                	echo 'DOCKER_TAG=${DOCKER_TAG}' >> .env

					echo 'POSTGRES_URL=${POSTGRES_URL}' >> .env
        			echo 'POSTGRES_USERNAME=${POSTGRES_USERNAME}' >> .env
        			echo 'POSTGRES_PASSWORD=${POSTGRES_PASSWORD}' >> .env

        			echo 'ELASTICSEARCH_SCHEME=${ELASTICSEARCH_SCHEME}' >> .env
        			echo 'ELASTICSEARCH_HOST=${ELASTICSEARCH_HOST}' >> .env
        			echo 'ELASTICSEARCH_PORT=${ELASTICSEARCH_PORT}' >> .env
        			echo 'ELASTICSEARCH_USERNAME=${ELASTICSEARCH_USERNAME}' >> .env
        			echo 'ELASTICSEARCH_PASSWORD=${ELASTICSEARCH_PASSWORD}' >> .env
        			echo 'ELASTICSEARCH_NAMESPACE_PREFIX=${ELASTICSEARCH_NAMESPACE_PREFIX}' >> .env

        			echo 'S3_REGION=${S3_REGION}' >> .env
        			echo 'S3_BUCKET_NAME=${S3_BUCKET_NAME}' >> .env
        			echo 'S3_ACCESS_KEY=${S3_ACCESS_KEY}' >> .env
        			echo 'S3_SECRET_KEY=${S3_SECRET_KEY}' >> .env

        			echo 'SECURITY_ALLOWED_ORIGINS=${SECURITY_ALLOWED_ORIGINS}' >> .env
        			echo 'KEYCLOAK_URL=${KEYCLOAK_URL}' >> .env
        			echo 'KEYCLOAK_REALM=${KEYCLOAK_REALM}' >> .env
        			echo 'KEYCLOAK_CLIENT_ID=${KEYCLOAK_CLIENT_ID}' >> .env
        			echo 'KEYCLOAK_CLIENT_SECRET=${KEYCLOAK_CLIENT_SECRET}' >> .env
				"""
            }
        }

        stage('Test') {
            steps {
				sh '''
					docker-compose --no-ansi build --pull --build-arg JENKINS_USER_ID=$(id -u jenkins) --build-arg JENKINS_GROUP_ID=$(id -g jenkins)
					docker-compose --no-ansi run --rm --no-deps -u $(id -u jenkins):$(id -g jenkins) app mvn clean test
				'''
            }
        }
		stage('Build') {
            steps {
				sh '''
					aws ecr get-login --region eu-west-1 --no-include-email | bash
					docker-compose --no-ansi -f docker-compose.build.yml build --pull
					docker-compose --no-ansi -f docker-compose.build.yml push
				'''
            }
        }
		stage('Deploy') {
            steps {
               sshagent(['jenkins-ssh-key']) {
                    sh """
						ansible-galaxy install --force -r ansible/requirements.yml
						ansible-playbook --limit=docker02.opendatahub.bz.it ansible/deploy.yml --extra-vars "build_number=${BUILD_NUMBER}"
					"""
                }
            }
        }
    }
}