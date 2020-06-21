package com.github.ddm4j.api.document.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication // web应用才生效
@ComponentScan(basePackages = { "com.github.ddm4j.api.document.controller", "com.github.ddm4j.api.document.check",
		"com.github.ddm4j.api.document.config" })
@ConfigurationProperties(prefix = "spring.document")
public class ApiDocumentConfig {

	// 开启文档: 默认关闭
	private boolean enable = false;
	// 是否开启数据校验
	private boolean check = true;
	// 扫描的controller路径
	private String controller;
	// 项目名
	private String name = "Spring Boot API Document";
	// 项目描述
	private String describe = "API Document";
	// 版本
	private String version = "V1.0";
	// 是否需要登录
	private boolean login = true;
	// 登录账号
	private String account = "ddm";
	// 登录密码
	private String password = "ddm";

	@Value("${server.servlet.context-path:}")
	private String path = "";

	public String getController() {
		return controller;
	}

	public void setController(String controller) {
		this.controller = controller;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean isLogin() {
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}

}
