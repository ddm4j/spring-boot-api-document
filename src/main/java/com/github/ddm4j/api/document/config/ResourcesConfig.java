package com.github.ddm4j.api.document.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourcesConfig implements WebMvcConfigurer {
	
	// 需要告知系统，这是要被当成静态文件的！
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// System.out.println("初始化-resourcesConfig");
		// 第一个方法设置访问路径前缀，第二个方法设置资源路径
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
		registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/");
	}
}
