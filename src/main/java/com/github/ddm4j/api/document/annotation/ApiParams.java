package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * ApiParam 的注解组，不推荐使用，
 * 请直接使用 ApiParam 多重注解方式。
 * 即直接在在方法写多个 ApiParam 注解。
 * 不支持 map 类型字段。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiParams {
	/**
	 * 请求参数
	 * 
	 * @return 参数集合
	 */
	ApiParam[] value();
}
