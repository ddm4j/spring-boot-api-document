package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 限制只能用于方法上，接口说明
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiMethod {

	/**
	 * @return 接口名称
	 */
	@AliasFor("name")
	public String value() default "";

	/**
	 * @return 接口名称
	 */
	@AliasFor("value")
	public String name() default "";

	/**
	 * @return 描述说明
	 */
	public String describe() default "";

	/**
	 * @return 作者
	 */
	public String author() default "";

	/**
	 * @return 版本
	 */
	public String version() default "";
}
