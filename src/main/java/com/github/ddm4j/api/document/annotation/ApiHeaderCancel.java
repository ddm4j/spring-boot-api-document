package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来表示该方法请求不需要请求头
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface ApiHeaderCancel {
	/**
	 * 要隐藏展示请求头的字段，为空，代表取消全部。
	 * 
	 * @return 隐藏字段
	 */
	public String[] value() default "";
}
