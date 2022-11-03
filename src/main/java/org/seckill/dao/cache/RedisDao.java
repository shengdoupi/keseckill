package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    //运行期schema

    //class代表类的字节码的对象，通过class对象可以反射拿到这个类（字节码）的属性方法等；
    //RuntimeSchema是基于这个class做了相应的模式，当创建对象时，会根据这个模式来赋予相应的值（序列化的本质：通过字节码和这个对象有哪些属性来将字节码的值传递给这个对象）

    private RuntimeSchema <Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    //jedispool类似于数据库连接池的connection pool

    private JedisPool jedisPool;

    public RedisDao(String ip, int port){
        jedisPool = new JedisPool(ip, port);
    }

    //1：通过redis拿到seckill对象，这样就不用访问db

    public Seckill getSeckill(long seckillId){
        //redis操作逻辑
        try{
            //jedis相当于数据库的connection
            Jedis jedis = jedisPool.getResource();

            try{
                String key = "seckill:" + seckillId;
                //redis并没有实现内部序列化操作
                //get -> byte [] -> 反序列化 ->Object (Seckill)
                //采用自定义序列化，开源社区提供的解决方案
                //如何把redis中的byte[]转换成Object呢？只需告诉这个对象的class，然后protostuff内部有个schema来描述这个class的结构，
                //但这个class必须是个pojo（有get，set方法），不能是String，Long这种；
                byte [] bytes = jedis.get(key.getBytes());
                if(bytes!= null){
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    //seckill被反序列化
                    return seckill;
                }

            } finally {
                jedis.close();
            }
        } catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }
    public String getNumberById(long seckillId){

            Jedis jedis = jedisPool.getResource();
            try{

                String key = "number:" +seckillId;
                return jedis.get(key);
            }finally {
                jedis.close();
            }


    }

    public String getRemainFlagById(long seckillId){
        try{
            Jedis jedis = jedisPool.getResource();
            try{

                String key = "remainFlag:" +seckillId;
                return jedis.get(key);
            }finally {
                jedis.close();
            }

        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }


    public void decrNumberById(long seckillId){
        try{
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "number:" +seckillId;
                jedis.decr(key);
            }finally {
                jedis.close();
            }


        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

    }

    public void setRemainFlagFalse(long seckillId){
        try{
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "remainFlag:" +seckillId;
                jedis.set(key,String.valueOf(0));
            }finally {
                jedis.close();
            }


        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }

    public String putRemainFlagById(long seckillId, int remainFlag){
        try{
            Jedis jedis = jedisPool.getResource();
            try{
                String key = "remainFlag:" +seckillId;
                return jedis.set(key,String.valueOf(remainFlag));
            }finally {
                jedis.close();
            }

        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public void putNumberById(long seckillId, int number){

            Jedis jedis = jedisPool.getResource();

                String key = "number:" +seckillId;

                jedis.set(key,String.valueOf(number));
                jedis.close();



    }


    //2：当发现缓存没有时，去put一个seckill
    public String putSeckill(Seckill seckill){

        //put : 拿到object seckill -> 序列化-> byte [] -> redis
        try{
            Jedis jedis = jedisPool.getResource();

            try{
                String key = "seckill:" + seckill.getSeckillId();
                byte [] bytes = ProtostuffIOUtil.toByteArray(seckill, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存
                int timeout = 60 * 60;
                String result = jedis.setex(key.getBytes(), timeout ,bytes);
                return result;
            } finally{
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }


        return null;
    }

}
