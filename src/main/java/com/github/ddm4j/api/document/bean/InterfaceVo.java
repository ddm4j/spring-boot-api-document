package com.github.ddm4j.api.document.bean;

import java.io.Serializable;
import java.util.List;

public class InterfaceVo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private List<String> uris;
	private String uri;
	private String describe;
	private String author;
	private String version;
	private String method;
	private boolean json = false;
	private String jsonMethod;
	// 请求参数是否是 Array
	private boolean paramArray = false;
	// 返回值是否是 Array
	private String responseMethod = "Object";

	private List<HeadVo> heads;// 请求头参数
	private List<HeadVo> uriParams;// 路径上参数
	private List<ParameterVo> parameters;// 请求参数
	private List<ResponseVo> responses;// 返回数据

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
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

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public boolean isJson() {
		return json;
	}

	public void setJson(boolean json) {
		this.json = json;
	}

	public String getJsonMethod() {
		return jsonMethod;
	}

	public void setJsonMethod(String jsonMethod) {
		this.jsonMethod = jsonMethod;
	}

	public List<HeadVo> getHeads() {
		return heads;
	}

	public void setHeads(List<HeadVo> heads) {
		this.heads = heads;
	}

	public List<ParameterVo> getParameters() {
		return parameters;
	}

	public void setParameters(List<ParameterVo> parameters) {
		this.parameters = parameters;
	}

	public List<ResponseVo> getResponses() {
		return responses;
	}

	public void setResponses(List<ResponseVo> responses) {
		this.responses = responses;
	}

	public boolean isParamArray() {
		return paramArray;
	}

	public void setParamArray(boolean paramArray) {
		this.paramArray = paramArray;
	}

	public List<HeadVo> getUriParams() {
		return uriParams;
	}

	public void setUriParams(List<HeadVo> uriParams) {
		this.uriParams = uriParams;
	}

	public String getResponseMethod() {
		return responseMethod;
	}

	public void setResponseMethod(String responseMethod) {
		this.responseMethod = responseMethod;
	}

}
