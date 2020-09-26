package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要隐藏的参数,为空表示，只留下被 ApiResponse 标识的字段
 * 支持 * 号匹配，只能用于前面或后面如: a_*.*_b
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiResponseIgnore {
	/**
	 * 需要隐藏的字段
	 * 
	 * @return 隐藏字段
	 */
	String[] value() default {};
}
