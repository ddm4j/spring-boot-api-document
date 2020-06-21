package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.web.bind.annotation.Mapping;

/**
 * 描述接口请求参数，及校验，建议所有参数使用 bean 包装，建议使用 JSON 交互(请求参数前加 @RequestBody),不支持 Map
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Mapping
public @interface ApiParams {
	/**
	 * 请求参数
	 * 
	 * @return 参数集合
	 */
	ApiParam[] value();
}
