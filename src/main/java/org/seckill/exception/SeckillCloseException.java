package org.seckill.exception;

/**
 * 秒杀关闭异常（时间到，库存没了），运行时异常
 */
public class SeckillCloseException extends SeckillException{

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
