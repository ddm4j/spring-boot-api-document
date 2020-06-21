package com.github.ddm4j.api.document.controller;

import java.util.List;

import com.github.ddm4j.api.document.bean.ControllerVo;
import com.github.ddm4j.api.document.bean.InterfaceJsonDoc;
import com.github.ddm4j.api.document.utils.ScanControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.ddm4j.api.document.config.ApiDocumentConfig;

@Controller
public class ApiDocumentController {

	@Autowired
	ApiDocumentConfig config;

	private List<ControllerVo> vos = null;

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
		if (null == vos && (null == config.getController() || "".equals(config.getController().trim()))) {
			// 文档关闭
			doc.setCode(1001);
			return doc;
		}

		if (config.isLogin()) {
			doc.setCode(2001);
			if (null != config.getAccount() && !"".equals(config.getAccount().trim())) {
				if (null == account || "".equals(account.trim())) {
					doc.setCode(3001);
					return doc;
				}

				if (!account.trim().equals(config.getAccount().trim())) {
					return doc;
				}
			}

			if (null != config.getPassword() && !"".equals(config.getPassword().trim())) {
				if (null == password || "".equals(password.trim())) {
					doc.setCode(3001);
					return doc;
				}

				if (!password.trim().equals(config.getPassword().trim())) {
					return doc;
				}
			}
		}
		// 权限都校验成功了
		doc.setCode(1000);
		// 判断是否扫描过了
		if (null == vos) {
			// 路径前缀处理
			String path = config.getPath();
			if (null != path && !"".equals(path)) {
				if (!path.startsWith("/")) {
					path = "/" + path;
				}

				if (path.endsWith("/") && path.length() > 2) {
					path = path.substring(0, path.length() - 2);
				}
			}
			// 扫描
			vos = ScanControllerUtil.scan(config.getController(), path);
		}

		doc.setControllers(vos);
		return doc;
	}
}
