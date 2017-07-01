package ru.afilippo.messenger.teacher.client;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.afilippo.messenger.messages.*;
import ru.afilippo.messenger.net.Protocol;
import ru.afilippo.messenger.net.ProtocolException;
import ru.afilippo.messenger.net.StringProtocol;
import ru.afilippo.messenger.store.User;


public class MessengerClient {

    private static final int PORT = 8283;
    private static final String HOST = "localhost";

    private Protocol protocol;
    private int port;
    private String host;
    private User user;

    private InputStream in;
    private OutputStream out;

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    private void setHost(String host) {
        this.host = host;
    }

    private void initSocket() throws IOException {
        Socket socket = new Socket(host, port);
        in = socket.getInputStream();
        out = socket.getOutputStream();


        Thread socketListenerThread = new Thread(() -> {
            final byte[] buf = new byte[1024 * 64];
            System.out.println("Starting listener thread...");
            while (!Thread.currentThread().isInterrupted()) {
                try {

                    int read = in.read(buf);
                    if (read > 0) {

                        Message msg = protocol.decode(Arrays.copyOf(buf, read));
                        onMessage(msg);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to process connection: " + e);
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });

        socketListenerThread.start();
    }

    /**
     * Реагируем на входящее сообщение
     */
    private void onMessage(Message msg) {

        switch (msg.getType()) {
            case MSG_LOGIN:
                LoginMessage loginMessage = (LoginMessage) msg;
                this.user = new User(loginMessage.getSenderId(), loginMessage.getLogin(), loginMessage.getPassword());

                System.out.println("You are logged in, " + user.getLogin()
                        + ". Your id is " + user.getId());
                break;
            case MSG_TEXT:
                TextMessage textMessage = (TextMessage) msg;
                System.out.println("You received a new message from id " + textMessage.getSenderId() +
                        ", chat " + textMessage.getChatId() +
                        ", text: " + textMessage.getText());
                break;

            case MSG_INFO_RESULT:
            case MSG_STATUS:
                StatusMessage statusMessage = (StatusMessage) msg;
                System.out.println(statusMessage.getInfo());
                break;

            case MSG_CHAT_CREATE_RESULT:
                ChatCreateResultMessage resultMessage = (ChatCreateResultMessage) msg;
                long created = resultMessage.getCreatedChatId();
                System.out.println("Created chat with id " + created);
                break;

            case MSG_CHAT_LIST_RESULT:
                ChatListResultMessage message = (ChatListResultMessage) msg;
                StringBuilder chats = new StringBuilder();
                chats.append("Your chats: ");

                for (Long chatId : message.getChats()) {
                    chats.append(chatId).append(" ");
                }

                System.out.println(chats.toString());

                break;

            case MSG_CHAT_HISTORY_RESULT:
                ChatHistoryResultMessage chatHistoryResultMessage = (ChatHistoryResultMessage) msg;
                StringBuilder history = new StringBuilder();
                history.append("Your history: \n");

                for (String historyMessage : chatHistoryResultMessage.readAllMessages()) {
                    history.append(historyMessage).append("\n");
                }

                System.out.println(history.toString());
        }
    }

    public void processInput(String line) throws IOException, ProtocolException {
        String[] tokens = line.split("\\s+");
        System.err.println("Tokens: " + Arrays.toString(tokens));
        String cmdType = tokens[0];
        switch (cmdType) {
            case "/login":
                if (tokens.length != 3) {
                    System.out.println("Not enough arguments. Make sure that you mention login and password");
                } else {
                    String login = tokens[1];
                    String password = tokens[2];
                    Message loginMessage = new LoginMessage(this.user, login, password);
                    send(loginMessage);
                }
                break;
            case "/help":
                String printHelp =
                    "/help - показать список команд и общий хэлп по месседжеру\n" +
                    "/login <логин_пользователя> <пароль> - залогиниться\n" +
                    "/register <логин_пользователя> <пароль> - зарегистрироваться\n" +
                    "/info [id] - получить всю информацию о пользователе, без аргументов - о себе\n" +
                    "/chat_list - получить список чатов пользователя\n" +
                    "/chat_create <user_id list> - создать новый чат, список пользователей приглашенных в чат\n" +
                    "/chat_history <chat_id> - список сообщений из указанного чата\n" +
                    "/text <id> <message> - отправить сообщение в заданный чат\n" +
                    "/q - выйти\n";
                System.out.println(printHelp);
                break;
            case "/text":

                if (tokens.length < 3) {
                    System.out.println("Not enough arguments. Make sure that you mention chat and text to this chat");
                } else {
                    try {
                        long chatId = Long.valueOf(tokens[1]);

                        StringBuilder text = new StringBuilder();
                        for (int i = 2; i < tokens.length; i++) {
                            text.append(tokens[i]);
                            text.append(" ");
                        }

                        if (text.toString().length() > 200) {
                            System.out.println("Your text is too big. Notice that 200 symbols is max number of them.");
                        }

                        TextMessage textMessage = new TextMessage(chatId, (this.user != null) ? this.user.getId() : 0, text.toString());
                        send(textMessage);
                    } catch (NumberFormatException e) {
                        System.out.println();
                    }
                }
                break;

            case "/chat_create":
                if (tokens.length < 2) {
                    System.out.println("Not enough arguments");
                } else {
                    try {
                        List<Long> participants = new ArrayList<>();

                        for (int i = 1; i < tokens.length; i++){
                            participants.add(Long.valueOf(tokens[i]));
                        }

                        ChatCreateMessage chatCreateMessage = new ChatCreateMessage(user, participants);
                        send(chatCreateMessage);

                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        System.out.println("Can't create message. Whoops! :(");
                    } catch (NumberFormatException numberFormatException) {
                        System.out.println("Can't parse participants ids");
                    }
                }

                break;

            case "/register":
                if (tokens.length != 3) {
                    System.out.println("Not enough arguments. Make sure that you mention login and password");
                } else {
                    String login = tokens[1];
                    String password = tokens[2];
                    Message loginMessage = new LoginMessage(this.user, login, password);
                    loginMessage.setType(Type.MSG_REGISTER);
                    send(loginMessage);
                }
                break;

            case "/info":

                if (tokens.length > 2) {
                    System.out.println("Too much arguments");
                } else {
                    try {
                        Long userId = tokens.length == 2 ? Long.valueOf(tokens[1]) : null;

                        UserInfoMessage userInfoMessage = new UserInfoMessage(this.user, userId);
                        send(userInfoMessage);

                    } catch (NumberFormatException numberFormatException) {
                        System.out.println("Can't parse friend id");
                    }
                }

                break;

            case "/chat_list":

                if (tokens.length > 2) {
                    System.out.println("Too much arguments. They are redundant.");
                } else {
                    try {

                        ChatListMessage chatListMessage = new ChatListMessage(this.user);
                        send(chatListMessage);

                    } catch (InstantiationException e) {
                        System.out.println("Something went wrong.");
                        e.printStackTrace();
                    }
                }

                break;

            case "/chat_history":

                if (tokens.length != 2) {
                    System.out.println("This command requires 2 arguments");
                } else {
                    try {

                        Long chatId = Long.valueOf(tokens[1]);

                        ChatHistoryMessage chatHistoryMessage = new ChatHistoryMessage(this.user, chatId);
                        send(chatHistoryMessage);

                    } catch (NumberFormatException numberFormatException) {
                        System.out.println("Can't parse chat id");
                    } catch (InstantiationException e) {
                        System.out.println("Something went wrong.");
                        e.printStackTrace();
                    }
                }

                break;

            default:
                System.err.println("Invalid input: " + line);
        }
    }

    /**
     * Отправка сообщения в сокет клиент -> сервер
     */
    private void send(Message msg) throws IOException, ProtocolException {
        System.err.println(msg);
        out.write(protocol.encode(msg));
        out.flush();
    }

    public static void main(String[] args) throws Exception {
        new MessengerClient().run(args);
    }

    public void init(){
        setHost(HOST);
        setPort(PORT);
        setProtocol(new StringProtocol());
        try {
            initSocket();
        } catch (Exception e) {
            System.err.println("Application failed. " + e);
        }
    }

    private void listenInput(){
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {


            System.out.println("$");
            while (true) {
                String input = bufferedReader.readLine();
                if ("/q".equals(input)) {
                    bufferedReader.close();
                    System.exit(0);
                }
                try {
                    processInput(input);
                } catch (ProtocolException | IOException e) {
                    System.err.println("Failed to process user input " + e);
                }
            }
        } catch (Exception e) {
            System.err.println("Application failed. " + e);
        }
    }

    private void run(String[] args) throws Exception {
        init();
        listenInput();
    }
}