package com.github.ddm4j.api.document.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.github.ddm4j.api.document.common.model.LMREntity;
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
		ApiParams aps = AnnotationUtils.getAnnotation(method, ApiParams.class);

		if (null != aps) {
			// ApiParams params = method.getAnnotation(ApiParams.class);
			// if (null != params) {
			apiParams = aps.value();
			// }
		} else {
			ApiParam param = method.getAnnotation(ApiParam.class);
			if (null != param) {
				apiParams = new ApiParam[1];
				apiParams[0] = param;
			}
		}

		LMREntity<List<ParameterVo>, List<HeadVo>, List<HeadVo>> lmr = extrad(method);
		if (null == lmr) {
			return ivo;
		}

		ivo.setJsonMethod(jsonMethod);
		// 请求参数
		List<ParameterVo> list = lmr.getLeft();

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

		// uri 参数
		List<HeadVo> uriVos = lmr.getMiddle();
		if (null != uriVos && uriVos.size() > 0) {
			if (null != apiParams && apiParams.length > 0) {
				for (ApiParam param : apiParams) {
					for (HeadVo uriVo : uriVos) {
						if (uriVo.getField().equals(param.field())) {
							if (!FieldUtil.isEmpty(param.describe())) {
								uriVo.setDescribe(param.describe());
							}
							uriVo.setRequired(param.required());

							uriVo.setMax(2147483647 == param.max() ? null : param.max());
							uriVo.setMin(-2147483648 == param.min() ? null : param.min());

							uriVo.setRegexp(getRegexp(param.regexp()));
						}
					}
				}
			}

			ivo.setUriParams(uriVos);
		}

		// 请求头参数
		List<HeadVo> headVos = lmr.getRight();
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
		}

		return ivo;
	}

	/**
	 * 删除全部清空，只剩下被 ApiParam 标识的
	 * 
	 * @param list      字段
	 * @param apiParams 注解集合
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
	 * @param list  子集合
	 * @param keys  标识 field
	 * @param index keys 索引
	 * @return 处理后的集合
	 */
	private <T extends ParamChildrenVo> List<T> removeNotApiParam(List<T> list, ApiParam[] apiParams, int index) {
		List<T> vos = new ArrayList<T>();
		String[] keys = null;
		boolean isOk = false;
		for (int i = 0; i < list.size(); i++) {
			T vo = list.get(i);
			isOk = false;
			for (ApiParam param : apiParams) {
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
		Set<String> types = new TreeSet<String>();
		Set<String> uris = new TreeSet<String>();
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
			} else if (at instanceof PatchMapping) {
				PatchMapping dm = (PatchMapping) at;
				if (null != dm.path() && dm.path().length > 0) {
					for (String path : dm.path()) {
						uris.add(path);
					}
				} else if (null != dm.value() && dm.value().length > 0) {
					for (String path : dm.value()) {
						uris.add(path);
					}
				}
				types.add("PATCH");
			}
			// 其它忽略
		}

		if (uris.size() > 0) {
			ivo.setUris(new ArrayList<String>(uris));
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

	// 提取详细参数<请求参数，uri参数，请求头参数>
	private LMREntity<List<ParameterVo>, List<HeadVo>, List<HeadVo>> extrad(Method method) {

		if (null != method.getParameterAnnotations() && method.getParameterAnnotations().length >= 1) {
			LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
			// 取出参数名
			String[] names = u.getParameterNames(method);

			Type[] types = method.getGenericParameterTypes();
			// 参数
			List<ParameterVo> vos = new ArrayList<ParameterVo>();
			// 请求头
			List<HeadVo> headVos = new ArrayList<HeadVo>();
			// 路径参数
			List<HeadVo> uriVos = new ArrayList<HeadVo>();
			LMREntity<List<ParameterVo>, List<HeadVo>, List<HeadVo>> lmr = new LMREntity<List<ParameterVo>, List<HeadVo>, List<HeadVo>>();
			// KVEntity<List<ParameterVo>, List<HeadVo>> kv = new
			// KVEntity<List<ParameterVo>, List<HeadVo>>();
			lmr.setLeft(vos);
			lmr.setRight(headVos);
			lmr.setMiddle(uriVos);

			// boolean json = false;
			int index = -1;
			for (int i = 0; i < method.getParameterAnnotations().length; i++) {
				Annotation[] ans = method.getParameterAnnotations()[i];
				RequestHeader head = null;
				PathVariable path = null;
				boolean ignore = false;
				boolean url = false;
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

					if (ans[j] instanceof PathVariable) {
						path = (PathVariable) ans[j];
						url = true;
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
				} else if (path != null) {
					for (ParameterVo vo : list) {
						HeadVo headVo = new HeadVo();
						headVo.setDescribe(vo.getDescribe());
						headVo.setField(vo.getField());
						headVo.setRequired(vo.isRequired());
						headVo.setType(vo.getType());
						headVo.setRegexp(vo.getRegexp());
						headVo.setMax(vo.getMax());
						headVo.setMin(vo.getMin());

						uriVos.add(headVo);
						// 指定了名称就只能有一个了
						if (!FieldUtil.isEmpty(path.value()) || !FieldUtil.isEmpty(path.name())) {
							if (!FieldUtil.isEmpty(path.value())) {
								headVo.setField(path.value());
							} else {
								headVo.setField(path.name());
							}
							break;
						}
					}
				} else {
					for (ParameterVo vo : list) {
						if (json && i != index && !url) {
							vo.setGet(true);
						}
						vos.add(vo);
					}
				}
			}
			return lmr;
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
	 * @param param 注解
	 * @param list  返回值对象
	 */
	private void replaceReuestField(ApiParam param, List<ParameterVo> list) {
		String[] keys = param.field().split("\\.");
		ParameterVo tempVo = null;
		List<? extends ParamChildrenVo> tempChildren = list;

		for (String key : keys) {
			for (ParamChildrenVo vo : tempChildren) {
				if (vo.getField().equals(key)) {
					tempVo = (ParameterVo) vo;
					if(param.required()) {
						tempVo.setRequired(param.required());// 为必须
					}
					tempChildren = vo.getChildren();
					break;
				}
			}
		}
		if (tempVo != null) {
			if (!FieldUtil.isEmpty(param.describe())) {
				tempVo.setDescribe(param.describe());
			}
			//tempVo.setRequired(param.required());
			tempVo.setRegexp(getRegexp(param.regexp()));

			// 最大或最小
			tempVo.setMax(2147483647 == param.max() ? null : param.max());
			tempVo.setMin(-2147483648 == param.min() ? null : param.min());
			
			// 所有上级，设置为必须
			if(tempVo.isRequired()) {
				tempVo = null;
				tempChildren = list;
				for (String key : keys) {
					for (ParamChildrenVo vo : tempChildren) {
						if (vo.getField().equals(key)) {
							tempVo = (ParameterVo) vo;
							
							tempChildren = vo.getChildren();
							break;
						}
					}
				}
			}
			
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
