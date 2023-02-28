package com.example.provider.service.rest;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author zcy
 * @date 2023/2/24
 * @description Rest业务类
 */
@SuppressWarnings("all")
@Slf4j
@Component
public class RestInterfaceImpl {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String OK = "OK";
    private static final String INSERT_SQL = "INSERT INTO `multi_thread_table` (`name`) VALUE ('i am fine')";

    /**
     * 模拟普通业务耗时
     *
     * @return
     * @throws InterruptedException
     */
    public String write() throws InterruptedException {
        // 模拟业务耗时
        TimeUnit.SECONDS.sleep(5);
        log.info("数据写入完毕");
        return OK;
    }

    /**
     * 无异常事务正常提交
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String writeWithTransactionNotRollBack() {
        jdbcTemplate.execute(INSERT_SQL);
        return OK;
    }

    /**
     * 有异常事务回滚
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String writeWithTransactionRollBack() {
        jdbcTemplate.execute(INSERT_SQL);
        throw new RuntimeException("");
    }

    /**
     * 异步方法
     *
     * @param mainThreadId
     * @return
     */
    @Async
    public String writeAsync(long mainThreadId) {
        long id = Thread.currentThread().getId();
        Assert.notEquals(mainThreadId, id);
        return OK;
    }

    @Override
    public String toString() {
        return "";
    }
}

