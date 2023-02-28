package com.example.provider.exception.advice;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vo.Response;

/**
 * @author zcy
 * @date 2023/2/26
 * @description Rest接口异常统一处理
 */
@RestControllerAdvice
public class RestExceptionAdvice {

    @ExceptionHandler(value = RuntimeException.class)
    public Response handleRuntimeException(RuntimeException runtimeException) {
        return Response.error("请联系管理员");
    }

    @ExceptionHandler(value = Exception.class)
    public Response handleException(Exception exception) {
        return Response.error("内部错误");
    }

}
