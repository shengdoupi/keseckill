package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件
@ContextConfiguration("classpath:spring/spring-dao.xml")

public class SuccessKilledDaoTest {
    @Resource
    private  SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() {

        /**
         * Preparing: insert ignore into success_killed(seckill_id,user_phone) values (?, ?)
         * Parameters: 1000(Long), 17321010571(Long)
         * Updates: 1
         */

        long id = 1001L;
        long phone = 17321010571L;
        int insertCount = successKilledDao.insertSuccessKilled(id, phone);
        System.out.println("insertCount = " + insertCount);
    }

    @Test
    public void queryByIdWithSeckill() {

        /**
         * SuccessKilled{seckillId=1001, userPhone=17321010571, state=0, createTime=null}
         * Seckill{seckillId=1001, name='500元秒杀ipad', number=200, startTime=Sun Nov 01 00:00:00 GMT+08:00 2015, endTime=Mon Nov 02 00:00:00 GMT+08:00 2015, createTime=Wed Nov 04 00:00:00 GMT+08:00 2015}
         */

        long id = 1001L;
        long phone = 17321010571L;
        SuccessKilled successKilled =  successKilledDao.queryByIdWithSeckill(id,phone);
        System.out.println(successKilled);
        System.out.println(successKilled.getSeckill());

    }
}