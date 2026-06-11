package com.DB.DBmarket.pojo.interceptor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*
        //获取请求的URL
        String url = request.getRequestURL().toString();
        log.info("请求的url：{}",url);

        //获取token
        String token = request.getHeader("token");
        //放行所有的OPTIONS试探请求
        if("OPTIONS".equals(request.getMethod().toUpperCase())) {
            System.out.println("Method:OPTIONS");
            return true;
        }
        if (StringUtils.isEmpty(token)) {
            log.info("请求头为空，返回未登录信息");
            Result noAuthorization=Result.noAuthorization();
            //手动转换对象-->JSON
            String notLogin= JSONUtil.toJsonStr(noAuthorization);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(notLogin);
            return false;
        }
        try {
            JwtUtils.parseJWT(token);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("解析令牌失败，返回错误信息");
            Result noAuthorization=Result.noAuthorization();
            //手动转换对象-->JSON
            String notLogin= JSONUtil.toJsonStr(noAuthorization);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(notLogin);
        }
       log.info("令牌合法，放行");
       */
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
