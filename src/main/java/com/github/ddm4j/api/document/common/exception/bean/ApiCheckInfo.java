package com.github.ddm4j.api.document.common.exception.bean;

import com.github.ddm4j.api.document.common.exception.ApiCheckError;

public class ApiCheckInfo {
	private String message;
	private String field;
	private String describe;
	private ApiCheckError error;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public ApiCheckError getError() {
		return error;
	}

	public void setError(ApiCheckError error) {
		this.error = error;
	}

}
