package com.github.ddm4j.api.document.bean;

import java.util.List;

public class InterfaceJsonDoc {
	private String name; // 系统名称
	private Integer code = 1000;// 状态
	private String describe;// 描述
	private String version;// 版本

	/**
	 * 接口Controller
	 */
	private List<ControllerVo> controllers;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public List<ControllerVo> getControllers() {
		return controllers;
	}

	public void setControllers(List<ControllerVo> controllers) {
		this.controllers = controllers;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
