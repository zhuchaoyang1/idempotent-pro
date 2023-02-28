package com.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.context.RequestIdContext;
import constant.GlobalConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 使用@Activate自动激活Filter
 * 避免使用者仍需要在yml中或者@Reference中指定生效的Filter
 *
 * @author zcy
 * @date 2023/2/23
 * @description Dubbo Filter
 * @see https://cn.dubbo.apache.org/zh-cn/docs3-v2/java-sdk/reference-manual/spi/description/filter/
 * @see https://cn.dubbo.apache.org/zh-cn/docs3-v2/java-sdk/concepts-and-architecture/service-invocation/#filter%E6%8B%A6%E6%88%AA%E5%99%A8
 */
@SuppressWarnings("all")
@Slf4j
@Activate(group = {Constants.CONSUMER, Constants.PROVIDER})
public class DubboFilter implements Filter {

    /**
     * 如果需要每次传递的attachments都是最新值，则需要在invoke之前手动清除当前线程中已有的参数
     * invocation.getAttachments().clear();
     * {@link com.alibaba.dubbo.rpc.RpcInvocation#addAttachmentsIfAbsent(java.util.Map)}
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result;
        String requestId = null;
        String url = invoker.getUrl().toString();
        String methodName = invocation.getMethodName();
        String dubboSide = invoker.getUrl().getParameter(Constants.SIDE_KEY);

        try {
            switch (dubboSide) {
                case Constants.CONSUMER: {
                    requestId = generateClientRequestId();
                    result = clientFilter(invoker, invocation, requestId);
                    break;
                }
                case Constants.PROVIDER: {
                    requestId = getRequestIdFromContext();
                    result = providerFilter(invoker, invocation, requestId);
                    break;
                }
                default: {
                    log.error("Dubbo-幂等性过滤器仍定当前角色失败,Url:{},Side:{}", url, dubboSide);
                    result = invoker.invoke(invocation);
                }
            }
        } finally {
            log.info("Dubbo-幂等性Url:{},MethodName:{},RequestId:{},Side:{}", url, methodName, requestId, dubboSide);
        }

        return result;
    }

    private String generateClientRequestId() {
        return new StringBuffer().append(UUID.randomUUID()).append(System.currentTimeMillis()).toString();
    }

    private String getRequestIdFromContext() {
        return RpcContext.getContext().getAttachment(GlobalConstant.REQUEST_ID);
    }

    private Result clientFilter(Invoker<?> invoker, Invocation invocation, String requestId) throws RpcException {
        RpcContext.getContext().setAttachment(GlobalConstant.REQUEST_ID, requestId);
        return invoker.invoke(invocation);
    }

    private Result providerFilter(Invoker<?> invoker, Invocation invocation, String requestId) throws RpcException {
        if (!StringUtils.isEmpty(requestId)) {
            RequestIdContext.setDubboRequestId(requestId);
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            // must clear! dubbo有线程池
            RequestIdContext.clearDubboRequestId();
        }
    }

}
