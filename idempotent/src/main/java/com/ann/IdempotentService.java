package com.ann;

import java.lang.annotation.*;

/**
 * 需要注意的是：
 * 此方式只能保证因上游业务框架层面的短时间内重试而引发的幂等性问题，
 * (譬如：Dubbo重试、MQ重试短时间内的重试，但第一次请求中由于业务较为耗时，数据库事务还未提交，导致了非幂等的情况)
 * <p>
 * 业务内部强烈继续要通过业务ID去保证幂等性，去避免业务上的重试，如：生产者重复发送、用户界面重复点击
 *
 * @author zcy
 * @date 2023/2/22
 * @description 需要保证幂等服务
 */
@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IdempotentService {

}