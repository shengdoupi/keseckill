package org.seckill.exception;


/**
 * 重复秒杀异常（运行期异常)
 */
public class RepeatKillExecution extends  SeckillException{

    public RepeatKillExecution(String message) {
        super(message);
    }

    public RepeatKillExecution(String message, Throwable cause) {
        super(message, cause);
    }
}
