package com.DB.DBmarket.config;

import com.DB.DBmarket.pojo.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.nio.file.Paths;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    //拦截器
    @Resource
    private LoginInterceptor loginInterceptor;
    @Resource
    private StorageProperties storageProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns(
                "/user/login",
                "/user/register",
                "/alipay/notify",
                "/alipay/return",
                "/storage/**"
        );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Token")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseDir = Paths.get(storageProperties.getLocal().getBaseDir())
                .toAbsolutePath()
                .normalize()
                .toString()
                .replace("\\", "/");
        registry.addResourceHandler("/storage/**")
                .addResourceLocations("file:" + baseDir + "/");
    }
}
