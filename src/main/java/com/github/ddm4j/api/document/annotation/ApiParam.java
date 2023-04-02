package com.github.ddm4j.api.document.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 用于描述 字段,配置校验等信息
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ApiParams.class)
public @interface ApiParam {

    /**
     * 字段，多层请用点， 如 a.b
     *
     * @return 字段
     */
    @AliasFor("field")
    public String value() default "";

    /**
     * 字段，多层使用 a.b 的方式。
     *
     * @return 字段名
     */
    @AliasFor("value")
    public String field() default "";

    /**
     * 是否必须，默认 否
     *
     * @return 是否必须
     */
    public boolean required() default false;

    /**
     * 正则校验: 使用 ${key} 方式，读取配置文件中的正则校验规则，自带以下规则：
     * ${email} 电子邮件、
     * ${phone} 手机号(中国)、
     * ${telephone} 固定电话(中国)、
     * ${date} 日期：yyyy-MM-dd、
     * ${date_M} 日期：yyyy-MM、
     * ${date_Md} 日期：MM-dd、
     * ${dateTime} 日期：yyyy-MM-dd HH:mm:ss、
     * ${dateTime_Hm} 日期：yyyy-MM-dd HH:mm、
     * ${dateTime_H} 日期：yyyy-MM-dd HH、
     * ${time} 日期：HH:mm:ss、
     * ${time_Hm} 日期：HH:mm、
     * ${time_ms} 日期：mm:ss、
     * 以上规则，可在配置文件中重写。
     *
     * @return 正则校验
     */
    public String regexp() default "";

    /**
     * 数字类型判断值,字符串判断长度,设置的值不能是：-2147483648
     *
     * @return 最小值
     */
    public int min() default -2147483648;

    /**
     * 数字类型判断值,字符串判断长度,设置的值不能是：2147483647
     *
     * @return 最大值
     */
    public int max() default 2147483647;

    /**
     * 名称
     *
     * @return
     */
    public String name() default "";

    /**
     * 校验错误消息，使用 ${key} 读取配置文件中的配置;
     * 默认： ${default}， 可以配置文件中重写;
     * required: 不能为空;
     * regexp: 非法数据;
     * max: 超过限制;
     * min: 低于限制;
     *
     * @return 校验错误消息
     */
    public String message() default "";
}
