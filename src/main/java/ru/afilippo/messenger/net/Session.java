package ru.afilippo.messenger.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import ru.afilippo.messenger.messages.StatusMessage;
import ru.afilippo.messenger.store.User;
import ru.afilippo.messenger.messages.Message;

/**
 * Сессия связывает бизнес-логику и сетевую часть.
 * Бизнес логика представлена объектом юзера - владельца сессии.
 * Сетевая часть привязывает нас к определнному соединению по сети (от клиента)
 */
public class Session {
    private static Logger LOGGER = LogManager.getLogger(Session.class.getName());

    private User user;

    private Protocol protocol;

    private SendMessage sendMessage;

//    private SocketChannel socketChannel;

//    public Session(SocketChannel socketChannel) {
//        this.socketChannel = socketChannel;
//        this.protocol = new StringProtocol();
//    }

    public Session(SendMessage sendMessage){
        this.protocol = new StringProtocol();
        this.sendMessage = sendMessage;
    }

    public void send(Message message) throws ProtocolException, IOException {
//        System.out.println();
//        System.out.println();
//
//        byte[] encoded = protocol.encode(message);
//        ByteBuffer buf = ByteBuffer.allocate(5096);
//        buf.put(encoded);
//        buf.flip();
//        socketChannel.write(buf);
//        buf.compact();

        sendMessage.send(message, this.protocol);


//        LOGGER.log(Level.INFO, "Sending info: {chat=" + message.getChatId() + ", senderId=" + message.getSenderId() + "}");

//        byte[] encoded = protocol.encode(message);
//        ByteBuffer buf = byteBufferPool.get();
//                ByteBuffer.allocate(5096);
//        buf.put(encoded);
//        buf.flip();
//        buf.compact();
        //1 share buffer to selector thread
//        socket2BufferMap.put(socketChannel, buf);
        //2 notify worker / io thread about socket is ready to be written
//        socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    public boolean isLogged(){
        if (this.user == null) {
            try {
                this.send(new StatusMessage("You are not logged in"));
            } catch (ProtocolException | IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public interface SendMessage{
        public void send(Message message, Protocol protocol) throws ProtocolException, IOException;
    }
}