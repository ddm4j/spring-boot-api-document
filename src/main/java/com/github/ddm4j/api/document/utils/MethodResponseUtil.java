package com.github.ddm4j.api.document.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.AnnotationUtils;

import com.github.ddm4j.api.document.annotation.ApiResponseIgnore;
import com.github.ddm4j.api.document.annotation.ApiResponse;
import com.github.ddm4j.api.document.annotation.ApiResponseCode;
import com.github.ddm4j.api.document.annotation.ApiResponses;
import com.github.ddm4j.api.document.bean.ParamChildrenVo;
import com.github.ddm4j.api.document.bean.ResponseVo;
import com.github.ddm4j.api.document.common.model.FieldInfo;
import com.github.ddm4j.api.document.common.model.KVEntity;

public class MethodResponseUtil {

	public KVEntity<String, List<ResponseVo>> getResponseVo(Method method) {

		// 提取返回值注解
		// ApiResponse[] responses = method.getAnnotationsByType(ApiResponse.class);
		ApiResponse[] responses = new ApiResponse[0];
		ApiResponses responsess = AnnotationUtils.getAnnotation(method, ApiResponses.class);
		if (null != responsess) {
			// ApiResponses params = method.getAnnotation(ApiResponses.class);
			// if (null != params) {
			responses = responsess.value();
			// }
		}

		KVEntity<String, List<FieldInfo>> kv = FieldUtil.extract(method.getGenericReturnType());
		if (null == kv) {
			return null;
		}
		KVEntity<String, List<ResponseVo>> entity = new KVEntity<String, List<ResponseVo>>();
		entity.setLeft(kv.getLeft());
		if (null == kv.getRight() || kv.getRight().size() == 0) {
			return entity;
		}

		// 提取
		List<ResponseVo> list = extractField(kv.getRight());

		// 删除隐藏的
		ApiResponseIgnore hides = method.getAnnotation(ApiResponseIgnore.class);
		if (null != hides) {

			if (null == hides.value() || hides.value().length == 0) {
				// 删除除了被 ApiResponse 标识之外字段
				list = removeNotResponse(list, responses);
			} else {
				// 删除指定的
				for (String field : hides.value()) {
					if (!FieldUtil.isEmpty(field))
						list = FieldUtil.removeField(list, field);
				}
			}
		}
		// code 隐藏或删除
		ApiResponseCode code = method.getAnnotation(ApiResponseCode.class);
		if (null != code && code.codes().length > 0) {
			removeCode(list,code);
		}

		// 注解替换
		if (null != responses && responses.length > 0) {
			for (ApiResponse param : responses) {
				replaceResponseField(param, list);
			}
		}
		entity.setRight(list);

		return entity;
	}

	private List<ResponseVo> removeCode(List<ResponseVo> list, ApiResponseCode code) {
		String[] keys = code.field().split("\\.");
		return removeCode(list,code,keys,0);
	}

	@SuppressWarnings("unchecked")
	private <T extends ParamChildrenVo> List<T> removeCode(List<T> list, ApiResponseCode code, String[] keys, int index) {
		List<T> vos = new ArrayList<T>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getField().equals(keys[index])) {
				if (null != list.get(i).getChildren() && list.get(i).getChildren().size() > 0) {
					boolean isOK = code.hide();// 是否隐藏
					for (int j = 0; j < list.get(i).getChildren().size(); j++) {
						isOK = code.hide();// 是否隐藏
						
						for (String key : code.codes()) {
							index = key.indexOf("*");
							if (index > -1 && index == 0) {
								// 匹配后面
								if(list.get(i).getChildren().get(j).getFieldName().endsWith(key.substring(1))) {
									if(code.hide()) {
										// 如果是隐藏，就不要加入了
										isOK = false;
									}else {
										// 如果是显示，就得加入了
										isOK = true;
									}
								}
							} else if (index > 0) {
								// 匹配后面
								if(list.get(i).getChildren().get(j).getFieldName().startsWith(key.substring(0,key.length()-1))) {
									if(code.hide()) {
										// 如果是隐藏，就不要加入了
										isOK = false;
									}else {
										// 如果是显示，就得加入了
										isOK = true;
									}
								}
							} else {
								// 不匹配
								if(list.get(i).getChildren().get(j).getFieldName().equals(key)) {
									if(code.hide()) {
										// 如果是隐藏，就不要加入了
										isOK = false;
									}else {
										// 如果是显示，就得加入了
										isOK = true;
									}
								}
							}
						}
						if(isOK) {
							vos.add((T) list.get(i).getChildren().get(j));
						}
					}
				}
				// 更新当前字段
				list.get(i).setChildren(vos);
				break;
			}
		}
		return list;
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
	private List<ResponseVo> removeNotResponse(List<ResponseVo> list, ApiResponse[] responses) {

		List<ResponseVo> vos = new ArrayList<ResponseVo>();
		String[] keys = null;
		boolean isOk = false;
		for (int i = 0; i < list.size(); i++) {
			ResponseVo vo = list.get(i);
			isOk = false;
			for (ApiResponse param : responses) {
				keys = param.field().split("\\.");
				// 匹配模式
				isOk = keys[0].equals(vo.getField());
				if (isOk && keys.length > 1 && null != vo.getChildren() && vo.getChildren().size() > 0) {
					vo.setChildren(removeNotResponse(vo.getChildren(), responses, 1));
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
	private <T extends ParamChildrenVo> List<T> removeNotResponse(List<T> list, ApiResponse[] responses, int index) {

		List<T> vos = new ArrayList<T>();
		String[] keys = null;
		boolean isOk = false;
		for (int i = 0; i < list.size(); i++) {
			T vo = list.get(i);
			isOk = false;
			for (ApiResponse param : responses) {
				keys = param.field().split("\\.");
				if (index <= keys.length - 1) {
					isOk = keys[index].equals(vo.getField());
					if (isOk && index < keys.length - 2 && null != vo.getChildren() && vo.getChildren().size() > 0) {
						vo.setChildren(removeNotResponse(vo.getChildren(), responses, index + 1));
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

	public List<ResponseVo> extractField(List<FieldInfo> infos) {
		List<ResponseVo> vos = new ArrayList<ResponseVo>();
		for (FieldInfo info : infos) {
			ResponseVo vo = new ResponseVo();
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
	 * 返回值注解替换
	 * 
	 * @param param
	 *            注解
	 * @param list
	 *            返回值对象
	 */
	private void replaceResponseField(ApiResponse param, List<ResponseVo> list) {
		String[] keys = param.field().split("\\.");
		ParamChildrenVo tempVo = null;
		List<? extends ParamChildrenVo> tempChildren = list;

		for (String key : keys) {
			for (ParamChildrenVo vo : tempChildren) {
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

}
