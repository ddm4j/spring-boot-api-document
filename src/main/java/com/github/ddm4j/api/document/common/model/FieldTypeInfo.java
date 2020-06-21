package com.github.ddm4j.api.document.common.model;

import java.lang.reflect.Type;

public class FieldTypeInfo {
	private Type genType;

	private Class<?> clazz;
	private FieldType claType;

	private FieldType insideType;
	private Class<?> insideClazz;

	public Type getGenType() {
		return genType;
	}

	public void setGenType(Type genType) {
		this.genType = genType;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public FieldType getClaType() {
		return claType;
	}

	public void setClaType(FieldType claType) {
		this.claType = claType;
	}

	public Class<?> getInsideClazz() {
		return insideClazz;
	}

	public void setInsideClazz(Class<?> insideClazz) {
		this.insideClazz = insideClazz;
	}

	public FieldType getInsideType() {
		return insideType;
	}

	public void setInsideType(FieldType insideType) {
		this.insideType = insideType;
	}
}
