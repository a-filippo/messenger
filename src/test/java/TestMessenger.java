import ru.afilippo.messenger.net.MessengerServer;
import ru.afilippo.messenger.teacher.client.MessengerClient;

/**
 * Created by afilippo on 30.06.17.
 */
public class TestMessenger {
    public static void main(String[] args) {
        Thread server = new Thread(() -> {
            MessengerServer.main(args);
        });
        server.start();

        try {
            Thread.sleep(2000L);
        } catch (Exception e){
            e.printStackTrace();
        }

//        for (int i = 0; i < 10000; i++){
//            Thread client = new Thread(() -> {
//                try {
//                    MessengerClient.main(args);
//                } catch (Exception e){
//                    e.printStackTrace();
//                }
//            });
//            System.out.println(i);
//        }

//        new Thread(() -> {
            try {
                MessengerClient client1 = initClient();
//                MessengerClient client2 = initClient();

                Thread.sleep(3000L);

                client1.processInput("/login afilippo 25121995");
                Thread.sleep(1000L);
                client1.processInput("/chat_create 2 3 4 5 6");
//                client2.processInput("/login 1 1");
            } catch (Exception e){
                e.printStackTrace();
            }
//        });
    }

    private static MessengerClient initClient(){
        MessengerClient client = new MessengerClient();
        client.init();
        return client;
    }
}
