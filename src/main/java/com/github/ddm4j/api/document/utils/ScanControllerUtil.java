package com.github.ddm4j.api.document.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.ddm4j.api.document.annotation.ApiController;
import com.github.ddm4j.api.document.annotation.ApiHeadParam;
import com.github.ddm4j.api.document.annotation.ApiHeader;
import com.github.ddm4j.api.document.annotation.ApiHeaderCancel;
import com.github.ddm4j.api.document.bean.ControllerVo;
import com.github.ddm4j.api.document.bean.HeadVo;
import com.github.ddm4j.api.document.bean.InterfaceVo;
import com.github.ddm4j.api.document.config.CheckConfig;

public class ScanControllerUtil {
	CheckConfig config;

	public ScanControllerUtil(CheckConfig config) {
		this.config = config;
	}

	public List<ControllerVo> scan(String packagePath, String base_path) {
		// 查询所有类
		Set<Class<?>> classList = ClassUtil.getClasses(packagePath);

		if (null == classList || classList.size() == 0) {
			System.out.println("没有找到：" + packagePath);
			return null;
		}

		List<ControllerVo> controllers = new ArrayList<ControllerVo>();

		MethodRequestUtil requestUtil = new MethodRequestUtil(config);
		MethodResponseUtil responseUtil = new MethodResponseUtil();
		// 循环操作
		for (Class<?> cla : classList) {

			// 没有 Controller 注解，下一个
			if (null == cla.getAnnotation(RestController.class) && null == cla.getAnnotation(Controller.class)) {
				continue;
			}
			// 提取Controller基本信息
			ControllerVo cvo = extractControllerInfo(cla, base_path);
			// 提取Controller 上的请求头信息
			List<HeadVo> headVos = extractHeaderInfo(cla);
			// System.out.println("提取Controller：" + cla.getName());
			// 提取方法
			List<InterfaceVo> interfaces = null;
			Method[] methods = cla.getMethods();
			if (null != methods && methods.length > 0) {
				interfaces = new ArrayList<InterfaceVo>();
				for (Method method : methods) {
					InterfaceVo ivo = requestUtil.getRequestVo(method, cvo.getMethod());
					if (null != ivo) {

						// 提取方法上的请求头信息
						ApiHeaderCancel headerCancel = method.getAnnotation(ApiHeaderCancel.class);
						if (null == headerCancel) {
							if (null != headVos && headVos.size() > 0) {
								for (HeadVo head : headVos) {
									if (null == ivo.getHeads()) {
										ivo.setHeads(new ArrayList<HeadVo>());
									}
									ivo.getHeads().add(head);
								}
							}
						}

						// 提取方法上的返回值
						ivo.setResponses(responseUtil.getResponseVo(method));
						// 保存到接口列表中

						if (null != cvo.getUris() && cvo.getUris().size() > 0) {
							if (null != ivo.getUris() && ivo.getUris().size() > 0) {
								for (String path1 : cvo.getUris()) {
									for (String path2 : ivo.getUris()) {
										InterfaceVo vo2 = new InterfaceVo();
										// 对象
										BeanUtils.copyProperties(ivo, vo2);

										if (!path1.startsWith("/")) {
											path1 = "/" + path1;
										}

										if (path1.endsWith("/")) {
											path1 = path1.substring(0, path1.length() - 1);
										}
										if (!path2.startsWith("/")) {
											path2 = "/" + path2;
										}
										vo2.setUri(path1 + path2);
										vo2.setUris(null);
										interfaces.add(vo2);
									}
								}
							}
						} else if (null != ivo.getUris() && ivo.getUris().size() > 0) {
							for (String path : ivo.getUris()) {
								InterfaceVo vo2 = new InterfaceVo();
								// 对象
								BeanUtils.copyProperties(ivo, vo2);
								if (!path.startsWith("/")) {
									path = "/" + path;
								}
								vo2.setUri(path);
								vo2.setUris(null);
								interfaces.add(vo2);
							}
						} else {
							// System.out.println(" -- " + method.getName() + " -- " +
							// JSON.toJSONString(ivo.getHeads()));
							interfaces.add(ivo);
						}

					}
				}
			}
			// 保存到 controller 中
			cvo.setInterfaces(interfaces);
			controllers.add(cvo);
		}

		return controllers;
	}

