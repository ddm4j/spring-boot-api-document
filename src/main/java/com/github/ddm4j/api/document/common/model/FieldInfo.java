package com.github.ddm4j.api.document.common.model;

import java.util.List;

public class FieldInfo {
	private String field;
	private String name;
	private String type;
	private String describe;
	private List<FieldInfo> children;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public List<FieldInfo> getChildren() {
		return children;
	}

	public void setChildren(List<FieldInfo> children) {
		this.children = children;
	}

}
