node('basic') {
  def commit_id
  def app
  def tag

  stage('Checkout source') {
    checkout scm
    sh 'git rev-parse HEAD > .git-commit-id'
    commit_id = readFile('.git-commit-id').trim()
  }

  stage('Build image') {
    app = docker.build 'mafiascum/site-chat-server'
  }

  stage('Push image') {
    sh 'docker login --username $DOCKER_HUB_USERNAME --password $DOCKER_HUB_PASSWORD'
    tag = "${env.BRANCH_NAME}-${commit_id}"
    app.push "${tag}"
  }

  stage('Deploy to cluster') {
    sh "kubectl --namespace ccatlett2000 set image deployment site-chat-server site-chat-server=mafiascum/site-chat-server:${tag}"
  }
}
