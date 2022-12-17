import Schema.LiftRide;
import Schema.SkiRequest;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

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
                    Gson gson = new Gson();

                    SkiRequest skiRequest = gson.fromJson(message,SkiRequest.class);
                    Jedis jedis = JedisConnectionFactory.getJedis();
                    Transaction transaction = jedis.multi();
/*                    transaction.incr("days:"+skiRequest.getSkierID()+":"+skiRequest.getSeasonID());
                    transaction.incrBy("verticals:"+skiRequest.getSkierID()
                            +":"+skiRequest.getDayID(),10*skiRequest.getLiftID());
                    transaction.sadd("lifts:"+skiRequest.getSkierID()+":"+skiRequest.getDayID(),
                            String.valueOf(skiRequest.getLiftID()));
                    transaction.sadd("visits:"+skiRequest.getResortID()+":"+skiRequest.getDayID(),String.valueOf(skiRequest.getSkierID()));*/
                    transaction.sadd("resort:"+skiRequest.getSkierID()+":"+skiRequest.getResortID()+":"+skiRequest.getSeasonID()+":"+skiRequest.getDayID(),Integer.toString(skiRequest.getSkierID()));
                    String key ="vertical:"+skiRequest.getSkierID();
                    transaction.hincrBy(key,skiRequest.getDayID()+":"+skiRequest.getSeasonID()+":"+skiRequest.getResortID(),skiRequest.getLiftID()*10);
                    transaction.hincrBy(key,skiRequest.getSeasonID()+":"+skiRequest.getResortID(),skiRequest.getLiftID()*10);

                    transaction.exec();
                    transaction.close();
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    jedis.close();
                };
                channel.basicConsume("mainQueue", false, deliverCallback, consumerTag -> { });
            } catch (IOException e) {
                e.printStackTrace();
            }

    }


}
