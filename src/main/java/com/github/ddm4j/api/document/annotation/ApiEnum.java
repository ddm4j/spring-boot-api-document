package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enum 类型字段描述，不可用于泛型，集合中
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) // 限制只能用于字段上
public @interface ApiEnum {
	/**
	 * @return 实际返回给前端的实际数据字段
	 */
	public String show() default "";

	/**
	 * 
	 * @return 对应显示值的描述的字段
	 */
	public String describe() default "";

}
