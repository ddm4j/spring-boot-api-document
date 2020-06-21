package com.github.ddm4j.api.document.bean;

import java.io.Serializable;
import java.util.List;

public class ControllerVo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String controller;
	private String name;
	private List<String> uris;
	private String describe;
	private String author;
	private String version;
	private Boolean json;
	private String method;

	private List<InterfaceVo> interfaces;

	public String getController() {
		return controller;
	}

	public void setController(String controller) {
		this.controller = controller;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getUris() {
		return uris;
	}

	public void setUris(List<String> uris) {
		this.uris = uris;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<InterfaceVo> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<InterfaceVo> interfaces) {
		this.interfaces = interfaces;
	}

	public Boolean getJson() {
		return json;
	}

	public void setJson(Boolean json) {
		this.json = json;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

}
