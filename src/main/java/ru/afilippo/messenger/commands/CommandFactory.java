package ru.afilippo.messenger.commands;

import ru.afilippo.messenger.messages.Type;
import ru.afilippo.messenger.store.MessageStore;
import ru.afilippo.messenger.store.UserStore;


public class CommandFactory {
    public static Command get(UserStore userStore, MessageStore messageStore, Type messageType){
        Command command;
        System.out.print(messageType);
        switch (messageType){
            case MSG_TEXT:
                command = new TextCommand(messageStore);
                break;
            case MSG_LOGIN:
                command = new LoginCommand(userStore);
                break;
            case MSG_REGISTER:
                command = new RegisterCommand(userStore);
                break;
            case MSG_CHAT_CREATE:
                command = new ChatCreateCommand(messageStore);
                break;
            case MSG_INFO:
                command = new UserInfoCommand(userStore);
                break;
            case MSG_CHAT_LIST:
                command = new ChatListCommand(messageStore);
                break;
            case MSG_CHAT_HISTORY:
                command = new ChatHistoryCommand(messageStore);
                break;
            default:
                command = null;
        }
        System.out.print(command);
        return command;
    }
}
