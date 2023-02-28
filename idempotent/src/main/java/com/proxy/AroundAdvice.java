package com.proxy;

import com.ann.IdempotentMethod;
import com.context.RequestIdContext;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.StringUtils;
import vo.Response;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author zcy
 * @date 2023/2/23
 * @description 使用CGlib代理
 */
@SuppressWarnings("all")
@Slf4j
public class AroundAdvice implements MethodInterceptor {

    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 流量唯一键前缀
     */
    private final String REDIS_IDEMPOTENT_PREFIX = "REDIS_IDEMPOTENT_PREFIX";

    public AroundAdvice(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        // 响应信息
        Object result;
        // 请求唯一标识符
        String trafficKey;

        Method agentMethod = methodInvocation.getMethod();
        Object[] arguments = methodInvocation.getArguments();

        // Object类方法
        if (Object.class.equals(agentMethod.getDeclaringClass())) {
            return methodInvocation.proceed();
        }

        // 解析 IdempotentMethod
        IdempotentMethod idempotentMethod = agentMethod.getAnnotation(IdempotentMethod.class);

        // 非需要幂等代理方法
        if (Objects.isNull(idempotentMethod)) {
            return methodInvocation.proceed();
        }

        IdempotentMethod.BusinessType businessType = idempotentMethod.businessType();
        trafficKey = getTrafficKey(agentMethod, arguments, businessType);

        // 兼容历史流量 若trafficKey为空 则不进行代理
        if (StringUtils.isEmpty(trafficKey)) {
            return methodInvocation.proceed();
        }

        // 需要代理 并且 有请求标识
        // 结合Redis做幂等校验
        boolean nonRepeatFlag = true;
        try {
            nonRepeatFlag = setExNx(trafficKey, idempotentMethod.ttl());
        } catch (Exception e) {
            log.error("幂等性Redis判断失败,请求唯一Key:{},失败原因:{}", trafficKey, e.toString());
        }

        if (!nonRepeatFlag) {
            // 幂等校验不通过，返回幂等异常
            return getErrorResponse(businessType);
        }

        result = methodInvocation.proceed();

        return result;
    }

    private String getTrafficKey(Method agentMethod, Object[] args, IdempotentMethod.BusinessType businessType) {
        String trafficKey = null;
        switch (businessType) {
            case MQ: {
                trafficKey = getMqKey(agentMethod, args);
                break;
            }
            case HTTP: {
                trafficKey = getHttpKey();
                break;
            }
            case DUBBO: {
                trafficKey = getDubboKey();
            }
        }
        return trafficKey;
    }

    /**
     * 获取MQ Key
     * msgId + 重试次数
     *
     * @param agentMethod
     * @return
     */
    private String getMqKey(Method agentMethod, Object[] args) {
        return null;
    }

    /**
     * 获取Http请求头Key
     *
     * @return
     */
    private String getHttpKey() {
        return String.format("%s:%s", REDIS_IDEMPOTENT_PREFIX, RequestIdContext.getHttpRequestId());
    }

    /**
     * 获取dubbo key
     *
     * @return
     */
    private String getDubboKey() {
        return String.format("%s:%s", REDIS_IDEMPOTENT_PREFIX, RequestIdContext.getDubboRequestId());
    }

    private boolean setExNx(String key, int expireTimeSeconds) {
        DefaultRedisScript redisScript = new DefaultRedisScript();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/STR_EX_NX.lua")));
        redisScript.setResultType(Boolean.class);
        return (boolean) redisTemplate.execute(redisScript, Arrays.asList(key), "", expireTimeSeconds);
    }

    private Object getErrorResponse(IdempotentMethod.BusinessType businessType) {
        Object result = null;
        switch (businessType) {
            case MQ: {
                // result = ZMSResult.status(MsgConsumedStatus.SUCCEED);
                break;
            }
            case HTTP:
            case DUBBO: {
                result = Response.repeat("请求重复调用");
                break;
            }
        }
        return result;
    }

}
