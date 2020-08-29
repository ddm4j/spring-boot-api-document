package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 字段说明
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) // 限制只能用于字段上
public @interface ApiField {
	/**
	 * @return 字段名称或说明
	 */
	@AliasFor("name")
	public String value() default "";

	/**
	 * @return 字段名称或说明
	 */
	@AliasFor("value")
	public String name() default "";
	/**
	 * @return 是否隐藏，默认 否
	 */
	public boolean hide() default false;

}
