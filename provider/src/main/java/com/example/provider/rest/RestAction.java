package com.example.provider.rest;

import com.ann.IdempotentMethod;
import com.ann.IdempotentService;
import com.example.provider.service.rest.RestInterfaceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.Response;

import javax.annotation.Resource;

/**
 * @author zcy
 * @date 2023/2/24
 * @description REST
 */
@RestController
@IdempotentService
@RequestMapping("rest")
public class RestAction {

    @Resource
    private RestInterfaceImpl restInterfaceImpl;

    @GetMapping("read")
    public Response read() {
        return Response.ok("i am data.");
    }

    @PutMapping("write")
    @IdempotentMethod(ttl = 50, businessType = IdempotentMethod.BusinessType.HTTP)
    public Response write() {
        return Response.ok(restInterfaceImpl.writeWithTransactionNotRollBack());
    }

}
