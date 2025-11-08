package com.github.ddm4j.api.document.bean;

import java.io.Serializable;

public class HeadVo extends ParamBaseVo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String regexp;
	private Long min;
	private Long max;

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	public Long getMin() {
		return min;
	}

	public void setMin(Long min) {
		this.min = min;
	}

	public Long getMax() {
		return max;
	}

	public void setMax(Long max) {
		this.max = max;
	}

}
