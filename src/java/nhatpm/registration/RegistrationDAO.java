/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nhatpm.registration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nhatpm.util.DBHelper;

/**
 *
 * @author nhatpm
 */
public class RegistrationDAO {
    
    public boolean checkLogin(String username, String password) 
            throws ClassNotFoundException, SQLException{
        boolean result = false;
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        
        try {
            con = DBHelper.getConnection();
            String sql = "Select username "
                    + "From Registration "
                    + "Where username = ? "
                    + "And password = ? ";

            stm = con.prepareStatement(sql);
            stm.setString(1, username);
            stm.setString(2, password);
            
            rs = stm.executeQuery();
            if (rs.next()) {
                result = true;
            }
        } finally {
            if (rs!=null) {
                rs.close();
            }
            if (stm!=null) {
                stm.close();
            }
            if (con!=null) {
                con.close();
            }
        }
        
        
        return result;
    }
    
}
