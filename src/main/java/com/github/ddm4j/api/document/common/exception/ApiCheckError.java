package com.github.ddm4j.api.document.common.exception;

/**
 * 参数校验失败枚举类
 */
public enum ApiCheckError {
	/*
	 * 系统错误
	 */
	ERROR,
	/*
	 * 数据为空
	 */
	EMPTY,
	/*
	 * 正则校验失败
	 */
	REGEXP,
	/*
	 * 长度不足或数值过小
	 */
	MIN,
	/*
	 * 长度过长或数值过大
	 */
	MAX;
}
