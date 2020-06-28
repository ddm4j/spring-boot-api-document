package com.github.ddm4j.api.document.common.exception;

import java.util.ArrayList;
import java.util.List;

import com.github.ddm4j.api.document.common.exception.bean.ApiCheckInfo;

/**
 * 字段校验异常对象
 */
public class ApiCheckException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String message;
	private List<ApiCheckInfo> apiCheckInfos = new ArrayList<ApiCheckInfo>();

	public ApiCheckException() {

	}

	public ApiCheckException(List<ApiCheckInfo> apiCheckInfos) {
		this.message = "校验数据发生异常";
		this.apiCheckInfos = apiCheckInfos;
	}

	/**
	 * @return 校验异常详情
	 */
	public List<ApiCheckInfo> getApiCheckInfos() {
		return apiCheckInfos;
	}

	/**
	 * 校验异常详情
	 * 
	 * @param checkInfos
	 *            详细信息
	 */
	public void setApiCheckInfos(List<ApiCheckInfo> checkInfos) {
		this.apiCheckInfos = checkInfos;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
