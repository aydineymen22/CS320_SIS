package com.sis.admin.validation;

import org.mindrot.jbcrypt.BCrypt;

public class SecurityService {
    public String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt());
    }
    public boolean verifyPassword(String plainText, String hashedPassword) {
        if (plainText == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainText, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
