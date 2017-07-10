package com.github.onsdigital.zebedee.json;

/**
 * Created by david on 12/03/2015.
 */
public class Credentials {

    public String email;
    public String password;

    /**
     * Optional - only needed when changing a password.
     */
    public String oldPassword;

    /**
     * Optional - only needed when creating the first password.
     */
    public String verify;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getVerify() {
        return verify;
    }

    public void setVerify(String verify) {
        this.verify = verify;
    }
}
