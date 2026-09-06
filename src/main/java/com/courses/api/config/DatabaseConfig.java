package com.courses.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseConfig(Path propertiesPath) {
        Properties properties = loadProperties(propertiesPath);
        this.url = require(properties, "db.url");
        this.username = require(properties, "db.username");
        // Local MySQL root often has an empty password; key must still exist.
        if (!properties.containsKey("db.password")) {
            throw new IllegalStateException("Missing required configuration value: db.password");
        }
        String rawPassword = properties.getProperty("db.password");
        this.password = rawPassword == null ? "" : rawPassword;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private static Properties loadProperties(Path propertiesPath) {
        if (!Files.exists(propertiesPath)) {
            throw new IllegalStateException(
                    "Missing configuration file: " + propertiesPath.toAbsolutePath()
                            + ". Create config/db.properties with db.url, db.username, and db.password.");
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(propertiesPath)) {
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration from " + propertiesPath.toAbsolutePath(), e);
        }
        return properties;
    }

    private static String require(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration value: " + key);
        }
        return value.trim();
    }
}
