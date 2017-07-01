package ru.afilippo.messenger.net;

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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by afilippo on 30.06.17.
 */
public class MessengerNIOServer {
    private static Logger LOGGER = LogManager.getLogger(MessengerServer.class.getName());
    private Selector selector;
    private Map<SocketChannel,List> dataMapper;
    private InetSocketAddress listenAddress;
    private UserStore userStore;
    private MessageStore messageStore;
    private BlockingQueue<Session> sessions;

    public static void main(String[] args) throws Exception {
        new MessengerNIOServer("localhost", 8283).startServer();

    }

    public MessengerNIOServer(String address, int port) throws IOException {
        listenAddress = new InetSocketAddress(address, port);
        dataMapper = new HashMap<>();
        MySQLStore store = new MySQLStore();
        this.userStore = new UserMySQLStore(store);
        this.messageStore = new MessageMySQLStore(store);
        sessions = new LinkedBlockingQueue<>();
        ThreadPool.init();
    }

    // create server channel
    private void startServer() throws IOException {
        this.selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // retrieve server socket and bind to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started...");

        while (true) {
            // wait for events
            this.selector.select();

            //work on selected keys
            Iterator keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();

                // this is necessary to prevent the same key from coming up
                // again the next time around.
                keys.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    this.accept(key);
                }
                else if (key.isReadable()) {
                    this.read(key);
                }
            }
        }
    }

    //accept a connection made to this channel's socket
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr);

        // register channel with selector for further IO
        dataMapper.put(channel, new ArrayList());
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    //read from the socket channel
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = channel.read(buffer);

        if (numRead == -1) {
            this.dataMapper.remove(channel);
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        try{

            Message msg = new StringProtocol().decode(data);
            System.out.println(msg);

            Session session = fixSessionChoice(this.sessions, msg.getSenderId());

            if (session == null) {
//                session = new Session(channel);
                session = new Session((message, protocol) -> {
                    byte[] encoded = protocol.encode(message);
                    ByteBuffer buf = ByteBuffer.allocate(5096);
                    buf.put(encoded);
                    buf.flip();
                    channel.write(buf);
                    buf.compact();
                });

                sessions.add(session);
            }

            Command command = CommandFactory.get(this.userStore, this.messageStore, msg.getType());

            Session[] sess = new Session[]{session};
            ThreadPool.addWork(() -> {
                command.execute(sess[0], msg, sessions);
            });
        } catch (ProtocolException e) {
            LOGGER.error("Problems with SocketChannel closing: ", e);
            e.printStackTrace();
        }
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
