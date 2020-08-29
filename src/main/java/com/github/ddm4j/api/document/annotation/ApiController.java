package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * Controller 说明
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // 限制只能用于类上
public @interface ApiController {

	/**
	 * @return 名称
	 */
	@AliasFor("name")
	public String value() default "";

	/**
	 * @return 名称
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
