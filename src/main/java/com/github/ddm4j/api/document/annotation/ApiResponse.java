package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 只能用在 @ApiResponses 注解中，用于描述 返回值 字段
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ApiResponses.class)
public @interface ApiResponse {
	/**
	 * 字段，多层请用点， 如 a.b
	 * 
	 * @return 字段
	 */
	public String field();

	/**
	 * 是否一定会返回，默认 否
	 * 
	 * @return false
	 */
	public boolean required() default false;

	/**
	 * 示例说明，会覆盖 ApiField 的说明
	 * 
	 * @return 描述
	 */
	public String describe() default "";
}
