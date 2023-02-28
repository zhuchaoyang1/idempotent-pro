package com.scanner;

import com.ann.IdempotentMethod;
import com.ann.IdempotentService;
import com.google.common.collect.Lists;
import com.proxy.IdempotentProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.util.ProxyUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * @author zcy
 * @date 2023/2/22
 * @description 扫描类
 */
@Slf4j
public class IdempotentScanner implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    /**
     * 幂等代理成功的类名称
     */
    private List<String> idempotentSucceedClassNames = Lists.newArrayList();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Object result = bean;
        // 重要：获取原始对象类，避免bean已经经过CGlib代理 导致注解缺失
        Class<?> beanClass = ProxyUtils.getUserClass(bean);

        // 1. 标注幂等服务
        if (!beanClass.isAnnotationPresent(IdempotentService.class)) {
            return result;
        }

        // 2. 具备幂等方法 方法不能被private、static、final修饰 否则代理无效
        // 注：要获取原始对象class进行判断，有可能bean已经是代理对象，导致注解缺失
        Method[] declaredMethods = beanClass.getDeclaredMethods();
        long idempotentMethodCount = Arrays.stream(declaredMethods).filter(method -> methodHasIdempotentMethodAnn(method) && methodIsRightSignature(method)).count();
        if (idempotentMethodCount == 0) {
            return result;
        }

        // 3. 生成服务代理
        try {
            result = IdempotentProxyFactory.getProxyInstance(redisTemplate, bean);
            idempotentSucceedClassNames.add(beanClass.getName());
        } catch (Exception e) {
            log.error("幂等性BeanClass:{},生成代理失败:{},无法保证幂等", beanClass.getName(), e.toString());
        }

        return result;
    }

    public boolean methodHasIdempotentMethodAnn(Method method) {
        return method.isAnnotationPresent(IdempotentMethod.class);
    }

    public boolean methodIsRightSignature(Method method) {
        int modifiers = method.getModifiers();
        return !Modifier.isFinal(modifiers)
                && !Modifier.isStatic(modifiers)
                && !Modifier.isPrivate(modifiers);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("生成幂等性代理:{}", idempotentSucceedClassNames);
        idempotentSucceedClassNames.clear();
    }

}