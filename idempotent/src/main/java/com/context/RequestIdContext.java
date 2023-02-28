package com.context;

import org.springframework.core.NamedThreadLocal;
import org.springframework.util.StringUtils;

/**
 * @author zcy
 * @date 2023/2/24
 * @description RequestId 上下文
 */
@SuppressWarnings("all")
public class RequestIdContext {

    public final static NamedThreadLocal<String> httpRequestId = new NamedThreadLocal<>("http-requestId");
    public final static NamedThreadLocal<String> dubboRequestId = new NamedThreadLocal<>("dubbo-requestId");

    public static void setHttpRequestId(String requestId) {
        if (StringUtils.isEmpty(requestId)) return;
        httpRequestId.set(requestId);
    }

    public static void clearHttpRequestId() {
        httpRequestId.remove();
    }

    public static String getHttpRequestId() {
        return httpRequestId.get();
    }

    public static void setDubboRequestId(String requestId) {
        if (StringUtils.isEmpty(requestId)) return;
        dubboRequestId.set(requestId);
    }

    public static void clearDubboRequestId() {
        dubboRequestId.remove();
    }

    public static String getDubboRequestId() {
        return dubboRequestId.get();
    }


}
