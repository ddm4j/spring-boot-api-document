package com.github.ddm4j.api.document.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.ddm4j.api.document.annotation.ApiIgnore;
import com.github.ddm4j.api.document.annotation.ApiMethod;
import com.github.ddm4j.api.document.annotation.ApiParam;
import com.github.ddm4j.api.document.annotation.ApiParamIgnore;
import com.github.ddm4j.api.document.annotation.ApiParams;
import com.github.ddm4j.api.document.bean.HeadVo;
import com.github.ddm4j.api.document.bean.InterfaceVo;
import com.github.ddm4j.api.document.bean.ParamChildrenVo;
import com.github.ddm4j.api.document.bean.ParameterVo;
import com.github.ddm4j.api.document.common.model.FieldInfo;
import com.github.ddm4j.api.document.common.model.KVEntity;
import com.github.ddm4j.api.document.config.CheckConfig;

public class MethodRequestUtil {
	private CheckConfig config;

	private boolean json = false;
	private String jsonMethod = null;

	public MethodRequestUtil(CheckConfig config) {
		this.config = config;
	}

	public InterfaceVo getRequestVo(Method method, String methodType) {
		json = false;
		jsonMethod = null;
		// 1 获取路径注解
		InterfaceVo ivo = extractUriAndType(method, methodType);

		if (null == ivo) {
			return null;
		}

		// 描述注解
		ApiMethod am = AnnotationUtils.getAnnotation(method, ApiMethod.class);
		if (null == am) {
			ivo.setName(method.getName());
			// ivo.setVersion("V1.0");
		}

		if (null != am) {
			ivo.setAuthor(am.author());
			ivo.setDescribe(am.describe());
			ivo.setName(am.value());
			ivo.setVersion(am.version());
		}

		// ApiParam[] apiParams = method.getAnnotationsByType(ApiParam.class);
		ApiParam[] apiParams = new ApiParam[0];
		ApiParams aps = AnnotationUtils.getAnnotation(method,ApiParams.class);
		
		if (null != aps) {
			//ApiParams params = method.getAnnotation(ApiParams.class);
			//if (null != params) {
				apiParams = aps.value();
			//}
		}

		KVEntity<List<ParameterVo>, List<HeadVo>> kv = extrad(method);
		if (null == kv) {
			return ivo;
		}

		ivo.setJsonMethod(jsonMethod);

		List<ParameterVo> list = kv.getLeft();

		if (null != list && list.size() > 0) {
			// 删除隐藏的
			ApiParamIgnore hides = method.getAnnotation(ApiParamIgnore.class);
			if (null != hides) {
				// 判断是不是只保留，被 ApiParam 标识的
				if (null == hides.value() || hides.value().length == 0) {
					list = removeNotApiParam(list, apiParams);
				} else {
					// 删除指定的
					for (String field : hides.value()) {
						FieldUtil.removeField(list, field);
					}
				}
			}
			// 注解替换
			if (null != apiParams && apiParams.length > 0) {
				for (ApiParam param : apiParams) {
					replaceReuestField(param, list);
				}
			}
		}
		ivo.setParameters(list);
		// 请头参数
		List<HeadVo> headVos = kv.getRight();
		if (null != headVos && headVos.size() > 0) {
			if (null != apiParams && apiParams.length > 0) {
				for (ApiParam param : apiParams) {
					for (HeadVo headVo : headVos) {
						if (headVo.getField().equals(param.field())) {
							if (!FieldUtil.isEmpty(param.describe())) {
								headVo.setDescribe(param.describe());
							}
							headVo.setRequired(param.required());

							headVo.setMax(2147483647 == param.max() ? null : param.max());
							headVo.setMin(-2147483648 == param.min() ? null : param.min());

							headVo.setRegexp(getRegexp(param.regexp()));
						}
					}
				}
			}

			ivo.setHeads(headVos);
		}
		// 判断是不是 json 请求
		if (json) {
			ivo.setJson(true);
			ivo.setMethod("POST");
		}

		return ivo;
	}

	/**
	 * 删除全部清空，只剩下被 ApiParam 标识的
	 * 
	 * @param list
	 *            字段
	 * @param apiParams
	 *            注解集合
	 * @return 处理后的数据
	 */
	private List<ParameterVo> removeNotApiParam(List<ParameterVo> list, ApiParam[] apiParams) {
		List<ParameterVo> vos = new ArrayList<ParameterVo>();
		String[] keys = null;
		boolean isOk = false;
		for (int i = 0; i < list.size(); i++) {
			ParameterVo vo = list.get(i);
			isOk = false;
			for (ApiParam param : apiParams) {
				keys = param.field().split("\\.");
				isOk = keys[0].equals(vo.getField());
				if (isOk && keys.length > 1 && null != vo.getChildren() && vo.getChildren().size() > 0) {
					list.get(i).setChildren(removeNotApiParam(vo.getChildren(), apiParams, 1));
				}
				if (isOk) {
					break;
				}
			}
			if (isOk) {
				vos.add(vo);
			}

		}
		return vos;
	}

