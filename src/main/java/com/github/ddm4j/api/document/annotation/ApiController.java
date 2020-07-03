package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 说明
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // 限制只能用于类上
public @interface ApiController {

	/**
	 * @return 类名称
	 */
	public String value();

	/**
	 * @return 描述说明
	 */
	public String describe() default "";

	/**
	 * @return 作者
	 */
	public String author() default "";

	/**
	 * @return 版本,默认 V1.0
	 */
	public String version() default "V1.0";

}
