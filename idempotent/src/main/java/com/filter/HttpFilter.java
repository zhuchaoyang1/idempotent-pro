package com.filter;

import constant.GlobalConstant;
import com.context.RequestIdContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author zcy
 * @date 2023/2/24
 * @description 接口过滤器
 */
@Slf4j
public class HttpFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        StringBuffer requestURL = httpServletRequest.getRequestURL();
        String requestId = httpServletRequest.getHeader(GlobalConstant.REQUEST_ID);
        if (!StringUtils.isEmpty(requestId)) {
            log.info("请求地址:{}, requestId:{}", requestURL, requestId);
            RequestIdContext.setHttpRequestId(requestId);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            // must clear, tomcat has thread pool.
            RequestIdContext.clearHttpRequestId();
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

}
