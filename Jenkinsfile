pipeline {
  agent any

  // Nếu bạn đã cấu hình Tools trong Jenkins, giữ khối này. Không có thì bỏ đi.
  tools {
    jdk 'jdk17'
    ant 'ant-latest'
  }

  parameters {
    string(name: 'DB_URL',  defaultValue: 'jdbc:sqlserver://localhost:1433;databaseName=PRJSE1932;encrypt=true;trustServerCertificate=true', description: 'JDBC URL')
    string(name: 'DB_USER', defaultValue: 'sa', description: 'DB username')
    string(name: 'DB_PASS', defaultValue: 'YourStrong!Passw0rd', description: 'DB password')
    string(name: 'TEST_USER', defaultValue: 'khanh', description: 'Username để test login')
    string(name: 'TEST_PASS', defaultValue: 'kieu123', description: 'Password để test login')
  }

  // Chỉ gán chuỗi (hợp lệ với Declarative)
  environment {
    COPYLIBS_JAR = 'tools/copylibs/org-netbeans-modules-java-j2seproject-copylibstask.jar'
    DB_URL   = "${params.DB_URL}"
    DB_USER  = "${params.DB_USER}"
    DB_PASS  = "${params.DB_PASS}"
    TEST_USER = "${params.TEST_USER}"
    TEST_PASS = "${params.TEST_PASS}"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'echo "Rev $(git rev-parse --short HEAD)"'
      }
    }

    stage('Prepare JDBC Driver') {
      steps {
        sh '''
          set -e
          mkdir -p .workspace_lib
          DRIVER_URL="https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/12.6.1.jre11/mssql-jdbc-12.6.1.jre11.jar"
          curl -fsSL "$DRIVER_URL" -o .workspace_lib/mssql-jdbc.jar
          ls -l .workspace_lib/mssql-jdbc.jar
        '''
      }
    }

    stage('Build (Ant nếu có)') {
      steps {
        sh '''
          set -e
          if command -v ant >/dev/null 2>&1 && [ -f build.xml ]; then
            # Nếu project NetBeans cần CopyLibs, bạn có thể thêm file vào repo ở COPYLIBS_JAR và gán -D
            if [ -f "$COPYLIBS_JAR" ]; then
              ant -Dlibs.CopyLibs.classpath="$COPYLIBS_JAR" clean compile || true
            else
              ant clean compile || true
            fi
          else
            echo "Không có Ant hoặc build.xml -> sẽ javac src nếu cần."
          fi
        '''
      }
    }

    stage('Smoke Test RegistrationDAO.checkLogin') {
      steps {
        sh '''
          set -e
          # Tạo runner Java để gọi DAO (không sửa code dự án)
          cat > DaoSmokeRunner.java <<'EOF'
          import nhatpm.registration.RegistrationDAO;
          import java.sql.*;
          public class DaoSmokeRunner {
            public static void main(String[] args) throws Exception {
              String dbUrl  = System.getenv("DB_URL");
              String dbUser = System.getenv("DB_USER");
              String dbPass = System.getenv("DB_PASS");
              String u = System.getenv("TEST_USER");
              String p = System.getenv("TEST_PASS");

              System.out.println("DB_URL=" + dbUrl);
              try { Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); }
              catch (ClassNotFoundException e) {
                System.err.println("Không tìm thấy com.microsoft.sqlserver.jdbc.SQLServerDriver");
                throw e;
              }

              try (Connection c = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                System.out.println("DB connected.");
              }

              RegistrationDAO dao = new RegistrationDAO();
              boolean ok = dao.checkLogin(u, p);
              if (ok) {
                System.out.println("checkLogin(" + u + "/*hidden*/) = TRUE");
                System.exit(0);
              } else {
                System.out.println("checkLogin(" + u + "/*hidden*/) = FALSE");
                System.exit(2);
              }
            }
          }
          EOF

          # Ưu tiên dùng class đã compile bởi Ant (build/classes). Nếu chưa có, tự javac src.
          CLASS_DIR="build/classes"
          if [ ! -d "$CLASS_DIR" ]; then
            SRC_DIR="src"
            if [ -d "$SRC_DIR" ]; then
              echo "Tự compile src -> build/classes"
              mkdir -p build/classes
              find "$SRC_DIR" -name "*.java" > sources.list
              javac -cp .workspace_lib/mssql-jdbc.jar -d build/classes @sources.list
            else
              echo "Không thấy build/classes hoặc src để compile."
              exit 1
            fi
          fi

          # Compile runner
          javac -cp "build/classes:.workspace_lib/mssql-jdbc.jar" DaoSmokeRunner.java

          # Run
          java -cp "build/classes:.workspace_lib/mssql-jdbc.jar:." DaoSmokeRunner
        '''
      }
    }

    stage('Artifacts') {
      steps {
        archiveArtifacts artifacts: 'dist/**/*, build/**/*', allowEmptyArchive: true
      }
    }
  }

  post {
    success { echo 'Pipeline OK (DAO smoke test passed)' }
    failure { echo 'Pipeline FAILED — xem log ở stage Smoke Test' }
  }
}
