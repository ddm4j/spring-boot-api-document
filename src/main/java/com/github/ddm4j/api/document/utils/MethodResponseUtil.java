package com.github.ddm4j.api.document.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.core.annotation.AnnotationUtils;

import com.github.ddm4j.api.document.annotation.ApiResponseIgnore;
import com.github.ddm4j.api.document.annotation.ApiResponse;
import com.github.ddm4j.api.document.annotation.ApiResponseCode;
import com.github.ddm4j.api.document.annotation.ApiResponses;
import com.github.ddm4j.api.document.bean.ParamChildrenVo;
import com.github.ddm4j.api.document.bean.ResponseVo;
import com.github.ddm4j.api.document.common.model.FieldInfo;
import com.github.ddm4j.api.document.common.model.KVEntity;
import com.github.ddm4j.api.document.config.ResponseCodeConfig;

public class MethodResponseUtil {

	ResponseCodeConfig config;

	public MethodResponseUtil(ResponseCodeConfig config) {
		this.config = config;
	}

	public KVEntity<String, List<ResponseVo>> getResponseVo(Method method, ApiResponseCode typeCode) {

		// 提取返回值注解
		// ApiResponse[] responses = method.getAnnotationsByType(ApiResponse.class);
		ApiResponse[] responses = new ApiResponse[0];
		ApiResponses responsess = AnnotationUtils.getAnnotation(method, ApiResponses.class);
		if (null != responsess) {
			responses = responsess.value();
		} else {
			ApiResponse response = method.getAnnotation(ApiResponse.class);
			if (null != response) {
				responses = new ApiResponse[1];
				responses[0] = response;
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
						list = FieldUtil.removeField(list, field);
				}
			}
		}
		// code 隐藏或删除
		ApiResponseCode code = AnnotationUtils.getAnnotation(method, ApiResponseCode.class);
		if (null != code || null != typeCode || (null != config && null != config.getField())) {
			removeCode(list, code, typeCode);
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

	private List<ResponseVo> removeCode(List<ResponseVo> list, ApiResponseCode code, ApiResponseCode typeCode) {
		String field = null;

		if (null != code) {
			field = code.field();
		} else if (null != typeCode) {
			field = typeCode.field();
		}
		if (FieldUtil.isEmpty(field)) {
			if (FieldUtil.isEmpty(config.getField())) {
				return list;
			} else {
				field = config.getField();
			}
		}
		// 状态码字段
		String[] keys = field.split("\\.");
		// 需要的
		Set<String> codes = new TreeSet<String>();
		// 描述
		Map<String, String> descs = new HashMap<String, String>();
		// 排除配置文件上的
		Set<String> cancelCodes = new TreeSet<String>();

		// Controller上的
		if (null != typeCode) {
			for (String codeStr : typeCode.codes()) {
				if (!FieldUtil.isEmpty(codeStr)) {
					if (codeStr.indexOf(":") > 0) {
						codes.add(codeStr.substring(0, codeStr.indexOf(":")));
						if (codeStr.indexOf(":") + 1 < codeStr.length()) {
							descs.put(codeStr.substring(0, codeStr.indexOf(":")),
									codeStr.substring(codeStr.indexOf(":") + 1));
						}
					} else {
						codes.add(codeStr);
					}
				}
			}
			for (String codeStr : typeCode.cancel()) {
				if (!FieldUtil.isEmpty(codeStr)) {
					cancelCodes.add(codeStr);
				}
			}
		}

		// 方法上的
		if (null != code) {
			for (String codeStr : code.codes()) {
				if (!FieldUtil.isEmpty(codeStr)) {
					if (codeStr.indexOf(":") > 0) {
						codes.add(codeStr.substring(0, codeStr.indexOf(":")));
						if (codeStr.indexOf(":") + 1 < codeStr.length()) {
							descs.put(codeStr.substring(0, codeStr.indexOf(":")),
									codeStr.substring(codeStr.indexOf(":") + 1));
						}

					} else {
						codes.add(codeStr);
					}
				}
			}
			for (String codeStr : code.cancel()) {
				if (!FieldUtil.isEmpty(codeStr)) {
					cancelCodes.add(codeStr);
				}
			}
		}

		if (null != config.getCodes() && config.getCodes().length > 0) {
			int index = -1;

			for (String codeStr : config.getCodes()) {
				if (!FieldUtil.isEmpty(codeStr)) {
					String desc = "";
					if (codeStr.indexOf(":") > 0) {
						if (codeStr.indexOf(":") + 1 < codeStr.length()) {
							desc = codeStr.substring(codeStr.indexOf(":") + 1);
						}
						codeStr = codeStr.substring(0, codeStr.indexOf(":"));
					}
					if (cancelCodes.size() > 0) {
						for (String checkCode : cancelCodes) {
							if (checkCode.indexOf(":") > 0) {
								checkCode = checkCode.substring(0, checkCode.indexOf(":"));
							}
							index = checkCode.indexOf("*");
							if (index > -1 && index == 0) {
								// 匹配后面
								if (!codeStr.endsWith(checkCode.substring(1))) {
									codes.add(codeStr);
									if (!FieldUtil.isEmpty(desc)) {
										descs.put(codeStr, desc);
									}
								}
							} else if (index > 0) {
								// 匹配后面
								if (!codeStr.startsWith(checkCode.substring(0, checkCode.length() - 1))) {
									codes.add(codeStr);
									if (!FieldUtil.isEmpty(desc)) {
										descs.put(codeStr, desc);
									}
								}
							} else {
								// 不匹配
								if (!codeStr.equals(checkCode)) {
									codes.add(codeStr);
									if (!FieldUtil.isEmpty(desc)) {
										descs.put(codeStr, desc);
									}
								}
							}
						}
					} else {
						codes.add(codeStr);
						if (!FieldUtil.isEmpty(desc)) {
							descs.put(codeStr, desc);
						}

					}
				}
			}
		}

		if (codes.size() == 0) {
			return list;
		}

		boolean hide = false;
		if (null != code) {
			hide = code.hide();
		} else if (null != typeCode) {
			hide = typeCode.hide();
		}
		return removeCode(list, codes, hide, keys, descs, 0);
	}

	@SuppressWarnings("unchecked")
	private <T extends ParamChildrenVo> List<T> removeCode(List<T> list, Set<String> codes, boolean hide, String[] keys,
			Map<String, String> descs, int index) {
		List<T> vos = new ArrayList<T>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getField().equals(keys[index])) {

				// 判断是否达到目标区域了
				if (index < keys.length - 1) {
					list.get(i).setChildren(removeCode(list.get(i).getChildren(), codes, hide, keys, descs, index + 1));
					return list;
				}
				String desc = null;
				if (null != list.get(i).getChildren() && list.get(i).getChildren().size() > 0) {
					boolean isOK = hide;// 是否隐藏
					for (int j = 0; j < list.get(i).getChildren().size(); j++) {
						isOK = hide;// 是否隐藏
						desc = null;
						for (String key : codes) {
							index = key.indexOf("*");
							if (index > -1 && index == 0) {
								// 匹配后面
								if (list.get(i).getChildren().get(j).getFieldName().endsWith(key.substring(1))) {
									if (hide) {
										// 如果是隐藏，就不要加入了
										isOK = false;
									} else {
										// 如果是显示，就得加入了
										isOK = true;
										desc = descs.get(key);
									}
								}
							} else if (index > 0) {
								// 匹配后面
								if (list.get(i).getChildren().get(j).getFieldName()
										.startsWith(key.substring(0, key.length() - 1))) {
									if (hide) {
										// 如果是隐藏，就不要加入了
										isOK = false;
									} else {
										// 如果是显示，就得加入了
										isOK = true;
										desc = descs.get(key);
									}
								}
							} else {
								// 不匹配
								if (list.get(i).getChildren().get(j).getFieldName().equals(key)) {
									if (hide) {
										// 如果是隐藏，就不要加入了
										isOK = false;
									} else {
										// 如果是显示，就得加入了
										isOK = true;
										desc = descs.get(key);
									}
								}
							}
						}
						if (isOK) {
							T code = (T) list.get(i).getChildren().get(j);
							if (null != desc) {
								code.setDescribe(desc);
							}
							vos.add(code);
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
