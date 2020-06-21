package com.github.ddm4j.api.document.bean;

import java.io.Serializable;
import java.util.List;

public class ParamChildrenVo<T extends ParamBaseVo> extends ParamBaseVo implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<T> children;

	public List<T> getChildren() {
		return children;
	}

	public void setChildren(List<T> children) {
		this.children = children;
	}

}
