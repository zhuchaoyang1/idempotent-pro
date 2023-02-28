package com.ann;

import com.config.Configure;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author zcy
 * @date 2023/2/25
 * @description 开启幂等服务
 */
@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(value = {Configure.class})
@ComponentScan(basePackages = {"com.*"})
public @interface EnableIdempotent {
}
