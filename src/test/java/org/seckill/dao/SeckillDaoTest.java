package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 配置spring和junit整合，junit启动时加载springIOC容器
 * spring-test,junit
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件
@ContextConfiguration("classpath:spring/spring-dao.xml")
public class SeckillDaoTest {

    //注入Dao实现类依赖
    @Resource
    private SeckillDao seckillDao;

    @Test
    public void queryById() {
        long id = 1000;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    @Test
    public void queryAll() {
        /**
         * Caused by: org.apache.ibatis.binding.BindingException: Parameter 'offset' not found. Available parameters are [0, 1, param1, param2]
         *
         */
//        List <Seckill> queryAll (int offset, int limit);
//    	java没有保存形参的记录，queryAll(int offset, int limit) ->queryAll(arg0, arg1);

        //在Dao接口方法型参数上加上@param("offset")注解,告诉MyBatis正确的形参是什么表述

//        Seckill{seckillId=1000, name='100元秒杀iphone', number=100, startTime=Sun Nov 01 00:00:00 GMT+08:00 2015, endTime=Mon Nov 02 00:00:00 GMT+08:00 2015, createTime=Wed Nov 04 00:00:00 GMT+08:00 2015}
//        Seckill{seckillId=1001, name='500元秒杀ipad', number=200, startTime=Sun Nov 01 00:00:00 GMT+08:00 2015, endTime=Mon Nov 02 00:00:00 GMT+08:00 2015, createTime=Wed Nov 04 00:00:00 GMT+08:00 2015}
//        Seckill{seckillId=1002, name='200元秒杀小米', number=300, startTime=Sun Nov 01 00:00:00 GMT+08:00 2015, endTime=Mon Nov 02 00:00:00 GMT+08:00 2015, createTime=Wed Nov 04 00:00:00 GMT+08:00 2015}
//        Seckill{seckillId=1003, name='300元秒杀huawei', number=400, startTime=Sun Nov 01 00:00:00 GMT+08:00 2015, endTime=Mon Nov 02 00:00:00 GMT+08:00 2015, createTime=Wed Nov 04 00:00:00 GMT+08:00 2015}

        List<Seckill> seckills = seckillDao.queryAll(0,100);
        for (Seckill seckill :seckills){
            System.out.println(seckill);
        }
    }

    @Test
    public void reduceNumber() {


        /**
         * update seckill set number = number-1 where seckill_id = ? and start_time <= ? and end_time >= ? and number > 0
         * Parameters: 1000(Long), 2022-03-07 14:56:31.347(Timestamp), 2022-03-07 14:56:31.347(Timestamp)
         *  Updates: 0
         */

        Date killTime  = new Date();
        int updateCount = seckillDao.reduceNumber(1000L,killTime);
        System.out.println("updateCount" + updateCount);
    }


}