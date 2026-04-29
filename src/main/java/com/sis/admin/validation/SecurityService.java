package com.sis.admin.validation;

public class SecurityService {
    public String hashPassword(String plainText) {

        return "hashed_" + plainText; // Stubbed hash
    }
}