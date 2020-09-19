package com.github.ddm4j.api.document.bean;

import java.io.Serializable;
import java.util.List;

public class ParamChildrenVo extends ParamBaseVo implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<? extends ParamChildrenVo> children;

	public List<? extends ParamChildrenVo> getChildren() {
		return children;
	}

	public void setChildren(List<? extends ParamChildrenVo> children) {
		this.children = children;
	}

}
