package com.github.ddm4j.api.document.check;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import com.github.ddm4j.api.document.annotation.ApiMark;
import com.github.ddm4j.api.document.annotation.ApiParam;
import com.github.ddm4j.api.document.annotation.ApiParams;
import com.github.ddm4j.api.document.common.exception.ApiCheckError;
import com.github.ddm4j.api.document.common.exception.ApiCheckException;
import com.github.ddm4j.api.document.common.exception.bean.ApiCheckInfo;
import com.github.ddm4j.api.document.config.CheckConfig;
import com.github.ddm4j.api.document.config.bean.MessageBean;

/**
 * 校验接口传递值
 */
@Aspect
@Component
public class ApiParamCheck {

	@Autowired
	CheckConfig config;
	// 日志对象
	static Logger logger = LoggerFactory.getLogger(ApiParamCheck.class);

	/**
	 * 全局校验
	 * 
	 * @param jp
	 *            参数对象
	 * @param apiParams
	 *            注解
	 * @throws Exception
	 *             异常信息
	 */
	@Before("execution(public * *(..)) && @annotation(apiParams)")
	public void checkParam(JoinPoint jp, ApiParams apiParams) throws Exception {
		if (null == config || !config.isEnable()) {
			// 未开校验
			return;
		}
		ApiParam[] params = apiParams.value();
		if (null == params || params.length == 0) {
			// 未配置
			return;
		}

		if (null == jp.getArgs() || jp.getArgs().length == 0) {
			logger.error("未没参数，不进行校验");
			// 没有参数
			return;
		}
		// 判断是不是JSON

		MethodSignature signature = (MethodSignature) jp.getSignature();
		// 获取参数参数名
		LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
		String[] names = u.getParameterNames(signature.getMethod());
		// 获取参数上的注解
		Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();
		// 循环处理取出
		Map<String, Object> paramObjs = new HashMap<String, Object>();
		for (int i = 0; i < parameterAnnotations.length; i++) {
			for (Annotation annotation : parameterAnnotations[i]) {
				if (annotation instanceof ApiMark) {
					Object obj = jp.getArgs()[i];
					paramObjs.put(names[i], obj);
				}
			}
		}
		// 校验
		checkParam(paramObjs, names, params);
	}

