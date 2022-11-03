package org.seckill.service;


import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillExecution;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

import java.util.List;

/**
 * 设计接口要站在使用者的角度
 * 三个方面：方法粒度，参数，返回类型（return 类型要友好/异常）
 */

public interface SeckillService {

    /**
     * 查询所有秒杀记录
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     */
    Seckill getById(long seckillId);

    /**
     *  * 秒杀开启时输出秒杀接口地址，否则输出系统时间和秒杀时间
     * 否则输出系统时间和秒杀时间
     * @param seckillId
     * @return
     */
    Exposer exportSeckillUrl (long seckillId);

    /**
     * 执行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */

    SeckillExecution executeSeckill (long seckillId, long userPhone, String md5)
        throws SeckillException, SeckillCloseException, RepeatKillExecution;
    /**
     * 执行秒杀操作by 存储过程
     */

    SeckillExecution executeSeckillProcedure (long seckillId, long userPhone, String md5);

}