	/**
	 * 删除全部清空，只剩下被 ApiParam 标识的，子集合
	 * 
	 * @param list
	 *            子集合
	 * @param keys
	 *            标识 field
	 * @param index
	 *            keys 索引
	 * @return 处理后的集合
	 */
	private <T extends ParamChildrenVo> List<T> removeNotApiParam(List<T> list, ApiParam[] apiParams,
			int index) {
		List<T> vos = new ArrayList<T>();
		String[] keys = null;
		boolean isOk = false;
		for (int i = 0; i < list.size(); i++) {
			T vo = list.get(i);
			isOk = false;
			for(ApiParam param:apiParams) {
				keys = param.field().split("\\.");
				if (index <= keys.length - 1) {
					isOk = keys[index].equals(vo.getField());
					if (isOk && index < keys.length - 2 && null != vo.getChildren() && vo.getChildren().size() > 0) {
						vo.setChildren(removeNotApiParam(vo.getChildren(), apiParams, index + 1));
					}
					if (isOk) {
						break;
					}
				}
			}
			if (isOk) {
				vos.add(vo);
			}
		}
		return vos;
	}

	// 提取url和请求方式
	private InterfaceVo extractUriAndType(Method method, String methodType) {

		InterfaceVo ivo = new InterfaceVo();

		// 提取路径
		ArrayList<String> types = new ArrayList<String>();
		ArrayList<String> uris = new ArrayList<String>();
		for (Annotation at : method.getAnnotations()) {
			if (at instanceof RequestMapping) {
				RequestMapping rm = (RequestMapping) at;
				if (null != rm.path() && rm.path().length > 0) {
					for (String path : rm.path()) {
						uris.add(path);
					}
				} else if (null != rm.value() && rm.value().length > 0) {
					for (String path : rm.value()) {
						uris.add(path);
					}
				}
				// 请求方式,转大写
				if (null != rm.method() && rm.method().length > 0) {
					for (RequestMethod me : rm.method()) {
						types.add(me.toString().toUpperCase());
					}
				}

			} else if (at instanceof DeleteMapping) {
				DeleteMapping dm = (DeleteMapping) at;
				if (null != dm.path() && dm.path().length > 0) {
					for (String path : dm.path()) {
						uris.add(path);
					}
				} else if (null != dm.value() && dm.value().length > 0) {
					for (String path : dm.value()) {
						uris.add(path);
					}
				}
				types.add("DELETE");
			} else if (at instanceof GetMapping) {
				GetMapping dm = (GetMapping) at;
				if (null != dm.path() && dm.path().length > 0) {
					for (String path : dm.path()) {
						uris.add(path);
					}
				} else if (null != dm.value() && dm.value().length > 0) {
					for (String path : dm.value()) {
						uris.add(path);
					}
				}
				types.add("GET");
			} else if (at instanceof PostMapping) {
				PostMapping dm = (PostMapping) at;
				if (null != dm.path() && dm.path().length > 0) {
					for (String path : dm.path()) {
						uris.add(path);
					}
				} else if (null != dm.value() && dm.value().length > 0) {
					for (String path : dm.value()) {
						uris.add(path);
					}
				}
				types.add("POST");
			} else if (at instanceof PutMapping) {
				PutMapping dm = (PutMapping) at;
				if (null != dm.path() && dm.path().length > 0) {
					for (String path : dm.path()) {
						uris.add(path);
					}
				} else if (null != dm.value() && dm.value().length > 0) {
					for (String path : dm.value()) {
						uris.add(path);
					}
				}
				types.add("PUT");
			}
			// 其它忽略
		}

		if (uris.size() > 0) {
			ivo.setUris(uris);
		} else {
			// 没有返回空
			return null;
		}

		if (types.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (String name : types) {
				sb.append(name + ",");
			}
			ivo.setMethod(sb.toString().substring(0, sb.toString().length() - 1));
		} else {
			// 接口上没有找到，用 Controller 的
			ivo.setMethod(methodType);
		}

		return ivo;
	}

