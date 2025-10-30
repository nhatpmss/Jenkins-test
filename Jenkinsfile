pipeline {
  agent any

  stages {
    stage('Checkout') {
      steps {
        // Dùng chính repo chứa Jenkinsfile
        checkout scm
        sh 'echo "Checked out $(git rev-parse --short HEAD)"'
      }
    }

    stage('Build (Ant nếu có)') {
      steps {
        sh '''
          set -e
          if ! command -v ant >/dev/null 2>&1; then
            echo "Ant chưa cài -> bỏ qua build."
            exit 0
          fi

          if [ ! -f build.xml ]; then
            echo "Không có build.xml -> bỏ qua build."
            exit 0
          fi

          echo "ant clean compile"
          ant clean compile || true

          # Nếu có target war thì đóng gói
          if ant -p | grep -qE '^\\s*war\\b'; then
            echo "ant war"
            ant war || true
          fi
        '''
      }
    }

    stage('Artifacts') {
      steps {
        // Lưu output nếu có (không có cũng không fail)
        archiveArtifacts artifacts: 'dist/**/*, build/**/*', allowEmptyArchive: true
      }
    }
  }

  post {
    success { echo 'Done' }
    failure { echo 'Failed — xem log build' }
  }
}
