package ru.afilippo.messenger.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.afilippo.messenger.commands.Command;
import ru.afilippo.messenger.commands.CommandFactory;
import ru.afilippo.messenger.messages.Message;
import ru.afilippo.messenger.store.MessageStore;
import ru.afilippo.messenger.store.UserStore;
import ru.afilippo.messenger.store.mysql.MessageMySQLStore;
import ru.afilippo.messenger.store.mysql.MySQLStore;
import ru.afilippo.messenger.store.mysql.UserMySQLStore;

/**
 *
 */
public class MessengerServer {

    static {
        System.setProperty("logback.configurationFile", "./cfg/log4j2");
    }

    private static Logger LOGGER = LogManager.getLogger(MessengerServer.class.getName());

    public static void main(String[] args) {

        MySQLStore store = new MySQLStore();
        UserStore userStore = new UserMySQLStore(store);
        MessageStore messageStore = new MessageMySQLStore(store);

        BlockingQueue<Session> sessions = new LinkedBlockingQueue<>();

        HashMap<SocketChannel, ByteBuffer> map = new HashMap<>();

        try (ServerSocketChannel open = openAndBind()) {
            open.configureBlocking(false);
            while (true) {
                SocketChannel accept = open.accept();

                if (accept != null) {
                    System.out.println("connected");
                    accept.configureBlocking(false);
                    map.put(accept, ByteBuffer.allocateDirect(4096));
                }
                map.keySet().removeIf(sc -> !sc.isOpen());

                map.forEach((sc, byteBuffer) -> {
                    try {
                        int read = sc.read(byteBuffer);
                        if (read == -1) {
                            close(sc);
                        } else if (read > 0) {
                            byteBuffer.flip();

                            Message msg = new StringProtocol().decode(read(byteBuffer));
                            System.out.println(msg);

                            byteBuffer.clear();

                            Session session = fixSessionChoice(sessions, msg.getSenderId());

                            if (session == null) {
//                                session = new Session(sc);
//                                sessions.add(session);
                            }

                            System.out.println("before factory " + msg.getType());
                            Command command = CommandFactory.get(userStore, messageStore, msg.getType());

                            Session[] sess = new Session[]{session};
                            new Thread(() -> {
                                command.execute(sess[0], msg, sessions);
                            }).start();

                        }
                    } catch (IOException e) {
                        close(sc);
                        e.printStackTrace();
                        LOGGER.error("Problems with SocketChannel: ", e);
                    } catch (ProtocolException e) {
                        LOGGER.error("Problems with SocketChannel closing: ", e);
                        e.printStackTrace();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static ServerSocketChannel openAndBind() throws IOException {
        ServerSocketChannel open = ServerSocketChannel.open();

        open.bind(new InetSocketAddress(8283));

        return open;
    }

    private static void close(SocketChannel sc) {
        try {
            sc.close();
        } catch (IOException e) {
            LOGGER.error("Can't close SocketChannel", e);
        }
    }

    private static byte[] read(ByteBuffer data) {
        byte[] decoded = new byte[data.limit()];
        for (int i = 0; i < data.limit(); i++) {
            decoded[i] = data.get(i);
        }
        return decoded;
    }

    private static Session fixSessionChoice(BlockingQueue<Session> sessions, Long senderId) {
        if (senderId == null) {
            return null;
        }
        for (Session session : sessions) {
            if (session.getUser() != null && session.getUser().getId() == senderId) {
                return session;
            }
        }
        return null;
    }
}
