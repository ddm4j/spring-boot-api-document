package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 返回值描述
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiResponses {
	/**
	 * 返回值列表
	 * 
	 * @return 数据
	 */
	ApiResponse[] value() default {};
}
