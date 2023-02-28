package com.example.consumer;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.dubbo.DubboInterface;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import vo.Response;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("all")
@Slf4j
@EnableDubbo
@SpringBootTest
class ConsumerApplicationTests {

    @Reference(timeout = 3_000, cluster = "failover", retries = 1)
    private DubboInterface dubboInterface;

    @Test
    public void contextLoads() throws InterruptedException {
        Response response;
        try {
            response = dubboInterface.doSomething();
            log.info("调用成功:{}", response.toString());
            if ("-1".equals(response.getCode())) {
                // 建议数据落表由定时任务触发，保证 at-least-once
            }
        } catch (Exception e) {
            log.error("调用失败,原因:{}", e.getMessage());
        }

        TimeUnit.SECONDS.sleep(100);
    }

}
