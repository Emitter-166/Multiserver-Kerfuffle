package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class Database {
    public static Connection connection;
    static {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:kerfuffle.db");
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS servers(_id TEXT PRIMARY KEY, games INTEGER)");
            statement.execute("CREATE TABLE IF NOT EXISTS kill_messages(_id INTEGER PRIMARY KEY, message TEXT UNIQUE)");
            statement.execute("CREATE TABLE IF NOT EXISTS death_messages(_id INTEGER PRIMARY KEY, message TEXT UNIQUE)");
            statement.execute("CREATE TABLE IF NOT EXISTS winners(_id TEXT UNIQUE, wins INTEGER)");
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
