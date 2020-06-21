package com.github.ddm4j.api.document.bean;

import java.io.Serializable;

public class ParameterVo extends ParamChildrenVo<ParameterVo> implements Serializable {
	private static final long serialVersionUID = 1L;
	private String reg;
	private Integer min;
	private Integer max;

	public String getReg() {
		return reg;
	}

	public void setReg(String reg) {
		this.reg = reg;
	}

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

}
