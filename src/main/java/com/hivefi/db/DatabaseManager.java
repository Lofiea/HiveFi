package com.hivefi.db;

import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.SQLException; 

public class DatabaseManager {
    private static String resolveUrl() {
        String prop = System.getProperty("HIVEFI_DB_URL");
        if (prop != null && !prop.isBlank()) return prop;
        String env = System.getenv("HIVEFI_DB_URL");
        if (env != null && !env.isBlank()) return env;
        return "jdbc:sqlite:hivefi.db"; 
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(resolveUrl());
    }

    private DatabaseManager() {}
}

