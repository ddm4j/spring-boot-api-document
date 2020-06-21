package com.github.ddm4j.api.document.bean;

import java.io.Serializable;

public abstract class ParamBaseVo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String field;
	private String type;
	private boolean required = false;
	private String describe;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

}
