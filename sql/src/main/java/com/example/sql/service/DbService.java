package com.example.sql.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author zcy
 * @date 2023/2/21
 * @description 数据库服务类
 */
@Component
@SuppressWarnings("all")
public class DbService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL_NAME1 = "INSERT INTO `multi_thread_table` (`name`) values ('name1')";
    private static final String INSERT_SQL_NAME2 = "INSERT INTO `multi_thread_table` (`name`) values ('name2')";

    /**
     * 声明式事务
     * 主线程/异步线程分别执行一条DML语句
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void multiThreadTransaction() throws ExecutionException, InterruptedException {
        jdbcTemplate.execute(INSERT_SQL_NAME1);
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> jdbcTemplate.execute(INSERT_SQL_NAME2));
        future.get();
        // 抛出异常，让当前事务回滚，默认情况下，Spring-Transaction默认只还原Error子类以及Runtime类型的异常
        throw new RuntimeException();
    }

}
