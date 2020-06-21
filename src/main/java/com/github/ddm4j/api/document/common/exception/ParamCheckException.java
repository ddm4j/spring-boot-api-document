package com.github.ddm4j.api.document.common.exception;

/**
 * 字段校验异常对象
 */
@SuppressWarnings("serial")
public class ParamCheckException extends RuntimeException {
	private String message;
	private String field;
	private String describe;
	private ParamCheckError error;

	public ParamCheckException() {

	}

	public ParamCheckException(String field, ParamCheckError error) {
		super();

		this.field = field;
		this.error = error;
	}

	public ParamCheckException(String field, ParamCheckError error, String describe, String message) {
		super();
		this.message = message;
		this.field = field;
		this.describe = describe;
		this.error = error;
	}

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

	public ParamCheckError getError() {
		return error;
	}

	public void setError(ParamCheckError error) {
		this.error = error;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

}
