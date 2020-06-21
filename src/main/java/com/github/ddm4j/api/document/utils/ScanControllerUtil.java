package com.github.ddm4j.api.document.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.ddm4j.api.document.annotation.ApiController;
import com.github.ddm4j.api.document.annotation.ApiHeadParam;
import com.github.ddm4j.api.document.annotation.ApiHeader;
import com.github.ddm4j.api.document.annotation.ApiHeaderCancel;
import com.github.ddm4j.api.document.annotation.ApiMethod;
import com.github.ddm4j.api.document.annotation.ApiParams;
import com.github.ddm4j.api.document.annotation.ApiResponseHides;
import com.github.ddm4j.api.document.annotation.ApiResponses;
import com.github.ddm4j.api.document.bean.ControllerVo;
import com.github.ddm4j.api.document.bean.HeadVo;
import com.github.ddm4j.api.document.bean.InterfaceVo;
import com.github.ddm4j.api.document.bean.ParamChildrenVo;
import com.github.ddm4j.api.document.bean.ParameterVo;
import com.github.ddm4j.api.document.bean.ResponseVo;

public class ScanControllerUtil {

	/**
	 * 搜索，扫描 Controller
	 * 
	 * @param packagePath
	 *            包路径
	 * @param base_path
	 *            基本路径
	 * @return 处理结果
	 */
	public static List<ControllerVo> scan(String packagePath, String base_path) {
		/**
		 * 查询所有类
		 */
		Set<Class<?>> classList = ClassUtil.getClasses(packagePath);

		if (null == classList || classList.size() == 0) {
			System.out.println("没有找到：" + packagePath);
			return null;
		}

		List<ControllerVo> controllers = new ArrayList<ControllerVo>();

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
					// System.out.println("提取方法："+method.getName());
					// 提取方法上的信息及请求参数信息
					InterfaceVo ivo = extractMethodInfo(cvo.getMethod(), method);

					if (null != ivo) {

						// 提取方法上的请求头信息
						ivo.setHeads(extractMethodHeaderInfo(headVos, method));
						// 提取方法上的返回值
						ivo.setResponses(extractReturnInfo(method, ivo));
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

		return (null != controllers && controllers.size() > 0) ? controllers : null;

	}

	/**
	 * 提取返回值信息
	 * 
	 * @param method
	 * @param ivo
	 * @return
	 */
	private static List<ResponseVo> extractReturnInfo(Method method, InterfaceVo ivo) {
		// 提取返回值注解
		ApiResponses response = method.getAnnotation(ApiResponses.class);
		// 扫描提取
		List<ResponseVo> vos = MethodFieldUtil.extractMothodReturnField(method,
				null != response ? response.value() : null);

		// 判断返回值是否是数组
		ivo.setResponseMethod((MethodFieldUtil.checkClazzType(method.getReturnType(), method.getGenericReturnType())));

		ApiResponseHides hides = method.getAnnotation(ApiResponseHides.class);

		if (null != hides && null != hides.value() && hides.value().length > 0) {
			// 删除隐藏的
			for (String field : hides.value()) {
				removeField(vos, field);
			}
		}
		return vos;
	}

	/**
	 * 删除指定属性
	 * 
	 * @param vos
	 *            list
	 * @param field
	 *            filed
	 * @param <T>
	 *            删除后的数据
	 */
	public static <T extends ParamChildrenVo<?>> void removeField(List<T> vos, String field) {
		if (null != vos && vos.size() > 0) {
			int index = -1;
			for (int i = 0; i < vos.size(); i++) {
				if (vos.get(i).getField().equals(field)) {
					index = i;
					break;
				}
			}
			if (index >= 0) {
				vos.remove(index);
			}
		}
	}

	/**
	 * 提取方法上的请求头信息
	 * 
	 * @param headVos
	 * @param method
	 * @return
	 */
	private static List<HeadVo> extractMethodHeaderInfo(List<HeadVo> headVos, Method method) {
		List<HeadVo> methodHeads = extractHeaderInfo(method);
		// 判断要不要取消 Controller 上的请求头信息
		if (null == method.getAnnotation(ApiHeaderCancel.class) && null != headVos && headVos.size() > 0) {
			// System.out.println("拼接");
			if (null == methodHeads) {
				// methodHeads = headVos;
				methodHeads = new ArrayList<HeadVo>();
				// BeanUtils.copyProperties(methodHeads, headVos);
			}
			// {
			for (HeadVo vo : headVos) {
				HeadVo hv = new HeadVo();
				BeanUtils.copyProperties(vo, hv);
				methodHeads.add(hv);
			}
			// }

		}
		return methodHeads;
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
					// 保存到列表
					headVos.add(head);
				}
			}
		}

		ApiHeadParam ahp = cla.getAnnotation(ApiHeadParam.class);
		if (null != ahp) {

			if (null == headVos) {
				headVos = new ArrayList<HeadVo>();
			}

			HeadVo vo = new HeadVo();
			vo.setDescribe(ahp.describe());
			vo.setField(ahp.value());

			vo.setRequired(ahp.required());
			vo.setType(ahp.type());

			headVos.add(vo);
		}
		return headVos;
	}

	/**
	 * 提取方法上面请求头信息
	 * 
	 * @param method
	 * @return
	 */
	private static List<HeadVo> extractHeaderInfo(Method method) {

		ApiHeader ah = method.getAnnotation(ApiHeader.class);
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
					// 保存到列表
					headVos.add(head);
				}
			}
		}

		ApiHeadParam ahp = method.getAnnotation(ApiHeadParam.class);
		if (null != ahp) {

			if (null == headVos) {
				headVos = new ArrayList<HeadVo>();
			}

			HeadVo vo = new HeadVo();
			vo.setDescribe(ahp.describe());
			vo.setField(ahp.value());

			vo.setRequired(ahp.required());
			vo.setType(ahp.type());

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
	private static ControllerVo extractControllerInfo(Class<?> cla, String base_path) {
		ControllerVo cvo = new ControllerVo();
		// 名称
		cvo.setController(cla.getName().substring(cla.getName().lastIndexOf(".") + 1));
		// System.out.println("Controller信息："+cvo.getController());
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
				// rquestMapping.method();
				// System.out.println(rquestMapping.method()[0]);

				cvo.setMethod(rquestMapping.method()[0].toString().toUpperCase());
			}
		}

		return cvo;
	}

	/**
	 * 提取方法上的信息及请求参数属性信息
	 * 
	 * @param methodType
	 * @param method
	 * @return
	 */
	private static InterfaceVo extractMethodInfo(String methodType, Method method) {

		InterfaceVo ivo = new InterfaceVo();

		RequestMapping rm = method.getAnnotation(RequestMapping.class);
		// 没有 RequestMapper, 则不处理
		if (null == rm) {
			return null;
		}
		// 提取路径
		ArrayList<String> uris = new ArrayList<String>();
		if (null != rm.path() && rm.path().length > 0) {
			for (String path : rm.path()) {
				uris.add(path);
			}
		} else if (null != rm.value() && rm.value().length > 0) {
			for (String path : rm.value()) {
				uris.add(path);
			}
		}
		if (uris.size() > 0) {
			ivo.setUris(uris);
		}

		// 请求方式,转大写
		if (null != rm.method() && rm.method().length > 0) {
			StringBuffer sb = new StringBuffer();
			for (RequestMethod me : rm.method()) {
				sb.append(" , ");
				sb.append(me.toString().toUpperCase());
			}
			methodType = sb.toString().substring(3);
		}
		ivo.setMethod(methodType);

		ApiMethod am = method.getAnnotation(ApiMethod.class);

		if (null == am) {
			ivo.setName(method.getName());
			ivo.setVersion("V1.0");
		}

		if (null != am) {

			ivo.setAuthor(am.author());
			ivo.setDescribe(am.describe());
			ivo.setName(am.value());
			ivo.setVersion(am.version());
		}

		if (method.getParameterTypes().length < 1) {
			// 没有参数
			return ivo;
		}
		Integer index = null;
		// 判断有没有加 json 请求注解
		if (null != method.getParameterAnnotations() && method.getParameterAnnotations().length >= 1) {
			for (int i = 0; i < method.getParameterAnnotations().length; i++) {
				Annotation[] ans = method.getParameterAnnotations()[i];
				for (Annotation annotation : ans) {
					// System.out.println(annotation);
					if (annotation instanceof RequestBody) {
						ivo.setJson(true);
						// JSON 只能使用 post
						ivo.setMethod("POST");
						index = i;
						break;
					}
				}
				if (null != index) {
					break;
				}
			}
		}

		ApiParams apiParams = method.getAnnotation(ApiParams.class);

		// 扫描
		List<ParameterVo> paramterVos = MethodFieldUtil.extractMothodParamerField(method, index,
				null != apiParams ? apiParams.value() : null);

		// 判断参数类型
		if (null != index) {
			ivo.setParamArray(MethodFieldUtil.checkIsArray(method.getParameterTypes()[index]));
		} else {
			ivo.setParamArray(false);
		}

		if (null != am && null != am.hides() && am.hides().length > 0) {
			// 删除隐藏的
			for (String field : am.hides()) {
				removeField(paramterVos, field);
			}

		}
		ivo.setParameters(paramterVos);
		return ivo;
	}

}
