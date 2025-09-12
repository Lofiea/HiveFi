package com.hivefi.utils;

import java.nio.file.*;
import java.sql.*;

public class Db {
  private static final String REL = "db/hivefi.db";

  private static String url() throws Exception {
    Path dbPath = Paths.get(System.getProperty("user.dir")).resolve(REL).toAbsolutePath();
    Files.createDirectories(dbPath.getParent());
    System.out.println("SQLite file → " + dbPath); // <-- see where it’s writing
    return "jdbc:sqlite:" + dbPath.toString();
  }

  public static Connection get() throws SQLException {
    try {
      return DriverManager.getConnection(url());
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }

  public static void init() throws SQLException {
    try (Connection c = get(); Statement s = c.createStatement()) {
      s.execute("""
        CREATE TABLE IF NOT EXISTS rates(
          base TEXT NOT NULL, fetched_at INTEGER NOT NULL, code TEXT NOT NULL,
          rate REAL NOT NULL, PRIMARY KEY(base,code)
        );
      """);
      s.execute("""
        CREATE TABLE IF NOT EXISTS expenses(
          id TEXT PRIMARY KEY, trip_id TEXT NOT NULL, category TEXT NOT NULL,
          amount_native REAL NOT NULL, currency TEXT NOT NULL, ts INTEGER NOT NULL
        );
      """);
    }
  }
}
