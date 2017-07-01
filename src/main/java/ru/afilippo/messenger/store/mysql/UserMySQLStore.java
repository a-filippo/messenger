package ru.afilippo.messenger.store.mysql;

import ru.afilippo.messenger.store.User;
import ru.afilippo.messenger.store.UserStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by afilippo on 26.06.17.
 */
public class UserMySQLStore implements UserStore{
    private final String TABLE_USER = "user";
    private final String USER_ID = "user_id";
    private final String LOGIN = "login";
    private final String PASSWORD = "password";

    private MySQLStore store;

    public UserMySQLStore(MySQLStore store){
        this.store = store;
    }

    @Override
    public User addUser(User user) {
        User[] users = new User[1];
        store.query((Connection con) -> {
            Statement stmt = con.createStatement();
            String queryString = "INSERT INTO " + TABLE_USER + " set " +
                    LOGIN + " = ?, " +
                    PASSWORD + " = ?";
            PreparedStatement query = con.prepareStatement(queryString, PreparedStatement.RETURN_GENERATED_KEYS);
            query.setString(1, user.getLogin());
            query.setString(2, user.getPassword());

            query.executeUpdate();

            ResultSet rs = query.getGeneratedKeys();
            rs.next();
            long id = rs.getLong(1);
            rs.close();

            users[0] = new User(id, user.getLogin(), user.getPassword());

            stmt.close();
            query.close();
        });
        return users[0];
    }

    @Override
    public User updateUser(User user) {
        return null;
    }

    @Override
    public User getUser(String login, String password) {
        User[] users = new User[1];
        store.query((Connection con) -> {
            Statement stmt = con.createStatement();
            String queryString = "select " +
                    USER_ID + " from " +
                    TABLE_USER + " where " +
                    LOGIN + " = ? AND " +
                    PASSWORD + " = ?";
            PreparedStatement query = con.prepareStatement(queryString);
            query.setString(1, login);
            query.setString(2, password);

            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                long id = rs.getLong(USER_ID);
                users[0] = new User(id, login, password);
            } else {
                users[0] = null;
            }

            rs.close();
            stmt.close();
            query.close();
        });
        return users[0];
    }

    @Override
    public User getUserById(Long id) {
        User[] users = new User[1];
        store.query((Connection con) -> {
            Statement stmt = con.createStatement();
            String queryString = "select " +
                    LOGIN + ", " +
                    PASSWORD + " from " +
                    TABLE_USER + " where " +
                    USER_ID + " = ?";
            PreparedStatement query = con.prepareStatement(queryString);
            query.setLong(1, id);

            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                String login = rs.getString(LOGIN);
                String password = rs.getString(PASSWORD);
                users[0] = new User(id, login, password);
            } else {
                users[0] = null;
            }

            rs.close();
            stmt.close();
            query.close();
        });
        return users[0];
    }
}
