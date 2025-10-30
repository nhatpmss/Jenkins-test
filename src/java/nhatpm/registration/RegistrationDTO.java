/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nhatpm.registration;

import java.io.Serializable;

/**
 *
 * @author nhatpm
 */
public class RegistrationDTO implements Serializable{
    private String username;
    private String password;
    private String fullname;
    private boolean role;

    public RegistrationDTO() {
    }

    public RegistrationDTO(String username, String password, String fullname, boolean role) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullname() {
        return fullname;
    }

    public boolean isRole() {
        return role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setRole(boolean role) {
        this.role = role;
    }
    
    
}