	// 校验参数
	public void checkParam(Map<String, Object> params, String[] names, ApiParam[] apiParams)
			throws Exception, Exception {
		List<ApiCheckInfo> infos = new ArrayList<ApiCheckInfo>();

		for (ApiParam apiParam : apiParams) {
			// System.out.println("--1-- " + apiParam.field() + " -
			// -------------------------------------- ");
			boolean empty = true;
			for (Entry<String, Object> param : params.entrySet()) {
				// System.out.println("--2-- " + param.getKey() + " --- " + param.getValue());
				// 判断是否为空
				if (null == param.getValue()) {
					if (apiParam.field().equals(param.getKey())) {
						empty = false;
						// System.out.println("--2-1-- ");
						// 查询消息
						MessageBean message = getMessage(apiParam);
						ApiCheckInfo info = checkValue(param.getValue(), apiParam, message);
						if (null != info) {
							infos.add(info);
						}
					}
				} else
				// 判断是否是接口类型或是否是基本类型
				if (param.getValue().getClass().isInterface()
						|| Number.class.isAssignableFrom(param.getValue().getClass())
						|| param.getValue().getClass() == String.class) {

					if (apiParam.field().equals(param.getKey())) {
						empty = false;
						// System.out.println("--3-1-- ");
						// 查询消息
						MessageBean message = getMessage(apiParam);
						ApiCheckInfo info = checkValue(param.getValue(), apiParam, message);
						if (null != info) {
							infos.add(info);
						}
					} else {
						continue;
					}
				} else {
					// System.out.println("--3-2-- ");
					empty = false;
					String[] keys = apiParam.field().split("\\.");
					Field field = null;
					Class<?> cla = param.getValue().getClass();
					Object value = param.getValue();

					boolean array = false;

					for (int i = 0; i < keys.length; i++) {
						String key = keys[i];
						// System.out.println("--3-1-2-- " + key + " cla --" + cla);
						field = getField(cla, key);
						if (null != field) {
							if (i < keys.length - 1) {
								// cla = field.getType();
								field.setAccessible(true);
								value = field.get(value);
								cla = value.getClass();

								// System.out.println("--3-1-value-- " + value);
								// 针对集合处理
								if (value.getClass().isArray() || List.class.isAssignableFrom(value.getClass())
										|| Set.class.isAssignableFrom(value.getClass())) {
									array = true;
									// System.out.println("--3-1-array-- " + key + " cla --" + value);
									// 查询消息
									MessageBean message = getMessage(apiParam);
									ApiCheckInfo info = checkParamArray(value, keys, i, apiParam, message);
									if (null != info) {
										infos.add(info);
									}
									break;
								}
							}
						} else {
							break;
						}
					}
					// 集合在上面已经处理过了
					if (array) {
						continue;
					}

					if (null != field) {

						// 设置可以访问私有属性
						field.setAccessible(true);
						value = field.get(value);
						// System.out.println("--3-1-3-- " + param.getKey() + " --- " + value);
						// 查询消息
						MessageBean message = getMessage(apiParam);
						// 校验值
						ApiCheckInfo info = checkValue(value, apiParam, message);
						if (null != info) {
							infos.add(info);
						}

					} else {
						// System.out.println("--3-1-4-- " + param.getKey() + " --- " +
						// apiParam.field());
						// 判断是不是同一个
						if (param.getKey().trim().equals(apiParam.field().trim())) {
							// 查询消息
							MessageBean message = getMessage(apiParam);
							ApiCheckInfo info = checkValue(param.getValue(), apiParam, message);
							if (null != info) {
								infos.add(info);
							}
						} else {
							// 找不到对应key
							logger.error("未找field:" + apiParam.field() + "  跳过校验");
						}

					}
				}
				if (!config.isAll()) {
					throw new ApiCheckException(infos);
				}
			}
			// 循环完了，是否还是空的
			if (empty && apiParam.required()) {
				// 查询消息
				MessageBean message = getMessage(apiParam);
				infos.add(getCheckInfo(apiParam, ApiCheckError.EMPTY, message.getRequired()));
			}
			if (!config.isAll()) {
				throw new ApiCheckException(infos);
			}

		}
		if (infos.size() > 0) {
			throw new ApiCheckException(infos);
		}

	}

	@SuppressWarnings("unchecked")
	private ApiCheckInfo checkParamArray(Object value, String[] keys, int index, ApiParam apiParam, MessageBean message)
			throws Exception {

		if (value.getClass().isArray()) {
			Object[] objs = (Object[]) value;
			for (Object obj : objs) {
				ApiCheckInfo info = checkFieldArrayValue(obj, keys, index, apiParam, message, obj);
				if (null != info) {
					return info;
				}
			}
		} else if (List.class.isAssignableFrom(value.getClass())) {
			List<Object> objs = (List<Object>) value;
			for (Object obj : objs) {
				ApiCheckInfo info = checkFieldArrayValue(obj, keys, index, apiParam, message, obj);
				if (null != info) {
					return info;
				}
			}
		} else {
			Set<Object> objs = (Set<Object>) value;
			for (Object obj : objs) {
				ApiCheckInfo info = checkFieldArrayValue(obj, keys, index, apiParam, message, obj);
				if (null != info) {
					return info;
				}
			}
		}
		return null;
	}

