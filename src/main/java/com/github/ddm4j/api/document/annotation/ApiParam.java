package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 放在 @ApiParams中,用于描述 字段
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ApiParams.class)
public @interface ApiParam {
	/**
	 * 字段
	 * 
	 * @return 字段名
	 */
	public String field();

	/**
	 * 是否必须，默认 否
	 * 
	 * @return 是否必须
	 */
	public boolean required() default false;

	/**
	 * 正则校验
	 * 
	 * @return 正则校验
	 */
	public String regexp() default "";

	/**
	 * 最小长度，数值为 最小值
	 * 
	 * @return 最小值
	 */
	public int min() default -2147483648;

	/**
	 * 最大长度，数值为最大值
	 * 
	 * @return 最大值
	 */
	public int max() default 2147483647;

	/**
	 * 示例说明
	 * 
	 * @return 示例说明
	 */
	public String describe() default "";

	/**
	 * 校验错误消息
	 * 
	 * @return 校验错误消息
	 */
	public String message() default "";
}
