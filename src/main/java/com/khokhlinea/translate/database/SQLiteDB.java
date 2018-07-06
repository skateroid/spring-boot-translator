package com.khokhlinea.translate.database;

import com.khokhlinea.translate.connection.MyConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.Date;

public class SQLiteDB {
    private MyConnection myConnection;
    private Connection connection;
    private Statement statement;
    private PreparedStatement ps;
    private static final SQLiteDB ourInstance = new SQLiteDB();

    public static SQLiteDB getInstance() {
        return ourInstance;
    }

    private SQLiteDB() {
        myConnection = new MyConnection();
        connect();
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:request_info.db");
            statement = connection.createStatement();
            checkCreateMainTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void checkCreateMainTable() throws SQLException {
        connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS connection_info (" +
                "    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "    time_of_request CHAR (30) NOT NULL, " +
                "    params CHAR (255) NOT NULL," +
                "    ip CHAR(30) NOT NULL );");
    }

    public void addDataToTable(String text, String from, String to) {
        StringBuilder params = new StringBuilder();
        params.append("text=").append(text).append(" from=").append(from).append(" to=").append(to);
        Date date = new Date();
        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement("INSERT INTO connection_info(time_of_request, params, ip) VALUES (?, ?, ?)");
            ps.setString(1, date.toString());
            ps.setString(2, params.toString());
            ps.setString(3, getIP());
            ps.addBatch();
            ps.executeBatch();
            connection.commit();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getIP() {
        try {
            URL myIpURL = new URL(myConnection.getProperties().getProperty("myIpURL"));
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    myIpURL.openStream()));
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void disconnect() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
