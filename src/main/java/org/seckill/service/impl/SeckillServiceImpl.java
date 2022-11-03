package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillExecution;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/***
 *
 */
//@component @Service @Dao @Controller


@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    //注入Service依赖
    //@Autowired / @Resource @Inject

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;
    //md5盐值字符串，用于混淆md5

    private final String slat = "faljfhal83&$Y*!(@(+l";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }


    public Exposer exportSeckillUrl(long seckillId) {

        //优化点：缓存优化通过redis缓存，降低数据库访问压力：超时的基础上维护一致性
        /**
         * get from cache
         * if null
         *  get db
         *  else
         *      putcache
         *   locgoin
         */
        //1: 访问redis,
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                redisDao.putSeckill(seckill);

                redisDao.putNumberById(seckillId,seckill.getNumber());
                redisDao.putRemainFlagById(seckillId,1);
            }
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        String md5 = getMD5(seckillId);

        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }


    /**
     * 使用注解控制事务方法的优点：
     * 1:开发团队达成一致约定：明确标注事务方法的编程风格。
     * 2：保证开发时间尽可能短，不要穿插其他网络操作
     * 3：不是所有方法都需要事务，如果只有一条修改操作，只读操作不需要事务控制
     */

    @Transactional
    //Spring声明式事务是通过异常来决定是commit还是rollback
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, SeckillCloseException, RepeatKillExecution {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }

        Date nowTime = new Date();




            try {
                int remainFlag = Integer.valueOf(redisDao.getNumberById(seckillId));
                if(remainFlag==0){
                    throw new SeckillCloseException("seckill closed");
                } else {
                    //执行秒杀逻辑： 减库存+记录购买行为
                    //简单优化 insert和update顺序减少mysql锁持有时间，降低了一倍的时间
                    //通过insert ignore，主键冲突返回0

                    int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
                    if (insertCount <= 0) {

                        throw new RepeatKillExecution("repeat kill");
                    } else {

                        //减库存，热点商品竞争（要拿到mysql行级锁，涉及行级锁竞争）
                        int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                        if (updateCount <= 0) {

                            throw new SeckillCloseException("seckill closed");
                        } else {
                            //秒杀成功 commit

                            SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                            return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                        }

//                    }
                    }

                }
            }
            catch (SeckillCloseException e1) {
                throw e1;
            } catch (RepeatKillExecution e2) {
                throw e2;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                //所有编译异常转换为运行期异常
                throw new SeckillException("seckill inner error:" + e.getMessage());
            }finally {
//                jedis.close();
            }



    }

    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStatEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        //通过seckillDao执行一个调用存储过程的逻辑

        Map<String, Object> map = new HashMap<String, Object>();
        //使用map是因为需要把result也放到存储过程中当参数；
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);

        //执行存储过程，result被赋值
        try {
            seckillDao.killByProcedure(map);
            //获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, sk);
            } else {
                //这里不用抛出异常了，因为不需要spring来管理事务了
                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
        }
    }
}
