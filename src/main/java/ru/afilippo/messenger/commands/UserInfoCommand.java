package ru.afilippo.messenger.commands;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.afilippo.messenger.messages.Message;
import ru.afilippo.messenger.messages.Type;
import ru.afilippo.messenger.messages.StatusMessage;
import ru.afilippo.messenger.messages.UserInfoMessage;
import ru.afilippo.messenger.net.ProtocolException;
import ru.afilippo.messenger.net.Session;
import ru.afilippo.messenger.store.User;
import ru.afilippo.messenger.store.UserStore;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class UserInfoCommand extends Command {

    private static Logger LOGGER = LogManager.getLogger(UserInfoCommand.class.getName());
    private UserStore userStore;

    public UserInfoCommand(UserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public void execute(Session session, Message message, BlockingQueue<Session> sessions) {
        if (!session.isLogged()) return;

        try {
            UserInfoMessage userInfoMessage = (UserInfoMessage) message;

            User user;
            if (userInfoMessage.getUserId() == null) {
                user = userStore.getUserById(session.getUser().getId());
            } else {
                user = userStore.getUserById(userInfoMessage.getUserId());
            }

            Message info = new StatusMessage(user.toString());
            info.setType(Type.MSG_INFO_RESULT);

            session.send(info);
        } catch (ProtocolException | IOException e) {
            LOGGER.error("Can't get user info: ", e);
            try {
                session.send(new StatusMessage("Can't find user with this id"));
            } catch (ProtocolException | IOException e1) {
                LOGGER.error("Can't send message: ", e1);
            }
        }

    }
}
