package com.sis.admin.validation;
import com.sis.admin.repository.UserRepository;

public class UserValidator {
    private final UserRepository repository = new UserRepository();

    public boolean isUniqueEmail(String email) {
        return !repository.existsByEmail(email);
    }
}