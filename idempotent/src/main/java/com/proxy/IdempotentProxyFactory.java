package com.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author zcy
 * @date 2023/2/23
 * @description 创建代理对象 Cglib方式
 */
@Slf4j
public class IdempotentProxyFactory {

    /**
     * 获取代理实例
     * 不直接使用JDK代理的原因：所代理的Bean没有接口时，则JDK无法进行代理
     * 不直接使用Cglib代理的原因：会造成IOC注入的属性丢失，除非手动复制被代理类的属性至代理类中，相对较为麻烦（考虑到原始的bean已经经过了多层代理 @Async、@Transaction、@Schedule等）
     * <p>
     * JDK Proxy 注入属性不丢失的原因：
     * 调用代理类的方法时，内部会通过super.invocation去调用invocation实现类，而实现类中会调用被代理类中的方法，而这个被代理类往往就是IOC中的类，具备一切属性
     * <p>
     * Cglib 注入属性丢失的原因：
     * 调用代理类的方法时，内部会调用MethodInterceptor实现类的intercept方法，并且使用this(代理类对象)为方法的obj赋值，而this对象并没有纳入IOC容器中，不具备一切注入的属性
     * <p>
     * https://blog.csdn.net/m0_37550986/article/details/119585988
     *
     * @param bean
     * @return
     */
    public static Object getProxyInstance(RedisTemplate redisTemplate, Object bean) {
//        Object o = Proxy.newProxyInstance(IdempotentProxyFactory.class.getClassLoader(), ProxyUtils.getUserClass(bean).getInterfaces(), new InvocationHandler() {
//            @Override
//            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                if (Object.class.equals(method.getDeclaringClass())) {
//                    return method.invoke(bean, args);
//                }
//
//                return method.invoke(bean, args);
//            }
//        });
//
//        return o;

//        Enhancer enhancer = new Enhancer();
//        enhancer.setSuperclass(ProxyUtils.getUserClass(bean));
//        enhancer.setCallback(new MethodInterceptor() {
//            @Override
//            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
//                return methodProxy.invokeSuper(o, objects);
//            }
//        });
//        return enhancer.create();

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(bean);
        proxyFactory.setProxyTargetClass(true);

        proxyFactory.addAdvice((new AroundAdvice(redisTemplate)));

        return proxyFactory.getProxy();
    }

}
