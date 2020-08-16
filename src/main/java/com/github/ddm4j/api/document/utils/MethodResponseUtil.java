package com.github.ddm4j.api.document.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.github.ddm4j.api.document.annotation.ApiResponseIgnore;
import com.github.ddm4j.api.document.annotation.ApiResponse;
import com.github.ddm4j.api.document.annotation.ApiResponses;
import com.github.ddm4j.api.document.bean.ResponseVo;
import com.github.ddm4j.api.document.common.model.FieldInfo;
import com.github.ddm4j.api.document.common.model.KVEntity;

public class MethodResponseUtil {

	public KVEntity<String, List<ResponseVo>> getResponseVo(Method method) {

		// 提取返回值注解
		ApiResponse[] responses = method.getAnnotationsByType(ApiResponse.class);
		if (null == responses) {
			ApiResponses params = method.getAnnotation(ApiResponses.class);
			if (null != params) {
				responses = params.value();
			}
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
						FieldUtil.removeField(list, field);
				}
			}
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
		for (int i = 0; i < list.size(); i++) {
			ResponseVo vo = list.get(i);
			boolean isOk = false;
			for (ApiResponse param : responses) {
				String[] keys = param.field().split("\\.");
				if (keys[0].equals(vo.getField())) {
					if (keys.length > 1 && null != vo.getChildren() && vo.getChildren().size() > 0) {
						list.get(i).setChildren(removeNotResponse(vo.getChildren(), keys, 1));
					}
					isOk = true;
					break;
				}
			}
			if (!isOk) {
				list.remove(i);
				i--;
			}

		}
		return list;
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
	private List<ResponseVo> removeNotResponse(List<ResponseVo> list, String[] keys, int index) {
		for (int i = 0; i < list.size(); i++) {
			ResponseVo vo = list.get(i);
			boolean isOk = false;
			if (keys[index].equals(vo.getField())) {
				if (index < keys.length - 1 && null != vo.getChildren() && vo.getChildren().size() > 0) {
					list.get(i).setChildren(removeNotResponse(vo.getChildren(), keys, index++));
				}
				isOk = true;
				break;
			}
			if (!isOk) {
				list.remove(i);
				i--;
			}
		}
		return list;
	}

	public List<ResponseVo> extractField(List<FieldInfo> infos) {
		List<ResponseVo> vos = new ArrayList<ResponseVo>();
		for (FieldInfo info : infos) {
			ResponseVo vo = new ResponseVo();
			vo.setField(info.getName());
			vo.setType(info.getType());
			vo.setDescribe(info.getDescribe());
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

}
