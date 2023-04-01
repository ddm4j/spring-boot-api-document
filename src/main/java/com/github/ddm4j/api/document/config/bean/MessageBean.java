package com.github.ddm4j.api.document.config.bean;
/**
 * 错误消息体 
 */
public class MessageBean {

	public static final String DEFAULT = "default";
	public static final String CUSTOM = "custom";

	private String required = "不能为空";
	private String regexp = "非法数据";
	private String max = "超过限制";
	private String min = "低于限制";

	public String getRequired() {
		return required;
	}

	public void setRequired(String required) {
		this.required = required;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

}
