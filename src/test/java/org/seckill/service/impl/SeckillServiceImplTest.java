package org.seckill.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillExecution;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)

//代表Spring容器启动的时候要加载哪些配置
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})

public class SeckillServiceImplTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> list = seckillService.getSeckillList();

        //{}是占位符
        //Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@66746f57]

        logger.info("list{}", list);
    }

    @Test
    public void getById() {
        long id = 1000;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}",seckill);

    }


    //集成测试代码完整逻辑，注意可重复执行
    @Test
    public  void testSeckillLogic() {
        long id = 1000;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if(exposer.isExposed()){
            logger.info("exposer={}", exposer);

            long phone  = 17321010672L;
            String md5 = exposer.getMd5();

            try{
                SeckillExecution execution = seckillService.executeSeckill(id,phone,md5);
                logger.info("result={}", execution);
            } catch (RepeatKillExecution e){
                logger.error(e.getMessage());
            } catch (SeckillCloseException e){
                logger.error(e.getMessage());
            }

        }else{
            //秒杀未开启
            logger.warn("exposer = {}",exposer);
        }

    }

    @Test
    public void executeSeckillProcedure(){
        long seckillId = 1000;
        long phone = 17521010552l;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExecution seckillExecution = seckillService.executeSeckillProcedure(seckillId,phone,md5);
            logger.info(seckillExecution.getStateInfo());
        }
    }


    @Test
    public void exportSeckillUrl() {

        long id = 1000;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        logger.info("exposer={}", exposer);

        //输出：
        //exposer=Exposer{exposed=false, md5='null', seckillId=1000, now=1646667173397, start=1446307200000, end=1446393600000}
        //修改时间后exposer=Exposer{exposed=true, md5='9ad332565497be150a6e3d38148d5d42', seckillId=1000, now=0, start=0, end=0}
    }

    @Test
    public void executeSeckill() {

        long id = 1000;
        long phone  = 17321010672L;
        String md5 = "9ad332565497be150a6e3d38148d5d42";

        try{
            SeckillExecution execution = seckillService.executeSeckill(id,phone,md5);
            logger.info("result={}", execution);
        } catch (RepeatKillExecution e){
            logger.error(e.getMessage());
        } catch (SeckillCloseException e){
            logger.error(e.getMessage());
        }




        //输出
//        DEBUG org.mybatis.spring.SqlSessionUtils - Creating a new SqlSession
//        23:43:17.274 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Registering transaction synchronization for SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@4b2a01d4]
//        23:43:17.280 [main] DEBUG o.m.s.t.SpringManagedTransaction - JDBC Connection [com.mchange.v2.c3p0.impl.NewProxyConnection@180da663] will be managed by Spring
//        23:43:17.283 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - ==>  Preparing: update seckill set number = number-1 where seckill_id = ? and start_time <= ? and end_time >= ? and number > 0
//        23:43:17.321 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - ==> Parameters: 1000(Long), 2022-03-07 23:43:17.265(Timestamp), 2022-03-07 23:43:17.265(Timestamp)
//                23:43:17.322 [main] DEBUG o.s.dao.SeckillDao.reduceNumber - <==    Updates: 1
//        23:43:17.322 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Releasing transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@4b2a01d4]
//        23:43:17.322 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Fetched SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@4b2a01d4] from current transaction
//        23:43:17.323 [main] DEBUG o.s.d.S.insertSuccessKilled - ==>  Preparing: insert ignore into success_killed(seckill_id,user_phone,state) values (?, ?,0)
//        23:43:17.323 [main] DEBUG o.s.d.S.insertSuccessKilled - ==> Parameters: 1000(Long), 17321010671(Long)
//                23:43:17.324 [main] DEBUG o.s.d.S.insertSuccessKilled - <==    Updates: 1
//        23:43:17.340 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Releasing transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@4b2a01d4]
//        23:43:17.340 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Fetched SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@4b2a01d4] from current transaction
//        23:43:17.342 [main] DEBUG o.s.d.S.queryByIdWithSeckill - ==>  Preparing: select sk.seckill_id, sk.user_phone, sk.create_time, sk.state, s.seckill_id "seckill.seckill_id", s.name "seckill.name", s.number "seckill.number", s.start_time "seckill.start_time", s.end_time "seckill.end_time", s.create_time "seckill.create_time" from success_killed sk inner join seckill s on sk.seckill_id = s.seckill_id where sk.seckill_id = ? and sk.user_phone = ?
//        23:43:17.342 [main] DEBUG o.s.d.S.queryByIdWithSeckill - ==> Parameters: 1000(Long), 17321010671(Long)
//                23:43:17.355 [main] DEBUG o.s.d.S.queryByIdWithSeckill - <==      Total: 1
//        23:43:17.360 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Releasing transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@4b2a01d4]
//        23:43:17.366 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Transaction synchronization committing SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@4b2a01d4]
//        23:43:17.366 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Transaction synchronization deregistering SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@4b2a01d4]
//        23:43:17.366 [main] DEBUG org.mybatis.spring.SqlSessionUtils - Transaction synchronization closing SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@4b2a01d4]
//result=SeckillExecution{seckillId=1000, state=1, stateInfo='秒杀成功', successKilled=SuccessKilled{seckillId=1000, userPhone=17321010672, state=0, createTime=null}}


    }
}