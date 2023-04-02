package com.github.ddm4j.api.document.common.exception.bean;

import com.github.ddm4j.api.document.annotation.ApiParam;
import com.github.ddm4j.api.document.common.exception.ApiCheckError;

public class ApiCheckInfo {
	private String message;
	private String field;
	private String name;

	private ApiCheckError error;

	private ApiParam apiParam;

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

	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}

	public ApiCheckError getError() {
		return error;
	}

	public void setError(ApiCheckError error) {
		this.error = error;
	}

	public void setApiParam(ApiParam param){
		this.apiParam = param;
	}
	public ApiParam getApiParam(){
		return this.apiParam;
	}

}
