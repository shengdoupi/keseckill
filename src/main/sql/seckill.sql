-- 秒杀执行的存储过程 --
DELIMITER $$ -- console ;转换为$$
-- 定义存储过程
CREATE PROCEDURE `seckill`.`execute_seckill`
-- 参数： in 表示定义输入参数，out表示输出参数
-- row_count()函数用来返回上一条修改类型sql的影响行数（insert，delete，update）
-- row_count()的结果 ： 如果是0，表示未修改数据，如果大于0，表示修改的行数，如果小于0，表示sql错误（未执行修改sql）
    (in v_seckill_id bigint, in v_phone bigint,
        in v_kill_time timestamp, out r_result int)
    BEGIN
        DECLARE insert_count int DEFAULT 0;
        START TRANSACTION;
        insert ignore into success_killed
            (seckill_id,user_phone,create_time)
            values ( v_seckill_id, v_phone, v_kill_time);
        select row_count() into insert_count;
        IF (insert_count = 0) THEN
            ROLLBACK;
            set r_result = -1;
        ELSEIF (insert_count < 0) THEN
            ROLLBACK;
            set r_result = -2;
        ELSE
            update seckill set number = number - 1
                where seckill_id = v_seckill_id
                and end_time > v_kill_time
                and start_time < v_kill_time
                and number > 0;
            select row_count() into insert_count;
            IF (insert_count = 0) THEN
                ROLLBACK;
                set r_result = 0;
 -- sql出错或等待行级锁超时 --
            ELSEIF (insert_count < 0) THEN
                ROLLBACK;
                set r_result = -2;
            ELSE
                COMMIT;
                set r_result = 1;
            END IF;
        END IF;
    END;

$$
-- 存储过程定义结束

DELIMITER ;
-- console中定义变量
set @r_result = -3;
-- console中执行存储过程
call execute_seckill(1003, 17521010671, now(),@r_result);
-- 获取结果
select @r_result;

-- 存储过程：希望通过存储过程把两条sql放在一起，不要通过java client来控制事务，rollback和commit
-- 1、存储过程优化：事务行级锁所持有的时间
-- 2、不要过渡依赖存储逻辑
-- 3、秒杀项目里，简单的逻辑可以用存储过程
-- 好处是减少了行级锁持有时间，因为这些sql在mysql服务器本地执行效率非常高
-- 4、QPS ：一个秒杀商品（单）6000/qps