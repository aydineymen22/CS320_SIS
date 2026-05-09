package com.sis.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/sis_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                readSetting("db.url", "DB_URL", DEFAULT_URL),
                readSetting("db.user", "DB_USER", DEFAULT_USER),
                readSetting("db.password", "DB_PASSWORD", DEFAULT_PASSWORD));
    }

    private static String readSetting(String propertyName, String environmentName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String environmentValue = System.getenv(environmentName);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        return defaultValue;
    }
}
