package ru.afilippo.messenger.messages;


import ru.afilippo.messenger.store.User;


public class LoginMessage extends Message {

    private String login;
    private String password;

    public LoginMessage() {
//        setType(Type.MSG_LOGIN);
    }

    public LoginMessage(User sender, String login, String password) {
        super(sender, Type.MSG_LOGIN);

        this.login = login;
        this.password = password;
    }

    public LoginMessage(User sender) {
        super(sender, Type.MSG_LOGIN);

        this.login = sender.getLogin();
        this.password = sender.getPassword();
    }

    public LoginMessage(Long senderId, Long chatId, Type messageType, String login, String password) {
        super(senderId, chatId, Type.MSG_LOGIN);

        if (messageType != Type.MSG_LOGIN) {
            System.out.println("Smth goes wrong");
        }

        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginMessage{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}' + "\n" + super.toString();
    }
}