	/**
	 * 提取类上面的请求头信息
	 * 
	 * @param cla
	 * @return
	 */
	private static List<HeadVo> extractHeaderInfo(Class<?> cla) {
		ApiHeader ah = cla.getAnnotation(ApiHeader.class);
		List<HeadVo> headVos = null;
		if (null != ah) {
			ApiHeadParam[] ahps = ah.value();
			if (null != ahps && ahps.length > 0) {
				headVos = new ArrayList<HeadVo>();
				for (ApiHeadParam ap : ahps) {
					HeadVo head = new HeadVo();

					head.setField(ap.value());
					head.setRequired(ap.required());
					head.setType(ap.type());
					head.setDescribe(ap.describe());
					head.setRegexp(ap.regexp());
					head.setMax(2147483647 == ap.max() ? null : ap.max());
					head.setMin(-2147483648 == ap.min() ? null : ap.min());
					
					// 保存到列表
					headVos.add(head);
				}
			}
		}

		ApiHeadParam ap = cla.getAnnotation(ApiHeadParam.class);
		if (null != ap) {

			if (null == headVos) {
				headVos = new ArrayList<HeadVo>();
			}

			HeadVo head = new HeadVo();
			head.setDescribe(ap.describe());
			head.setField(ap.value());

			head.setRequired(ap.required());
			head.setType(ap.type());
			head.setRegexp(ap.regexp());
			
			head.setMax(2147483647 == ap.max() ? null : ap.max());
			head.setMin(-2147483648 == ap.min() ? null : ap.min());
			
			headVos.add(head);
		}
		return headVos;
	}

	/**
	 * 提取 Controller 上的信息
	 * 
	 * @param cla
	 * @param base_path
	 * @return
	 */
	private ControllerVo extractControllerInfo(Class<?> cla, String base_path) {
		ControllerVo cvo = new ControllerVo();
		// 名称
		cvo.setController(cla.getName().substring(cla.getName().lastIndexOf(".") + 1));
		// 判断是不是 JSON
		Annotation json = cla.getAnnotation(ResponseBody.class);
		if (null == json) {
			json = cla.getAnnotation(RestController.class);
		}
		if (null != json) {
			cvo.setJson(true);
		}

		// 获取 类上面的注解
		// ApiController 注解
		ApiController apiController = cla.getAnnotation(ApiController.class);
		if (null != apiController) {

			cvo.setName(apiController.value());
			cvo.setDescribe(apiController.describe());
			cvo.setVersion(apiController.version());
			cvo.setAuthor(apiController.author());
		} else {
			// System.out.println("没有注解");
			cvo.setName(cvo.getController());
			cvo.setVersion("V1.0");
		}

		// RequestMapper 注解
		RequestMapping rquestMapping = cla.getAnnotation(RequestMapping.class);
		if (null != rquestMapping) {

			if (null != base_path && !"".equals(base_path.trim())) {
				base_path = base_path.trim();
				if (!base_path.startsWith("/")) {
					base_path = "/" + base_path;
				}
			}

			List<String> uris = new ArrayList<String>();
			if (null != rquestMapping.path() && rquestMapping.path().length > 0) {
				for (String path : rquestMapping.path()) {
					if (!path.startsWith("/")) {
						path = "/" + path;
					}
					uris.add(base_path + path);
				}
			} else if (null != rquestMapping.value() && rquestMapping.value().length > 0) {
				for (String path : rquestMapping.value()) {
					if (!path.startsWith("/")) {
						path = "/" + path;
					}
					uris.add(base_path + path);
				}
			}

			if (uris.size() > 0) {
				cvo.setUris(uris);
			}
			// 请求方式,转大写
			if (null != rquestMapping.method() && rquestMapping.method().length > 0) {
				cvo.setMethod(rquestMapping.method()[0].toString().toUpperCase());
			}
		}

		return cvo;
	}

}