	private ApiCheckInfo checkFieldArrayValue(Object value, String[] keys, int index, ApiParam apiParam,
			MessageBean message, Object obj) throws IllegalAccessException, Exception {
		// System.out.println("array 1---------- " + value);
		Object value2 = value;
		Field field = null;
		for (int i = index + 1; i < keys.length; i++) {
			// System.out.println("array 2---------- " + keys[i]);
			field = getField(obj.getClass(), keys[i]);
			if (null != field) {
				if (i < keys.length - 1) {
					field.setAccessible(true);
					value2 = field.get(obj);
					if (value2.getClass().isArray() || List.class.isAssignableFrom(value2.getClass())
							|| Set.class.isAssignableFrom(value2.getClass())) {
						// 递归校验
						ApiCheckInfo info = checkParamArray(value2, keys, i, apiParam, message);
						if (null != info) {
							return info;
						}
						// 不用在让下面校验了
						field = null;
					}
				}
			}
		}
		// 校验值
		if (null != field) {
			// System.out.println("array end---------- " + value2);
			field.setAccessible(true);
			Object v = field.get(value2);
			// 校验值
			ApiCheckInfo info = checkValue(v, apiParam, message);
			if (null != info) {
				return info;
			}
		}
		return null;
	}

	/**
	 * 校验值
	 * 
	 * @param value
	 *            值
	 * @param apiParam
	 *            注解对象
	 */
	private ApiCheckInfo checkValue(Object value, ApiParam apiParam, MessageBean bean) {

		if (null == value) {
			if (apiParam.required()) {
				return getCheckInfo(apiParam, ApiCheckError.EMPTY, bean.getRequired());
			}
			return null;
		}
		if (value.getClass().isInterface()) {
			// 判断是不是接口类型，接口类型只判断是为空
			if (apiParam.required()) {
				return getCheckInfo(apiParam, ApiCheckError.EMPTY, bean.getRequired());
			}
		} else if (Number.class.isAssignableFrom(value.getClass())) {
			// 数字类型
			Double dou = Double.parseDouble(value.toString());

			if (dou < apiParam.min()) {
				return getCheckInfo(apiParam, ApiCheckError.MIN, bean.getMin());
			}

			if (dou > apiParam.max()) {
				return getCheckInfo(apiParam, ApiCheckError.MAX, bean.getMax());
			}
			String regexp = getRegexp(apiParam.regexp());
			if (!isEmpty(regexp) && !value.toString().matches(regexp)) {
				return getCheckInfo(apiParam, ApiCheckError.REGEXP, bean.getRegexp());
			}

		} else if (value.getClass() == String.class) {
			String str = value.toString().trim();

			if (str.length() < apiParam.min()) {
				return getCheckInfo(apiParam, ApiCheckError.MIN, bean.getMin());
			}

			if (str.length() > apiParam.max()) {
				return getCheckInfo(apiParam, ApiCheckError.MAX, bean.getMax());
			}

			// 字符串类型
			String regexp = getRegexp(apiParam.regexp());
			if (!isEmpty(regexp) && !str.matches(regexp)) {
				return getCheckInfo(apiParam, ApiCheckError.REGEXP, bean.getRegexp());
			}

		} else
		// 判断是不是 list
		if (List.class.isAssignableFrom(value.getClass())) {
			@SuppressWarnings("unchecked")
			List<Object> values = (List<Object>) value;
			for (Object obj : values) {
				ApiCheckInfo info = checkValue(obj, apiParam, bean);
				if (null != info) {
					return info;
				}
			}
		} else
		// 判断是不是 set
		if (Set.class.isAssignableFrom(value.getClass())) {
			@SuppressWarnings("unchecked")
			Set<Object> values = (Set<Object>) value;
			for (Object obj : values) {
				ApiCheckInfo info = checkValue(obj, apiParam, bean);
				if (null != info) {
					return info;
				}
			}
		} else
		// 判断是不是数组
		if (value.getClass().isArray()) {
			Object[] values = (Object[]) value;
			for (Object obj : values) {
				ApiCheckInfo info = checkValue(obj, apiParam, bean);
				if (null != info) {
					return info;
				}
			}
		} else
		// 判断是不是日期类型
		if (Date.class.isAssignableFrom(value.getClass())) {
			String regexp = getRegexp(apiParam.regexp());
			if (!isEmpty(regexp)) {
				SimpleDateFormat sdf = null;
				String key = apiParam.regexp();
				if (key.startsWith("${") && key.endsWith("}") && key.length() > 3) {
					key = key.substring(2, key.length() - 1);
				} else {
					// 判断是否自定义了 格式化方式
					if (isEmpty(config.getDateFormat())) {
						key = "default";
					} else {
						key = "custom";
					}
				}
				try {
					// 区分是否是自定义，或默认
					if("custom".equals(key)) {
						sdf = new SimpleDateFormat(config.getDateFormat());
					}else if(key.startsWith("default")) {
						// 默认
						sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					}else if(key.startsWith("time")) {
						if(key.endsWith("Hm")) {
							sdf = new SimpleDateFormat("HH:mm");
						}else if(key.endsWith("ms")) {
							sdf = new SimpleDateFormat("mm:ss");
						}else {
							sdf = new SimpleDateFormat("HH:mm:ss");
						}
					}else if(key.startsWith("dateTime")) {
						if(key.endsWith("Hm")) {
							sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						}else if(key.endsWith("H")) {
							sdf = new SimpleDateFormat("yyyy-MM-dd HH");
						}else {
							sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						}
					}else {
						if(key.endsWith("M")) {
							sdf = new SimpleDateFormat("yyyy-MM");
						}else if(key.endsWith("Md")) {
							sdf = new SimpleDateFormat("MM-dd");
						}else {
							sdf = new SimpleDateFormat("yyyy-MM-dd");
						}
					}
					
					Date date = (Date) value;

					String str = sdf.format(date);
					if (!str.matches(regexp)) {
						return getCheckInfo(apiParam, ApiCheckError.REGEXP, bean.getRegexp());
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} else {
			// System.out.println(value.getClass());
			// System.out.println("null");
			// 未找到,非基本数据类型或String类型只能校验是否为空
		}
		return null;
	}

	// 获致正则表达式
	private String getRegexp(String regexp) {
		if (isEmpty(regexp)) {
			return null;
		}
		if (regexp.startsWith("${") && regexp.endsWith("}")) {
			String key = regexp.substring(2, regexp.length() - 1);
			String reg = config.getRegexps().get(key);
			if (isEmpty(reg)) {
				logger.error("参数校验：" + regexp + " 正则表达式,不存在");
				return null;
			}
			return reg;
		}
		return regexp;
	}

	// 获取错误消息
	private MessageBean getMessage(ApiParam apiParam) {
		MessageBean bean = null;
		if (!isEmpty(apiParam.message())) {
			String key = apiParam.message();
			if (key.startsWith("${") && key.endsWith("}")) {
				if (key.length() <= 3) {
					key = "default";
				} else {
					key = key.substring(2, key.length() - 1);
				}
			}
			bean = config.getMessages().get(apiParam.message());
		}

		if (null == bean) {
			bean = config.getMessages().get("default");
		}
		return bean;
	}

	// 生成校验详情
	private ApiCheckInfo getCheckInfo(ApiParam apiParam, ApiCheckError error, String message) {
		ApiCheckInfo info = new ApiCheckInfo();
		info.setDescribe(apiParam.describe());
		info.setError(error);
		info.setField(apiParam.field());
		info.setMessage(message);
		return info;
	}

	// 获取属性
	public static Field getField(Class<?> cla, String key) {
		Field field = null;
		try {
			field = cla.getDeclaredField(key);
		} catch (NoSuchFieldException ex) {

		}
		// 没有找到，就去父级找
		if (null == field && Object.class != cla.getSuperclass()) {
			return getField(cla.getSuperclass(), key);
		}
		return field;
	}

	/**
	 * 判断字符串是否为空
	 * 
	 * @param str
	 *            字符串
	 * @return 结果： true 为空 ,false 不为空
	 */
	public static boolean isEmpty(String str) {
		if (null == str || "".equals(str.trim())) {
			return true;
		}
		return false;
	}
}
