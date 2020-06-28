package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限制只能用于方法上，接口说明
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiMethod {

	/**
	 * 接口名称
	 * 
	 * @return 接口名称
	 */
	public String value();

	/**
	 * 描述说明
	 * 
	 * @return 描述说明
	 */
	public String describe() default "";

	/**
	 * 作者
	 * 
	 * @return 作者
	 */
	public String author() default "";

	/**
	 * 版本，默认V1.0.0
	 * 
	 * @return 版本
	 */
	public String version() default "V1.0.0";
}
