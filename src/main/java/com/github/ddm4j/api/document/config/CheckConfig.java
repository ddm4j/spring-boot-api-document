package com.github.ddm4j.api.document.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.github.ddm4j.api.document.config.bean.MessageBean;

/**
 * 参数校验配置
 *
 */
@Component
@ConfigurationProperties(prefix = "api-document.check")
public class CheckConfig {
	// 是否启用
	public boolean enable = true;
	// 是否全部校验
	private boolean all = false;

	@Value("${api-document.check.date-format:}")
	private String dateFormat;

	// 正则校验
	private Map<String, String> regexps = new HashMap<String, String>();
	// 校验错误消息
	private Map<String, MessageBean> messages = new HashMap<String, MessageBean>();

	{
		// 创建系统默认配置
		// 错误消息
		messages.put("default", new MessageBean());
		// 正则
		regexps.put("email", "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");// email
		regexps.put("phone", "^(13[0-9]|17[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$");// 手机号码
		regexps.put("telephone", "^(\\(\\d{3,4}-)|\\d{3.4}-)?\\d{7,8}$");// 固定电话

		regexps.put("date", "^[0-9]{4}-(0?[0-9]|1[0-2])-(0?[0-9]|[12][0-9]|3[01])$");// 日期：yyyy-MM-dd
		regexps.put("date_M", "^[0-9]{4}-(0?[0-9]|1[0-2])$");// 日期：yyyy-MM
		regexps.put("date_Md", "^(0?[0-9]|1[0-2])-(0?[0-9]|[12][0-9]|3[01])$");// 日期：MM-dd

		regexps.put("time", "^(0?[0-9]|1[0-9]|2[0-3]):(0?[0-9]|[1-5][0-9]):(0?[0-9]|[1-5][0-9])$");// 时间：HH:mm:ss
		regexps.put("time_Hm", "^(0?[0-9]|1[0-9]|2[0-3]):(0?[0-9]|[1-5][0-9])$");// 时间：HH:mm
		regexps.put("time_ms", "^(0?[0-9]|[1-5][0-9]):(0?[0-9]|[1-5][0-9])$");// 时间：mm:ss

		regexps.put("dateTime",
				"^[0-9]{4}-(0?[0-9]|1[0-2])-(0?[0-9]|[12][0-9]|3[01]) (0?[0-9]|1[0-9]|2[0-3]):(0?[0-9]|[1-5][0-9]):(0?[0-9]|[1-5][0-9])$");// 日期时间:
		regexps.put("dateTime_Hm",
				"^[0-9]{4}-(0?[0-9]|1[0-2])-(0?[0-9]|[12][0-9]|3[01]) (0?[0-9]|1[0-9]|2[0-3]):(0?[0-9]|[1-5][0-9])$");// 日期时间:
		regexps.put("dateTime_H", "^[0-9]{4}-(0?[0-9]|1[0-2])-(0?[0-9]|[12][0-9]|3[01]) (0?[0-9]|[1-5][0-9])$");// 日期时间:
																												// yyyy-MM-dd
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean isAll() {
		return all;
	}

	public void setAll(boolean all) {
		this.all = all;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public Map<String, String> getRegexps() {
		return regexps;
	}

	public void setRegexps(Map<String, String> regexps) {
		this.regexps = regexps;
	}

	public Map<String, MessageBean> getMessages() {
		return messages;
	}

	public void setMessages(Map<String, MessageBean> messages) {
		this.messages = messages;
	}

}
