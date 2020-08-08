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
		if (null != hides && null != hides.value() && hides.value().length > 0) {
			for (String field : hides.value()) {
				FieldUtil.removeField(list, field);
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
