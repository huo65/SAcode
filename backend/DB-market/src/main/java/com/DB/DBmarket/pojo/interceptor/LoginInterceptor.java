package com.DB.DBmarket.pojo.interceptor;


import cn.hutool.json.JSONUtil;
import com.DB.DBmarket.pojo.Result;
import com.DB.DBmarket.pojo.utils.CurrentUser;
import com.DB.DBmarket.pojo.utils.CurrentUserHolder;
import com.DB.DBmarket.pojo.utils.JwtUtils;
import io.jsonwebtoken.Claims;
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
        String url = request.getRequestURL().toString();
        log.info("请求的url：{}",url);

        if("OPTIONS".equals(request.getMethod().toUpperCase())) {
            return true;
        }

        String token = request.getHeader("Token");
        if (token == null || token.trim().isEmpty()) token = request.getHeader("token");
        if (token == null || token.trim().isEmpty()) {
            if (isPublicRead(request)) return true;
            return writeUnauthorized(response);
        }

        try {
            Claims claims = JwtUtils.parseJWT(token);
            String id = String.valueOf(claims.get("id"));
            String name = String.valueOf(claims.get("name"));
            String type = String.valueOf(claims.get("type"));
            CurrentUserHolder.set(new CurrentUser(id, name, type));
        } catch (Exception e) {
            log.info("解析令牌失败，返回错误信息");
            return writeUnauthorized(response);
        }
        return true;
    }

    private boolean isPublicRead(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase();
        String path = request.getRequestURI();
        return "GET".equals(method) && (
                "/category".equals(path) ||
                "/product/search".equals(path) ||
                "/product/info".equals(path) ||
                "/restaurant/list".equals(path) ||
                "/restaurant/info".equals(path)
        );
    }

    private boolean writeUnauthorized(HttpServletResponse response) throws Exception {
        Result noAuthorization = Result.noAuthorization();
        String notLogin = JSONUtil.toJsonStr(noAuthorization);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(notLogin);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        CurrentUserHolder.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
