package com.github.ddm4j.api.document.common.model;

public enum FieldType {
	/*
	 * 不是泛型，纯 Class
	 */
	Clazz(1000),
	/*
	 * 不是泛型，纯数组 Class[]
	 */
	ArrayClass(1001),
	/*
	 * 泛型：Class<T>
	 */
	ClassT(1002),
	/*
	 * Class<T>[]
	 */
	ArrayClassT(1003),
	/*
	 * 未确定泛型：T
	 */
	T(1004),
	/*
	 * 纯泛型数组：T
	 */
	ArrayT(1005);

	private int i;

	private FieldType(int i) {
		this.i = i;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public static FieldType getFieldType(int i) {
		for (FieldType aparameterStatus : values()) {
			if (aparameterStatus.getI() == i) {
				return aparameterStatus;
			}
		}
		return null;
	}

}
