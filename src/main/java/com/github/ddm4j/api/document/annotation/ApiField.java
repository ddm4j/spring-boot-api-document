package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段说明
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) // 限制只能用于字段上
public @interface ApiField {
	/**
	 * 字段名称
	 * 
	 * @return 名称
	 */
	public String value();

	/**
	 * 是否隐藏，默认 否
	 * 
	 * @return 隐藏是否
	 */
	public boolean hide() default false;

}
