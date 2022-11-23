import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class main {
    public static void main(String[] args) {
        int numThread = 16;
        ConcurrentHashMap map = new ConcurrentHashMap();
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("44.228.237.106");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("username");
        connectionFactory.setPassword("971128");
        connectionFactory.setVirtualHost("roy");
        try {
            Connection connection = connectionFactory.newConnection();
            RMQChannelFactory rmqChannelFactory = new RMQChannelFactory(connection);
            RMQChannelPool rmqChannelPool = new RMQChannelPool(numThread,rmqChannelFactory);
            for (int i = 0; i < numThread; i++) {
                Thread thread = new Thread(new Consumer(map,rmqChannelPool));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
