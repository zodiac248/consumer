import Schema.LiftRide;
import Schema.SkiRequest;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import javax.management.relation.RelationNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer implements Runnable {
    private ConcurrentHashMap map;
    RMQChannelPool rmqChannelPool;
    public Consumer(ConcurrentHashMap map, RMQChannelPool rmqChannelPool){
        this.rmqChannelPool = rmqChannelPool;
        this.map = map;
    }
    @Override
    public void run() {

            try {
                Channel channel = rmqChannelPool.borrowObject();
                channel.queueDeclare("mainQueue", false, false, false, null);
                channel.basicQos(1);
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    Gson gson = new Gson();
                    SkiRequest skiRequest = gson.fromJson(message,SkiRequest.class);
                    map.put(skiRequest.getLiftID(),skiRequest);
                };
                channel.basicConsume("mainQueue", false, deliverCallback, consumerTag -> { });
            } catch (IOException e) {
                e.printStackTrace();
            }

    }


}
