package com.example.sql;

import com.example.sql.service.DbService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@SpringBootTest
class SqlApplicationTests {

    @Resource
    private DbService dbService;

    @Test
    void contextLoads() throws ExecutionException, InterruptedException {
        dbService.multiThreadTransaction();
    }

}
