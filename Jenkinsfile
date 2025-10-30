pipeline {
  agent any

  tools {
    jdk  'jdk17'
    ant  'ant-latest'
  }

  parameters {
    string(name: 'TEST_USER',     defaultValue: 'khanh',   description: 'Username hợp lệ để test login')
    string(name: 'TEST_PASS',     defaultValue: 'kieu123', description: 'Password hợp lệ để test login')
    string(name: 'APP_PORT',      defaultValue: '8082',    description: 'Port chạy webapp')
    string(name: 'CONTEXT_PATH',  defaultValue: '/praticeMVC2', description: 'Context path (vd: /praticeMVC2)')
    string(name: 'SEARCH_TOKEN',  defaultValue: '',        description: 'Từ khoá kỳ vọng ở trang login đúng (vd: "WELCOME")')
    string(name: 'INVALID_TOKEN', defaultValue: '',        description: 'Từ khoá kỳ vọng ở trang login sai (vd: "INVALID")')
  }

  environment {
    MSSQL_JDBC_URL    = 'https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/12.6.1.jre11/mssql-jdbc-12.6.1.jre11.jar'
    SERVLET_API_URL   = 'https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar'
    WEBAPP_RUNNER_URL = 'https://repo1.maven.org/maven2/com/heroku/webapp-runner/9.0.109.0/webapp-runner-9.0.109.0.jar'
  }

  options {
    timestamps()
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
        sh 'echo "Rev $(git rev-parse --short HEAD)"'
      }
    }

    stage('Download dependencies') {
      steps {
        sh '''
          set -e
          mkdir -p .workspace_lib
          echo "Download JDBC, Servlet API, Webapp Runner"
          curl -fsSL "$MSSQL_JDBC_URL"    -o .workspace_lib/mssql-jdbc.jar
          curl -fsSL "$SERVLET_API_URL"   -o .workspace_lib/servlet-api.jar
          curl -fsSL "$WEBAPP_RUNNER_URL" -o .workspace_lib/webapp-runner.jar
          ls -lh .workspace_lib
        '''
      }
    }

    stage('Build WAR') {
      steps {
        sh '''
          set -e

          # Build bằng Ant nếu có build.xml
          if command -v ant >/dev/null 2>&1 && [ -f build.xml ]; then
            echo "ant clean dist"
            ant clean dist || true
          fi

          # Chuẩn bị cấu trúc build nếu chưa có
          mkdir -p build/web/WEB-INF/classes build/web/WEB-INF/lib

          # Copy web content
          if [ -d web ]; then
            cp -R web/* build/web/ 2>/dev/null || true
          elif [ -d src/main/webapp ]; then
            cp -R src/main/webapp/* build/web/ 2>/dev/null || true
          fi

          # Biên dịch Java (fallback)
          SRC_DIRS=""
          for d in src src/java src/main/java; do
            [ -d "$d" ] && SRC_DIRS="$SRC_DIRS $d"
          done
          if [ -n "$SRC_DIRS" ]; then
            find $SRC_DIRS -name "*.java" > sources.list
            javac -cp ".workspace_lib/servlet-api.jar:.workspace_lib/mssql-jdbc.jar" \
                  -d build/web/WEB-INF/classes @sources.list
          fi

          # Thêm JDBC driver
          cp .workspace_lib/mssql-jdbc.jar build/web/WEB-INF/lib/

          # Đóng gói WAR
          mkdir -p target
          cd build/web
          jar cf /var/jenkins_home/workspace/SWT301/target/praticeMVC2.war *
          cd -
          ls -lh target/praticeMVC2.war
        '''
      }
    }

    stage('Run App') {
      steps {
        sh '''
          set -e
          echo "Starting webapp-runner..."
          nohup bash -c "java -jar .workspace_lib/webapp-runner.jar \
            --port ${APP_PORT} --path ${CONTEXT_PATH} target/praticeMVC2.war" \
            > .workspace_lib/app.log 2>&1 &
          echo $! > .workspace_lib/app.pid
          echo "PID $(cat .workspace_lib/app.pid)"

          for i in $(seq 1 40); do
            if curl -fsS "http://localhost:${APP_PORT}${CONTEXT_PATH}/" >/dev/null 2>&1; then
              echo "App is up at http://localhost:${APP_PORT}${CONTEXT_PATH}"
              break
            fi
            sleep 1
          done
        '''
      }
    }

    stage('Test LoginServlet') {
      steps {
        sh '''
          set -e
          BASE="http://localhost:${APP_PORT}${CONTEXT_PATH}"
          OK_URL="$BASE/LoginServlet?txtUsername=${TEST_USER}&txtPassword=${TEST_PASS}&action=Login"
          BAD_URL="$BASE/LoginServlet?txtUsername=${TEST_USER}&txtPassword=WRONG&action=Login"

          echo "GET $OK_URL"
          curl -fsS "$OK_URL" -o ok.html

          echo "GET $BAD_URL"
          curl -fsS "$BAD_URL" -o bad.html

          echo "---- ok.html (top 3 lines) ----"
          head -n 3 ok.html || true
          echo "---- bad.html (top 3 lines) ----"
          head -n 3 bad.html || true

          if [ -n "${SEARCH_TOKEN}" ] && [ -n "${INVALID_TOKEN}" ]; then
            grep -q "${SEARCH_TOKEN}"  ok.html  || { echo "Thiếu SEARCH_TOKEN"; exit 1; }
            grep -q "${INVALID_TOKEN}" bad.html || { echo "Thiếu INVALID_TOKEN"; exit 1; }
          else
            if cmp -s ok.html bad.html; then
              echo "Hai trang giống nhau, có thể login sai logic."
              exit 2
            else
              echo "Hai trang khác nhau — LoginServlet hoạt động đúng."
            fi
          fi
        '''
      }
    }

    stage('Archive logs') {
      steps {
        archiveArtifacts artifacts: 'ok.html,bad.html,.workspace_lib/app.log,target/praticeMVC2.war', allowEmptyArchive: true
      }
    }
  }

  post {
    always {
      echo 'Dọn dẹp server...'
      sh '''
        if [ -f .workspace_lib/app.pid ]; then
          kill $(cat .workspace_lib/app.pid) 2>/dev/null || true
        fi
      '''
    }
    success {
      echo 'Pipeline PASSED — LoginServlet hoạt động bình thường'
    }
    failure {
      echo 'Pipeline FAILED — kiểm tra ok.html, bad.html và app.log'
    }
  }
}
