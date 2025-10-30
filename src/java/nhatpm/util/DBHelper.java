/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nhatpm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author nhatpm
 */
public class DBHelper {

    public static Connection getConnection()
            throws ClassNotFoundException, SQLException {
        Connection result = null;

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String url = "jdbc:sqlserver://"
                + "localhost:1433;"
                + "databaseName=PRJSE1932;"
                + "encrypt=true;"
                + "trustServerCertificate=true;";
        result = DriverManager.getConnection(url, "sa", "YourStrong!Passw0rd");

        return result;

    }

}
