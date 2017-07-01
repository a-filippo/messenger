package ru.afilippo.messenger.store.mysql;

import java.sql.*;
import java.util.Vector;

public class MySQLStore {
    private static final String DB_URL = "jdbc:mysql://localhost:8889/messenger?useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private final int INITIAL_CONN_COUNT = 10;

    private Vector<Connection> availableConns;
    private Vector<Connection> usedConns;
    private String url;

    public MySQLStore(){
        availableConns = new Vector<>();
        usedConns = new Vector<>();

        for (int i = 0; i < INITIAL_CONN_COUNT; i++) {
            availableConns.addElement(getConnection());
        }
    }

    private Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    private synchronized Connection retrieve() throws SQLException {
        Connection newConn = null;
        if (availableConns.size() == 0) {
            newConn = getConnection();
        } else {
            newConn = (Connection) availableConns.lastElement();
            availableConns.removeElement(newConn);
        }
        usedConns.addElement(newConn);
        return newConn;
    }

    private synchronized void putback(Connection c) throws NullPointerException {
        if (c != null) {
            if (usedConns.removeElement(c)) {
                availableConns.addElement(c);
            } else {
                throw new NullPointerException("Connection not in the usedConns array");
            }
        }
    }

    public int getAvailableConnsCnt() {
        return availableConns.size();
    }

    void query(QueryBody queryBody){
        Connection con = null;

        try {
            con = retrieve();

            queryBody.body(con);

        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            putback(con);
        }

//        System.out.println(getAvailableConnsCnt());
    }

    int size(ResultSet rs) throws SQLException{
        rs.last();
        int size = rs.getRow();
        rs.beforeFirst();
        return size;
    }

    interface QueryBody{
        void body(Connection con) throws SQLException;
    }
}
