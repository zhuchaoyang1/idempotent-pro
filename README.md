## 背景：

我们项目组有一个 Dubbo 提供方对外暴露服务。最近在数据库中发现了单号重复的现象，即数据重复。我们排查了上游与本系统的日志，发现本系统在同一时间确实有多条日志，而上游系统只有一次。你可能会想到这是上游的重试机制导致的。 重试机制也是一把双刃剑。它可以尽可能保证业务的健壮性，避免因网络问题对业务造成冲击，但也有可能导致数据重复。如下图所示：

![重试机制.png](https://upload-images.jianshu.io/upload_images/23929363-78f5e0785b0c055f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


ServerOne向ServerTwo发起请求，每次请求的超时时间设置为2秒，并进行了3次重试。然而，ServerTwo的业务处理需要5秒。因此，在ServerOne进行的每次重试中，都会引发TimeOut异常。

上述问题不同于网络错误的异常。每一次重试都会导致实际流量传输到ServerTwo服务中，从而产生了非幂等的情况。

在上述情况中，ServerTwo的方法可能添加了业务幂等性校验，如下所示：

**第一步：**查询数据库，查看是否存在重复行数据；

**第二步：**根据第一步的结果，如果数据重复，则放弃执行后续代码，否则执行请求。

您认为这样的处理方法会出现问题吗？实际上，这种处理方法仍然存在一定的重复调用风险。例如，第一次请求时，数据库事务可能尚未提交。

了解到重试机制可能会导致数据重复问题，并增加下游服务的压力和放大数据链路，详情请见以下博客：

[如何优雅地重试](https://mp.weixin.qq.com/s/6IkTnUbBlHjM3GM_bT35tA)

### 在理解了上述背景之后，我们着手于制造出一种通用工具，尽可能避免因框架层重试导致的数据重复。

工具Github地址：

[](https://github.com/zhuchaoyang1/idempotent-pro)[https://github.com/zhuchaoyang1/idempotent-pro](https://github.com/zhuchaoyang1/idempotent-pro)

工具考虑了 `HTTP`、`Dubbo`、`MQ` 三种可能会出现幂等性问题的中间件。

工具的主要思路在于利用注解与动态代理技术实现幂等性校验。

项目的主要思路为：

1.  创建 `IdempotentService`、`IdempotentMethod` 注解，在需要保证幂等性的类上标注 `IdempotentService` 注解，在类中需要保证幂等性的方法中标注 `IdempotentMethod` 注解
2.  Spring 启动时，利用 Spring 提供的扩展口之一 `BeanPostProcessor` 对 IOC 中所有的 Bean 进行扫描（主要是标注了注解），对此类 Bean 利用 `ProxyFactory` 进行生成代理，并放入 IOC 容器中
3.  在代理中主要获取流量中的 `RequestId` 参数，并利用 Redis 的 `set ex nx` 进行幂等性校验，若校验不通过则代理类中不会再调用被代理方法，而是直接返回异常，若幂等性服务校验通过则调用被代理方法，执行业务逻辑。

获取 `RequestId` 的伪代码：

**Http：**

```
// 要求客户端在每次请求时生成请求唯一ID并设置进Header中
request.getHeader(”request-id”);
```

**Dubbo：**

```
// 这要求Dubbo客户端传递隐式参数，这样做的好处在于避免修改业务参数
// 本案例中封装的幂等性工具通过Dubbo-Filter完成隐式参数赋值，业务无感知
RpcContext.getContext.getAttachment(”request-id”);
```

**MQ：**

```
消息唯一ID + 重试次数（若有）；
```

![幂等性校验机制.png](https://upload-images.jianshu.io/upload_images/23929363-5c0e0dbdbd46954f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

上图展示了整个幂等性校验工具的思路，同时也涉及到以下知识点，读者需特别注意：

1.  为什么要使用`ProxyFactory`动态代理，而不能使用`JDK`或`Cglib`代理
2.  Dubbo Filter的自动激活方案
3.  如何在不影响Http或Dubbo请求的情况下完成无入侵式操作

这些问题都可以在源码上找到答案，希望读者认真阅读。
