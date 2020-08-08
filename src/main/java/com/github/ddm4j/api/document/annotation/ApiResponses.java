package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ApiResponse 注解组，不推荐使用，
 * 请直接使用 ApiResponse 多重注解方式，
 * 即直接在在方法写多个 ApiResponse 注解。
 * 不支持map类型字段。
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
