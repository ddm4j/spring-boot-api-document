package com.github.ddm4j.api.document.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "api-document.response-code")
public class ResponseCodeConfig {
	/**
	 * 字段
	 */
	private String field = null;
	/**
	 * 状态码
	 */
	private String[] codes = null;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = null == field?null:field.trim().equals("")?null:field.trim();
	}

	public String[] getCodes() {
		return codes;
	}

	public void setCodes(String[] codes) {
		this.codes = codes;
	}

}
