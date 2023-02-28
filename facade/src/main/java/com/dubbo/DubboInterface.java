package com.dubbo;

import vo.Response;

/**
 * @author zcy
 * @date 2023/2/21
 * @description dubbo 接口
 */
@SuppressWarnings("all")
public interface DubboInterface {

    Response doSomething() throws InterruptedException;

}
