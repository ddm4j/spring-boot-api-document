package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 用户于配置Api返回code信息，只显示会返回的code信息
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiResponseCode {
	/**
	 * @return 返回值字段名
	 */
	public String field() default "";

	/**
	 * @return 是隐藏这些code还是只显示这些code，默认 false,只显示被 codes 标识的
	 */
	public boolean hide() default false;

	/**
	 * @return 只保留需要返回的 code，字段名，支持 * 匹配，只能于用前面或后面，如: *A , A*
	 */
	@AliasFor("codes")
	public String[] value() default {};
	
	/**
	 * @return 只保留需要返回的 code，字段名，支持 * 匹配，只能于用前面或后面，如: *A , A*
	 */
	@AliasFor("value")
	public String[] codes() default {};
	
	/**
	 * @return 隐藏在配置文件中配置，统一返回的状态码，支持 * 匹配，只能于用前面或后面，如: *A , A*
	 */
	public String[] cancel() default {};
}
