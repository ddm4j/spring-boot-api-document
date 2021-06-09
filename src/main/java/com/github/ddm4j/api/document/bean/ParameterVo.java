package com.github.ddm4j.api.document.bean;

import java.io.Serializable;

public class ParameterVo extends ParamChildrenVo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String regexp;
	private Integer min;
	private Integer max;
	private boolean get;

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	public boolean isGet() {
		return get;
	}

	public void setGet(boolean get) {
		this.get = get;
	}

}