	// 提取详细参数
	private KVEntity<List<ParameterVo>, List<HeadVo>> extrad(Method method) {

		if (null != method.getParameterAnnotations() && method.getParameterAnnotations().length >= 1) {
			LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
			// 取出参数名
			String[] names = u.getParameterNames(method);

			Type[] types = method.getGenericParameterTypes();
			// 参数
			List<ParameterVo> vos = new ArrayList<ParameterVo>();
			// 请求头
			List<HeadVo> headVos = new ArrayList<HeadVo>();

			KVEntity<List<ParameterVo>, List<HeadVo>> kv = new KVEntity<List<ParameterVo>, List<HeadVo>>();
			kv.setLeft(vos);
			kv.setRight(headVos);

			// boolean json = false;
			int index = -1;
			for (int i = 0; i < method.getParameterAnnotations().length; i++) {
				Annotation[] ans = method.getParameterAnnotations()[i];
				RequestHeader head = null;

				boolean ignore = false;
				for (int j = 0; j < ans.length; j++) {
					if (ans[j] instanceof RequestBody) {
						json = true;
						index = i;
					}

					if (ans[j] instanceof ApiIgnore) {
						ignore = true;
						break;
					}

					if (ans[j] instanceof RequestHeader) {
						head = (RequestHeader) ans[j];
					}
				}
				// 忽略了，下一个
				if (ignore) {
					continue;
				}

				List<ParameterVo> list = extractField(types[i], names[i]);
				if (null == list) {
					continue;
				}
				if (null != head) {

					for (ParameterVo vo : list) {
						HeadVo headVo = new HeadVo();
						headVo.setDescribe(vo.getDescribe());
						headVo.setField(vo.getField());
						headVo.setRequired(vo.isRequired());
						headVo.setType(vo.getType());
						headVo.setRegexp(vo.getRegexp());
						headVo.setMax(vo.getMax());
						headVo.setMin(vo.getMin());

						if (head.required()) {
							headVo.setRequired(true);
						}
						headVos.add(headVo);
						// 指定了名称就只能有一个了
						if (!FieldUtil.isEmpty(head.value()) || !FieldUtil.isEmpty(head.name())) {
							if (!FieldUtil.isEmpty(head.value())) {
								headVo.setField(head.value());
							} else {
								headVo.setField(head.name());
							}
							break;
						}
					}

				} else {
					for (ParameterVo vo : list) {
						if (json && i != index) {
							vo.setUrl(true);
						}
						vos.add(vo);
					}
				}
			}
			return kv;
		}
		return null;
	}

	private List<ParameterVo> extractField(Type type, String name) {
		KVEntity<String, List<FieldInfo>> kv = FieldUtil.extract(type);
		if (null == kv) {
			return null;
		}

		if (FieldUtil.isEmpty(kv.getLeft())) {
			return null;
		}

		if (null == jsonMethod && json) {
			jsonMethod = kv.getLeft();
		}

		if (null == kv.getRight() || kv.getRight().size() == 0) {

			List<ParameterVo> vos = new ArrayList<ParameterVo>();

			ParameterVo vo = new ParameterVo();
			vo.setField(name);
			vo.setType(kv.getLeft());
			vos.add(vo);

			return vos;
		} else {
			return extractField(kv.getRight());
		}

	}

	public List<ParameterVo> extractField(List<FieldInfo> infos) {
		List<ParameterVo> vos = new ArrayList<ParameterVo>();
		for (FieldInfo info : infos) {
			ParameterVo vo = new ParameterVo();
			vo.setField(info.getName());
			vo.setType(info.getType());
			vo.setDescribe(info.getDescribe());
			vo.setFieldName(info.getField());// 字段名
			if (null != info.getChildren() && info.getChildren().size() > 0) {
				vo.setChildren(extractField(info.getChildren()));
			}
			vos.add(vo);
		}
		return vos;
	}

	/**
	 * 参数注解替换
	 * 
	 * @param param
	 *            注解
	 * @param list
	 *            返回值对象
	 */
	private void replaceReuestField(ApiParam param, List<ParameterVo> list) {
		String[] keys = param.field().split("\\.");
		ParameterVo tempVo = null;
		List<? extends ParamChildrenVo> tempChildren = list;

		for (String key : keys) {
			for (ParamChildrenVo vo : tempChildren) {
				if (vo.getField().equals(key)) {
					tempVo = (ParameterVo) vo;
					tempChildren = vo.getChildren();
					break;
				}
			}
		}
		if (tempVo != null) {
			if (!FieldUtil.isEmpty(param.describe())) {
				tempVo.setDescribe(param.describe());
			}
			tempVo.setRequired(param.required());
			tempVo.setRegexp(getRegexp(param.regexp()));

			// 最大或最小
			tempVo.setMax(2147483647 == param.max() ? null : param.max());
			tempVo.setMin(-2147483648 == param.min() ? null : param.min());
		}
	}

	// 获取正则表达式
	private String getRegexp(String regexp) {
		if (regexp.startsWith("${") && regexp.endsWith("}")) {
			String key = regexp.substring(2, regexp.length() - 1);
			String reg = config.getRegexps().get(key);
			if (FieldUtil.isEmpty(reg)) {
				// logger.error("参数校验：" + regexp + " 正则表达式");
				return null;
			}
			return reg;
		}
		return regexp;
	}

}
