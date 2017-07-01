package ru.afilippo.messenger.store.mysql;

import ru.afilippo.messenger.messages.Message;
import ru.afilippo.messenger.messages.TextMessage;
import ru.afilippo.messenger.store.MessageStore;
import ru.afilippo.messenger.store.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageMySQLStore implements MessageStore {
    private final String TABLE_CHAT = "chat";
    private final String TABLE_MESSAGE = "message";
    private final String CHAT_ID = "chat_id";
    private final String MESSAGE_ID = "message_id";
    private final String USER_ID = "user_id";
    private final String TEXT = "text";

    private MySQLStore store;

    public MessageMySQLStore(MySQLStore store){
        this.store = store;
    }


    @Override
    public List<Long> getChatsByUserId(Long userId) {
        Long[][] chats = new Long[1][];
        store.query((Connection con) -> {
            Statement stmt = con.createStatement();
            String queryString = "select " +
                    CHAT_ID + " from " +
                    TABLE_CHAT + " where " +
                    USER_ID + " = ?";
            PreparedStatement query = con.prepareStatement(queryString);
            query.setLong(1, userId);

            ResultSet rs = query.executeQuery();
            int size = store.size(rs);
            if (size > 0){
                chats[0] = new Long[size];
                int i = 0;
                while (rs.next()) {
                    long chat = rs.getLong(CHAT_ID);
                    chats[0][i] = chat;
                }
            } else {
                chats[0] = new Long[0];
            }

            rs.close();
            stmt.close();
            query.close();
        });

        return Arrays.asList(chats[0]);
    }

    @Override
    public List<Long> getMessagesFromChat(Long chatId) {
        Long[][] messages = new Long[1][];
        store.query((Connection con) -> {
            Statement stmt = con.createStatement();
            String queryString = "select " +
                    MESSAGE_ID + " from " +
                    TABLE_MESSAGE + " where " +
                    CHAT_ID + " = ? order by " + MESSAGE_ID + " ASC";
            PreparedStatement query = con.prepareStatement(queryString);
            query.setLong(1, chatId);

            ResultSet rs = query.executeQuery();
            int size = store.size(rs);
            if (size > 0){
                messages[0] = new Long[size];
                int i = 0;
                while (rs.next()) {
                    long chat = rs.getLong(CHAT_ID);
                    messages[0][i] = chat;
                }
            } else {
                messages[0] = new Long[0];
            }

            rs.close();
            stmt.close();
            query.close();
        });

        return Arrays.asList(messages[0]);
    }

    @Override
    public Message getMessageById(Long messageId) {
        Message[] messages = new Message[1];
        store.query((Connection con) -> {
            Statement stmt = con.createStatement();
            String queryString = "select " +
                    CHAT_ID + ", " +
                    TEXT + ", " +
                    USER_ID + " from " +
                    TABLE_MESSAGE + " where " +
                    MESSAGE_ID + " = ?";
            PreparedStatement query = con.prepareStatement(queryString);
            query.setLong(1, messageId);

            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                long chat = rs.getLong(CHAT_ID);
                long userId = rs.getLong(USER_ID);
                String text = rs.getString(TEXT);
                messages[0] = new TextMessage(chat, userId, text);
            } else {
                messages[0] = null;
            }

            rs.close();
            stmt.close();
            query.close();
        });
        return messages[0];
    }

    @Override
    public void addMessage(Long chatId, Message message) {
        TextMessage textMessage = (TextMessage) message;

        store.query((Connection con) -> {
            Statement stmt = con.createStatement();
            String queryString = "INSERT INTO " + TABLE_MESSAGE + " set " +
                    CHAT_ID + " = ?, " +
                    USER_ID + " = ?, " +
                    TEXT + " = ?";
            PreparedStatement query = con.prepareStatement(queryString);
            query.setLong(1, chatId);
            query.setLong(2, textMessage.getSenderId());
            query.setString(3, textMessage.getText());

            query.executeUpdate();

            stmt.close();
            query.close();
        });
    }

    @Override
    public void addUserToChat(Long userId, Long chatId) {
        store.query((Connection con) -> {
            Statement stmt = con.createStatement();
            String queryString = "INSERT INTO " + TABLE_CHAT + " set " +
                    CHAT_ID + " = ?, " +
                    USER_ID + " = ? ";
            PreparedStatement query = con.prepareStatement(queryString);
            query.setLong(1, chatId);
            query.setLong(2, userId);

            query.executeUpdate();

            stmt.close();
            query.close();
        });
    }

    public Long createChat(Long[] users){
        Long[] maxId = new Long[1];
        store.query((Connection con) -> {
            Statement stmt = con.createStatement();
            String queryString = "select max(" + CHAT_ID + ") as max_chat_id from " + TABLE_CHAT;

            PreparedStatement query = con.prepareStatement(queryString);

            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                maxId[0] = rs.getLong("max_chat_id");
            } else {
                maxId[0] = 0L;
            }

            rs.close();
            stmt.close();
            query.close();
        });

        maxId[0]++;

        for (int i = 0; i < users.length; i++){
            Long[] user = new Long[]{users[i]};
            store.query((Connection con) -> {
                Statement stmt = con.createStatement();
                String queryString = "INSERT INTO " + TABLE_CHAT + " set " +
                        CHAT_ID + " = ?, " +
                        USER_ID + " = ? ";
                PreparedStatement query = con.prepareStatement(queryString);
                query.setLong(1, maxId[0]);
                query.setLong(2, user[0]);

                query.executeUpdate();

                stmt.close();
                query.close();
            });
        }

        return maxId[0];
    }

    public Long[] getUserIdsByChatId(long chatId){
        Long[][] ids = new Long[1][];
        store.query((Connection con) -> {
            Statement stmt = con.createStatement();
            String queryString = "select " +
                    USER_ID + " from " +
                    TABLE_CHAT + " where " +
                    CHAT_ID + " = ?";
            PreparedStatement query = con.prepareStatement(queryString);
            query.setLong(1, chatId);

            ResultSet rs = query.executeQuery();
            int size = store.size(rs);
            ids[0] = new Long[size];
            int i = 0;
            while (rs.next()) {
                ids[0][i++] = rs.getLong(USER_ID);
            }

            rs.close();
            stmt.close();
            query.close();
        });
        return ids[0];
    }

    public TextMessageModel[] getHistoryMessagesByChatId(Long chatId){
        TextMessageModel[][] messages = new TextMessageModel[1][];
        store.query((Connection con) -> {
            Statement stmt = con.createStatement();
            String queryString = "select " +
                    USER_ID + ", " +
                    TEXT + " from " +
                    TABLE_MESSAGE + " where " +
                    CHAT_ID + " = ? order by " + MESSAGE_ID + " asc";
            PreparedStatement query = con.prepareStatement(queryString);
            query.setLong(1, chatId);

            ResultSet rs = query.executeQuery();
            int size = store.size(rs);
            messages[0] = new TextMessageModel[size];
            int i = 0;
            while (rs.next()) {
                messages[0][i++] = new TextMessageModel(rs.getLong(USER_ID), rs.getString(TEXT));
            }

            rs.close();
            stmt.close();
            query.close();
        });
        return messages[0];
    }
}
