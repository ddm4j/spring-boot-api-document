package com.github.ddm4j.api.document.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.ddm4j.api.document.annotation.ApiField;
import com.github.ddm4j.api.document.annotation.ApiResponseHides;
import com.github.ddm4j.api.document.annotation.ApiResponseParam;
import com.github.ddm4j.api.document.annotation.ApiResponses;
import com.github.ddm4j.api.document.bean.ResponseVo;
import com.github.ddm4j.api.document.common.model.FieldType;

public class MethodResponseUtil {

	public List<ResponseVo> getResponseVo(Method method) {
		// 提取返回值注解
		ApiResponses response = method.getAnnotation(ApiResponses.class);

		Type genType = method.getGenericReturnType();

		if (null != genType) {
			FieldType type = FieldUtil.checkFieldType(genType);
			genType = FieldUtil.extractGenType(genType, null, type);
		}

		List<ResponseVo> list = getResponseFields(method.getReturnType(), genType);

		// 删除隐藏的
		ApiResponseHides hides = method.getAnnotation(ApiResponseHides.class);
		if (null != hides && null != hides.value() && hides.value().length > 0) {
			for (String field : hides.value()) {
				FieldUtil.removeField(list, field);
			}
		}
		// 注解替换
		if (null != response && response.value().length > 0) {
			for (ApiResponseParam param : response.value()) {
				replaceResponseField(param, list);
			}
		}

		return list;
	}

	/**
	 * 返回值注解替换
	 * 
	 * @param param
	 *            注解
	 * @param list
	 *            返回值对象
	 */
	private void replaceResponseField(ApiResponseParam param, List<ResponseVo> list) {
		String[] keys = param.field().split("\\.");
		ResponseVo tempVo = null;
		List<ResponseVo> tempChildren = list;

		for (String key : keys) {
			for (ResponseVo vo : tempChildren) {
				if (vo.getField().equals(key)) {
					tempVo = vo;
					tempChildren = vo.getChildren();
					break;
				}
			}
		}
		if (tempVo != null) {
			tempVo.setRequired(param.required());
			if (!FieldUtil.isEmpty(param.describe())) {
				tempVo.setDescribe(param.describe());
			}
		}
	}

	private List<ResponseVo> getResponseFields(Class<?> cla, Type genType) {

		if (null == cla) {
			return null;
		}

		List<ResponseVo> list = new ArrayList<ResponseVo>();

		Field[] fis = cla.getDeclaredFields();
		for (Field field : fis) {

			FieldType type = FieldUtil.checkFieldType(field.getGenericType());
			ResponseVo vo = null;
			Class<?> cla2 = null;
			switch (type) {
			case Clazz:// OK
				vo = getResponseFieldInfo(field, field.getType(), false, null, type);
				break;
			case ArrayClass: // OK
				cla2 = FieldUtil.extractClassByType(field.getGenericType(), null, type);
				// 获取
				vo = getResponseFieldInfo(field, cla2, true, null, type);
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
						vo = getResponseFieldInfo(field, cla2, true, null, type);
					}
				} else {
					cla2 = field.getType();
					vo = getResponseFieldInfo(field, cla2, false, genType, type);
				}

				break;
			case ArrayClassT: //
				vo = getResponseFieldInfo(field, field.getType(), false, genType, type);
				break;
			case T: // OK
				if (null != genType) {
					cla2 = FieldUtil.extractClassByType(field.getGenericType(), genType, type);
					vo = getResponseFieldInfo(field, cla2, false, genType, type);
				}
				break;
			case ArrayT:
				if (null != genType) {
					cla2 = FieldUtil.extractClassByType(field.getGenericType(), genType, type);
					type = FieldType.T;
					vo = getResponseFieldInfo(field, cla2, true, genType, type);
				}

				break;
			}

			if (null != vo) {
				list.add(vo);
			}

		}

		if (Object.class != cla.getSuperclass()) {
			List<ResponseVo> list2 = getResponseFields(cla.getSuperclass(), genType);
			if (null != list2)
				for (ResponseVo field : list2) {
					list.add(field);
				}
		}

		return list;
	}

	private ResponseVo getResponseFieldInfo(Field field, Class<?> cla2, boolean array, Type genType, FieldType type) {
		// 获取上面的注解
		ApiField apiField = field.getAnnotation(ApiField.class);
		// 判断是不是隐藏了
		if (null != apiField && apiField.hide()) {
			return null;
		}

		ResponseVo vo = new ResponseVo();
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
			break;
		case 7: // 日期类型
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

			List<ResponseVo> children = null;
			switch (type) {
			case Clazz:// OK
				children = getResponseFields(cla2, genType);
				break;
			case ArrayClass:
				cla2 = FieldUtil.extractClassByType(field.getGenericType(), genType, type);
				if (null != cla2) {
					children = getResponseFields(cla2, genType);
				}
				break;
			case T:// OK
			case ArrayT:// OK
				if (null != cla2) {
					children = getResponseFields(cla2, genType);
				}
				break;
			default: // OK
				children = getResponseFields(cla2, genType);
				break;

			}
			if (null != children) {
				vo.setChildren(children);
			}
		}
		return vo;
	}

}
