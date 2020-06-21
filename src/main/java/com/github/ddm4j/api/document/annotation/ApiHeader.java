package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PI请求头说明，限制只能用于Controller和方法上
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface ApiHeader {

	/**
	 * 参数名，如果为空，不处理
	 * 
	 * @return 参数名
	 */
	public ApiHeadParam[] value() default {};

}
