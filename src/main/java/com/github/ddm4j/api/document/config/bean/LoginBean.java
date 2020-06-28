package com.github.ddm4j.api.document.config.bean;

/**
 * 登录配置
 */
public class LoginBean {
	// 是否启用
	private boolean enable = true;
	// 账号
	private String account = "ddm4j";
	// 密码
	private String password = "ddm4j";

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
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

}
