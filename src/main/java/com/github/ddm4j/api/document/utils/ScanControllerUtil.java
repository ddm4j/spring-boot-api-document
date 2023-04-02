package com.github.ddm4j.api.document.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.ddm4j.api.document.annotation.ApiController;
import com.github.ddm4j.api.document.annotation.ApiHeaderCancel;
import com.github.ddm4j.api.document.annotation.ApiIgnore;
import com.github.ddm4j.api.document.annotation.ApiResponseCode;
import com.github.ddm4j.api.document.bean.ControllerVo;
import com.github.ddm4j.api.document.bean.HeadVo;
import com.github.ddm4j.api.document.bean.InterfaceVo;
import com.github.ddm4j.api.document.bean.ResponseVo;
import com.github.ddm4j.api.document.common.model.KVEntity;
import com.github.ddm4j.api.document.config.CheckConfig;
import com.github.ddm4j.api.document.config.DocumentConfig;
import com.github.ddm4j.api.document.config.ResponseCodeConfig;
import com.github.ddm4j.api.document.config.bean.RequestHeaderBean;

public class ScanControllerUtil {
	CheckConfig config;
	DocumentConfig documentConfig;
	ResponseCodeConfig codeConfig;

	Logger logger = LoggerFactory.getLogger(ScanControllerUtil.class);

	public ScanControllerUtil(CheckConfig config, DocumentConfig documentConfig, ResponseCodeConfig codeConfig) {
		this.config = config;
		this.documentConfig = documentConfig;
		this.codeConfig = codeConfig;
	}

	public List<ControllerVo> scan(String packagePath, String base_path) {
		// 查询所有类
		Set<Class<?>> classList = ClassUtil.getClasses(packagePath);

		if (null == classList || classList.size() == 0) {
			logger.error("没有找到 Controller 路径：" + packagePath);
			return null;
		}

		List<ControllerVo> controllers = new ArrayList<ControllerVo>();

		MethodRequestUtil requestUtil = new MethodRequestUtil(config);
		MethodResponseUtil responseUtil = new MethodResponseUtil(codeConfig);

		// 提取上的请求头信息
		List<HeadVo> headVosConfig = extractHeaderInfo();
		// 循环操作
		for (Class<?> cla : classList) {

			// 判断是否忽略了
			if (null != cla.getAnnotation(ApiIgnore.class)) {
				continue;
			}

			// 没有 Controller 注解，下一个
			if (null == cla.getAnnotation(RestController.class) && null == cla.getAnnotation(Controller.class)) {
				continue;
			}
			// 提取Controller基本信息
			ControllerVo cvo = extractControllerInfo(cla, base_path);
			// 处理取消请求头信息
			List<HeadVo> headVos = handleHeaderCancel(headVosConfig, cla.getAnnotation(ApiHeaderCancel.class));

			// 提取方法
			List<InterfaceVo> interfaces = null;

			Method[] methods = cla.getMethods();
			if (null != methods && methods.length > 0) {
				interfaces = new ArrayList<InterfaceVo>();

				ApiResponseCode code = cla.getAnnotation(ApiResponseCode.class);

				for (Method method : methods) {

					// 判断是否忽略了
					if (null != method.getAnnotation(ApiIgnore.class)) {
						continue;
					}

					InterfaceVo ivo = requestUtil.getRequestVo(method, cvo.getMethod());
					if (null != ivo) {

						// 提取方法上的请求头信息
						List<HeadVo> methodVos = handleHeaderCancel(headVos,
								method.getAnnotation(ApiHeaderCancel.class));

						if (null != methodVos) {
							handleMethodHander(ivo, methodVos);
						}

						// 提取方法上的返回值
						KVEntity<String, List<ResponseVo>> kv = responseUtil.getResponseVo(method, code);
						if (null != kv) {
							ivo.setResponseMethod(kv.getLeft());
							ivo.setResponses(kv.getRight());
						}

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

	// 处理方法上的请求头
	private void handleMethodHander(InterfaceVo ivo, List<HeadVo> methodVos) {
		if (null != methodVos && methodVos.size() > 0) {
			for (HeadVo head : methodVos) {

				if (null == ivo.getHeads()) {
					ivo.setHeads(new ArrayList<HeadVo>());
				}
				boolean isOk = true;
				for (HeadVo vo : ivo.getHeads()) {
					if (vo.getField().equals(head.getField())) {
						if (FieldUtil.isEmpty(vo.getName())) {
							vo.setName(head.getName());
						}
						isOk = false;
						break;
					}
				}
				if (isOk) {
					ivo.getHeads().add(head);
				}

			}
		}
	}

	// 处理取消请求头
	private List<HeadVo> handleHeaderCancel(List<HeadVo> headVosConfig, ApiHeaderCancel headerCancel) {
		if (null == headerCancel) {
			return headVosConfig;
		}
		List<HeadVo> headVos = new ArrayList<HeadVo>();
		// 为空，取消全部
		if (null != headerCancel.value() && headerCancel.value().length > 0) {
			// 不为空，取消指定的
			for (HeadVo headVo : headVosConfig) {
				boolean isOk = true;
				for (String key : headerCancel.value()) {
					if (key.equals(headVo.getField())) {
						isOk = false;
						break;
					}
				}
				if (isOk) {
					headVos.add(headVo);
				}
			}
		}
		return headVos;
	}

	/**
	 * 提取类上面的请求头信息
	 * @return
	 */
	private List<HeadVo> extractHeaderInfo() {
		List<HeadVo> headVos = new ArrayList<HeadVo>();

		for (Entry<String, RequestHeaderBean> bean : documentConfig.getHeader().entrySet()) {
			HeadVo vo = new HeadVo();

			vo.setField(bean.getKey());
			if (null != bean.getValue()) {
				vo.setName(bean.getValue().getDescribe());
				vo.setMax(bean.getValue().getMax());
				vo.setMin(bean.getValue().getMin());
				vo.setRegexp(bean.getValue().getRegexp());
				vo.setRequired(bean.getValue().getRequired());
				vo.setType(bean.getValue().getType());
			}
			headVos.add(vo);
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

		// cvo.setController(cla.getName().substring(cla.getName().lastIndexOf(".") +
		// 1));
		cvo.setController(cla.getSimpleName());
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
		ApiController apiController = AnnotationUtils.getAnnotation(cla, ApiController.class);
		if (null != apiController) {

			cvo.setName(apiController.value());
			cvo.setDescribe(apiController.describe());
			cvo.setVersion(apiController.version());
			cvo.setAuthor(apiController.author());
		}

		// RequestMapper 注解
		RequestMapping rquestMapping = AnnotationUtils.getAnnotation(cla, RequestMapping.class);
		if (null != rquestMapping) {

			if (null != base_path && !"".equals(base_path.trim())) {
				base_path = base_path.trim();
				if (!base_path.startsWith("/")) {
					base_path = "/" + base_path;
				}
			}

			List<String> uris = new ArrayList<String>();
			if (null != rquestMapping.value() && rquestMapping.value().length > 0) {
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
