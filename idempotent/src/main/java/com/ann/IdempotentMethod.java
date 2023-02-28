package com.ann;

import java.lang.annotation.*;

/**
 * @author zcy
 * @date 2023/2/22
 * @description 需要保证幂等的方法
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotentMethod {

    /**
     * Key的过期时间
     * 单位：s
     *
     * @return
     */
    int ttl();

    /**
     * 业务类型
     *
     * @return
     */
    BusinessType businessType();

    enum BusinessType {
        MQ,
        HTTP,
        DUBBO
    }

}