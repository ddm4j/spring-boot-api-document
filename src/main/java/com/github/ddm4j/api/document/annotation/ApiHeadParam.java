package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限制只能用于类和方法上，请求头参数说明
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ApiHeadParam {
	/**
	 * 字段名
	 * 
	 * @return 字段名
	 */
	public String value();

	/**
	 * 数据类型，默认 String
	 * 
	 * @return 数据类型
	 */
	public String type() default "String";

	/**
	 * 是否必须，默认 是
	 * 
	 * @return 是否必须
	 */
	public boolean required() default true;

	/**
	 * 正则表达式
	 * 
	 * @return 正则表达式
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
}
