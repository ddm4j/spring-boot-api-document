package com.github.ddm4j.api.document.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.github.ddm4j.api.document.config.bean.LoginBean;

/**
 * 文档配置
 */
@Component
@ConfigurationProperties(prefix = "api-document.document")
public class DocumentConfig {
	// 是否启用
	private boolean enable = true;
	// 扫描路径
	private String path;
	// 前缀
	private String prefix;
	// 项目名称
	private String name;
	// 项目版本
	private String version;
	// 描述
	private String describe;
	// 登录配置
	private LoginBean login;

	// 获取统一路径
	@Value("${server.servlet.context-path:}")
	private String contextPath = "";

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public LoginBean getLogin() {
		return login;
	}

	public void setLogin(LoginBean login) {
		this.login = login;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

}
