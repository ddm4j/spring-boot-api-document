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
	 * 类名称
	 * 
	 * @return 名称
	 */
	public String value();

	/**
	 * 描述说明
	 * 
	 * @return 描述
	 */
	public String describe() default "";

	/**
	 * 作者
	 * 
	 * @return 作者
	 */
	public String author() default "";

	/**
	 * 版本,默认 V1.0
	 * 
	 * @return 版本
	 */
	public String version() default "V1.0";

}
