package com.github.ddm4j.api.document.controller;

import com.github.ddm4j.api.document.bean.InterfaceJsonDoc;
import com.github.ddm4j.api.document.utils.ScanControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.ddm4j.api.document.config.CheckConfig;
import com.github.ddm4j.api.document.config.DocumentConfig;

@Controller
public class ApiDocumentController {

	@Autowired
	DocumentConfig config;
	@Autowired
	CheckConfig chConfig;

	/**
	 * 获取数据
	 * 
	 * @param account
	 *            login_account
	 * @param password
	 *            login_password
	 * @return code 1000 成功 1001 文档关闭 2001 登录失败 3001 账号或密码为空
	 * 
	 */
	@ResponseBody
	@RequestMapping("/api/document")
	public InterfaceJsonDoc document(String account, String password) {
		// 数据返回对象
		InterfaceJsonDoc doc = new InterfaceJsonDoc();

		doc.setName(config.getName());
		doc.setDescribe(config.getDescribe());
		doc.setVersion(config.getVersion());

		if (!config.isEnable()) {
			// 文档关闭
			doc.setCode(1001);
			return doc;
		}
		// 未设置扫描包路径，等同为关闭
		if ((null == config.getPath() || "".equals(config.getPath().trim()))) {
			// 文档关闭
			doc.setCode(1001);
			return doc;
		}

		if (config.getLogin().isEnable()) {
			doc.setCode(2001);
			if (null != config.getLogin().getAccount() && !"".equals(config.getLogin().getAccount().trim())) {
				if (null == account || "".equals(account.trim())) {
					doc.setCode(3001);
					return doc;
				}

				if (!account.trim().equals(config.getLogin().getAccount().trim())) {
					return doc;
				}
			}

			if (null != config.getLogin().getPassword() && !"".equals(config.getLogin().getPassword().trim())) {
				if (null == password || "".equals(password.trim())) {
					doc.setCode(3001);
					return doc;
				}

				if (!password.trim().equals(config.getLogin().getPassword().trim())) {
					return doc;
				}
			}
		}
		// 权限都校验成功了
		doc.setCode(1000);
		// 路径前缀处理
		String path = config.getContextPath();
		if (null != path && !"".equals(path)) {
			if (!path.startsWith("/")) {
				path = "/" + path;
			}

			if (path.endsWith("/") && path.length() > 2) {
				path = path.substring(0, path.length() - 1);
			}
		}
		// 前缀
		String prefix = config.getPrefix();
		if (null != prefix && !"".equals(prefix.trim())) {
			if (!prefix.startsWith("/")) {
				prefix = "/" + prefix;
			}

			if (prefix.endsWith("/") && path.length() > 2) {
				prefix = prefix.substring(0, prefix.length() - 1);
			}
			path = prefix + path;
		}
		ScanControllerUtil util = new ScanControllerUtil(chConfig, config);
		// 扫描
		doc.setControllers(util.scan(config.getPath(), path));
		return doc;
	}
}
