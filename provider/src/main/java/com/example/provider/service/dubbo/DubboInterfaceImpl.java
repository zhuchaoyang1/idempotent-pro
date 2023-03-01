package com.example.provider.service.dubbo;

import com.alibaba.dubbo.config.annotation.Service;
import com.ann.IdempotentMethod;
import com.ann.IdempotentService;
import com.dubbo.DubboInterface;
import lombok.extern.slf4j.Slf4j;
import vo.Response;

import java.util.concurrent.TimeUnit;

/**
 * @author zcy
 * @date 2023/2/21
 * @description 实现类
 */
@SuppressWarnings("all")
@Slf4j
@Service
@IdempotentService
public class DubboInterfaceImpl implements DubboInterface {

    /**
     * 服务提供方要注意幂等性
     *
     * @return
     * @throws InterruptedException
     */
    @Override
    @IdempotentMethod(ttl = 10, businessType = IdempotentMethod.BusinessType.DUBBO)
    public Response doSomething() throws InterruptedException {
        // 模拟接口耗时
        TimeUnit.SECONDS.sleep(5);
        return Response.ok("业务逻辑处理成功");
    }

}

