package com.github.ddm4j.api.document.common.exception;

/**
 * 参数校验失败枚举类
 * 
 * @author DDM
 *
 */
public enum ParamCheckError {
	/*
	 * 系统错误
	 */
	ERROR(1001),
	/*
	 * 数据为空
	 */
	EMPTY(1002),
	/*
	 * 正则校验失败
	 */
	REGULAR(1003),
	/*
	 * 长度不足或数值过小
	 */
	MIN(1004),
	/*
	 * 长度过长或数值过大
	 */
	MAX(1005);

	private int i;

	private ParamCheckError(int i) {
		this.i = i;
	}

	public int getValue() {
		return i;
	}

}
