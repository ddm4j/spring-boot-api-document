package com.github.ddm4j.api.document.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import com.github.ddm4j.api.document.annotation.ApiField;
import com.github.ddm4j.api.document.annotation.ApiMark;
import com.github.ddm4j.api.document.annotation.ApiMethod;
import com.github.ddm4j.api.document.annotation.ApiParam;
import com.github.ddm4j.api.document.annotation.ApiParamHides;
import com.github.ddm4j.api.document.annotation.ApiParams;
import com.github.ddm4j.api.document.bean.HeadVo;
import com.github.ddm4j.api.document.bean.InterfaceVo;
import com.github.ddm4j.api.document.bean.ParameterVo;
import com.github.ddm4j.api.document.common.model.FieldType;
import com.github.ddm4j.api.document.common.model.KVEntity;
import com.github.ddm4j.api.document.config.CheckConfig;

public class MethodRequestUtil {
	private CheckConfig config;

	private boolean json = false;

	public MethodRequestUtil(CheckConfig config) {
		this.config = config;
	}

	public InterfaceVo getRequestVo(Method method, String methodType) {
		json = false;
		// 1 获取路径注解
		RequestMapping rm = method.getAnnotation(RequestMapping.class);
		if (null == rm) {
			// 没有，注解不扫描
			return null;
		}
		InterfaceVo ivo = new InterfaceVo();
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

		// 描述注解
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

		ApiParams apiParams = method.getAnnotation(ApiParams.class);

		KVEntity<List<ParameterVo>, List<HeadVo>> kv = extrad(method);
		List<ParameterVo> list = kv.getKey();

		if (null != list && list.size() > 0) {
			// 删除隐藏的
			ApiParamHides hides = method.getAnnotation(ApiParamHides.class);
			if (null != hides && null != hides.value() && hides.value().length > 0) {
				for (String field : hides.value()) {
					FieldUtil.removeField(list, field);
				}
			}
			// 注解替换
			if (null != apiParams && apiParams.value().length > 0) {
				for (ApiParam param : apiParams.value()) {
					replaceReuestField(param, list);
				}
			}
		}
		ivo.setParameters(list);
		// 请头参数
		List<HeadVo> headVos = kv.getValue();
		if (null != headVos && headVos.size() > 0) {
			if (null != apiParams && apiParams.value().length > 0) {
				for (ApiParam param : apiParams.value()) {
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
			ivo.setMethod("post");
		}

		return ivo;
	}

	private KVEntity<List<ParameterVo>, List<HeadVo>> extrad(Method method) {

		if (null != method.getParameterAnnotations() && method.getParameterAnnotations().length >= 1) {
			LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
			// 取出参数名
			String[] names = u.getParameterNames(method);
			// 取出参数
			Class<?>[] clas = method.getParameterTypes();
			Type[] types = method.getGenericParameterTypes();
			// 参数
			List<ParameterVo> vos = new ArrayList<ParameterVo>();
			// 请求头
			List<HeadVo> headVos = new ArrayList<HeadVo>();

			KVEntity<List<ParameterVo>, List<HeadVo>> kv = new KVEntity<List<ParameterVo>, List<HeadVo>>();
			kv.setKey(vos);
			kv.setValue(headVos);

			// boolean json = false;
			int index = -1;
			for (int i = 0; i < method.getParameterAnnotations().length; i++) {
				Annotation[] ans = method.getParameterAnnotations()[i];
				RequestHeader head = null;
				boolean isOK = false;
				for (int j = 0; j < ans.length; j++) {
					if (ans[j] instanceof RequestBody) {
						json = true;
						index = i;
					}

					if (ans[j] instanceof RequestHeader) {
						head = (RequestHeader) ans[j];
					}

					if (ans[j] instanceof ApiMark) {
						isOK = true;
					}
				}

				if (isOK) {
					Type genType = types[i];

					if (null != genType) {
						FieldType type = FieldUtil.checkFieldType(genType);
						genType = FieldUtil.extractGenType(genType, null, type);
					}

					List<ParameterVo> list = extractField(clas[i], names[i], genType);
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

			}
			return kv;
		}
		return null;
	}

	private List<ParameterVo> extractField(Class<?> cla, String name, Type genType) {

		List<ParameterVo> vos = new ArrayList<ParameterVo>();

		ParameterVo vo = null;
		
		if(Date.class.isAssignableFrom(cla)) {
			vo = new ParameterVo();
			vo.setField(name);
			vo.setDescribe(name);
			vo.setType("date");
			vos.add(vo);
			return vos;
		}else
		// 判断是不是接口类型
		if (cla.isInterface()) {
			// 是接口
			if (cla.isAssignableFrom(MultipartFile.class)) {
				vo = new ParameterVo();
				vo.setField(name);
				vo.setDescribe(name);
				vo.setType("File");
				vos.add(vo);
				return vos;
			} else if (List.class.isAssignableFrom(cla) || Set.class.isAssignableFrom(cla)) {
				List<ParameterVo> vos2 = extractField((Class<?>) genType, name, genType);
				if (null != vos2 && vos2.size() > 0) {
					vo = new ParameterVo();
					vo.setField(name);
					vo.setDescribe(name);
					vo.setType("Array<Object>");
					vo.setChildren(vos2);

					vos.add(vo);
				}

				return vos;
			}

		} else if (cla.isArray()) {
			cla = cla.getComponentType();
			if (Number.class.isAssignableFrom(cla)) {
				vo = new ParameterVo();
				vo.setField(name);
				vo.setDescribe(name);
				vo.setType("Array<Number>");
				vos.add(vo);
				return vos;
			} else if (cla == String.class) {
				vo = new ParameterVo();
				vo.setField(name);
				vo.setDescribe(name);
				vo.setType("Array<String>");
				vos.add(vo);
				return vos;
			} else {
				// 不处理
			}
		} else if (Number.class.isAssignableFrom(cla)) {
			vo = new ParameterVo();
			vo.setField(name);
			vo.setDescribe(name);
			vo.setType("Number");
			vos.add(vo);
			return vos;
		} else if (cla == String.class) {
			vo = new ParameterVo();
			vo.setField(name);
			vo.setDescribe(name);
			vo.setType("String");
			vos.add(vo);
			return vos;
		} else {
			// 自定义类
			vos = getRequestFields(cla, genType);
		}

		// 其他不处理
		return vos.size() > 0 ? vos : null;

	}

	public List<ParameterVo> getRequestFields(Class<?> cla, Type genType) {

		if (null == cla) {
			return null;
		}

		List<ParameterVo> list = new ArrayList<ParameterVo>();

		Field[] fis = cla.getDeclaredFields();
		for (Field field : fis) {

			FieldType type = FieldUtil.checkFieldType(field.getGenericType());
			ParameterVo vo = null;
			Class<?> cla2 = null;
			switch (type) {
			case Clazz:// OK
				vo = getRequestFieldInfo(field, field.getType(), false, null, type);
				break;
			case ArrayClass: // OK
				cla2 = FieldUtil.extractClassByType(field.getGenericType(), null, type);
				// 获取
				vo = getRequestFieldInfo(field, cla2, true, null, type);
				break;
			case ClassT: // OK
				// list和set 需要特殊处理
				if (List.class.isAssignableFrom(field.getType()) || Set.class.isAssignableFrom(cla)) {
					Type gen = FieldUtil.extractClassByType(field.getGenericType(), genType, type);

					if (null == gen) {
						type = FieldUtil.checkFieldType(genType);
						gen = FieldUtil.extractClassByType(genType, genType, type);
					}

					if (null != gen) {
						cla2 = (Class<?>) gen;
						vo = getRequestFieldInfo(field, cla2, true, null, type);
						// type = FieldType.ArrayT;
					}
				} else {
					cla2 = field.getType();
					vo = getRequestFieldInfo(field, cla2, false, genType, type);
				}

				break;
			case ArrayClassT: //
				vo = getRequestFieldInfo(field, field.getType(), false, genType, type);
				break;
			case T: // OK
				if (null != genType) {
					cla2 = FieldUtil.extractClassByType(field.getGenericType(), genType, type);
					vo = getRequestFieldInfo(field, cla2, false, genType, type);
				}
				break;
			case ArrayT:
				if (null != genType) {
					cla2 = FieldUtil.extractClassByType(field.getGenericType(), genType, type);
					type = FieldType.T;
					vo = getRequestFieldInfo(field, cla2, true, genType, type);
				}

				break;
			}

			if (null != vo) {
				list.add(vo);
			}

		}

		if (Object.class != cla.getSuperclass()) {
			List<ParameterVo> list2 = getRequestFields(cla.getSuperclass(), genType);
			if (null != list2)
				for (ParameterVo field : list2) {
					list.add(field);
				}
		}

		return list;
	}

	private ParameterVo getRequestFieldInfo(Field field, Class<?> cla2, boolean array, Type genType, FieldType type) {// 获取上面的注解
		ApiField apiField = field.getAnnotation(ApiField.class);
		// 判断是不是隐藏了
		if (null != apiField && apiField.hide()) {
			return null;
		}

		ParameterVo vo = new ParameterVo();
		if (null != apiField) {
			vo.setDescribe(apiField.value());
		}
		vo.setField(field.getName());

		switch (FieldUtil.checkBaseClass(cla2)) {
		case -2:
			return null;
		case 1:// Number
			if (array) {
				vo.setType("Array<Number>");
			} else {
				vo.setType("Number");
			}
			break;
		case 2:// String
			if (array) {
				vo.setType("Array<String>");
			} else {
				vo.setType("String");
			}
			break;
		case 3:// Enum
			if (array) {
				vo.setType("Array<Enum/Number>");
			} else {
				vo.setType("Enum/Number");
			}
			break;
		case 0:// 接口，未知
			if (array) {
				vo.setType("Array<Other>");
			} else {
				vo.setType("Other");
			}
		case 7:// 日期类型
			if (array) {
				vo.setType("Array<Date>");
			} else {
				vo.setType("Date");
			}
			break;
		default:// bean
			if (array) {
				vo.setType("Array<Object>");
			} else {
				vo.setType("Object");
			}
			switch (type) {
			case T:
			case ArrayT:
			case ClassT:
			case ArrayClassT:
				// 取值
				genType = FieldUtil.extractGenType(field.getGenericType(), genType, type);
				break;
			case Clazz:
			case ArrayClass:
				genType = null;
				break;

			}

			List<ParameterVo> children = null;
			switch (type) {
			case Clazz:// OK
				children = getRequestFields(cla2, genType);
				break;
			case ArrayClass:
				cla2 = FieldUtil.extractClassByType(field.getGenericType(), genType, type);
				if (null != cla2) {
					children = getRequestFields(cla2, genType);
				}
				break;
			case T:// OK
			case ArrayT:// OK
				if (null != cla2) {
					children = getRequestFields(cla2, genType);
				}
				break;
			default: // OK
				children = getRequestFields(cla2, genType);
				break;

			}
			if (null != children) {
				vo.setChildren(children);
			}
		}
		return vo;
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
		List<ParameterVo> tempChildren = list;

		for (String key : keys) {
			for (ParameterVo vo : tempChildren) {
				if (vo.getField().equals(key)) {
					tempVo = vo;
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
