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
import org.springframework.web.bind.annotation.RequestHeader;

import com.github.ddm4j.api.document.annotation.ApiIgnore;
import com.github.ddm4j.api.document.annotation.ApiParam;
import com.github.ddm4j.api.document.annotation.ApiParams;
import com.github.ddm4j.api.document.common.exception.ApiCheckError;
import com.github.ddm4j.api.document.common.exception.ApiCheckException;
import com.github.ddm4j.api.document.common.exception.bean.ApiCheckInfo;
import com.github.ddm4j.api.document.common.model.KVEntity;
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
			logger.error("未查询到参数，不进行校验");
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
			boolean ignore = false;
			String name = names[i];
			for (Annotation annotation : parameterAnnotations[i]) {
				if (annotation instanceof ApiIgnore) {
					ignore = true;
					break;
				}
				if (annotation instanceof RequestHeader) {
					RequestHeader rh = (RequestHeader) annotation;
					if (!isEmpty(rh.value())) {
						name = rh.value();
					} else if (!isEmpty(rh.name())) {
						name = rh.name();
					}
				}
			}
			if (!ignore) {
				Object obj = jp.getArgs()[i];
				paramObjs.put(name, obj);
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
			// System.out.println(apiParam.field());
			boolean empty = true;
			for (Entry<String, Object> param : params.entrySet()) {
				// System.out.println("--- :" + param.getKey());
				// 判断是否为空
				if (null == param.getValue()) {
					if (apiParam.field().equals(param.getKey())) {
						empty = false;
						// 查询消息
						MessageBean message = getMessage(apiParam);
						ApiCheckInfo info = getCheckInfo(apiParam, ApiCheckError.EMPTY, message.getRequired());
						if (null != info) {
							infos.add(info);
						}
					}
				}
				// 判断是不是数组和集合
				else if (param.getValue().getClass().isArray()
						|| List.class.isAssignableFrom(param.getValue().getClass())
						|| Set.class.isAssignableFrom(param.getValue().getClass())) {
					// System.out.println("array");
					String[] keys = apiParam.field().split("\\.");
					// 数组或集合
					MessageBean message = getMessage(apiParam);
					KVEntity<Boolean, ApiCheckInfo> info = checkParamArray(param.getValue(), keys, 0, apiParam,
							message);
					if (null != info) {
						empty = info.getLeft();
						if (null != info.getRight())
							infos.add(info.getRight());
					}
				}
				// 判断是否是接口类型或是否是基本类型
				else if (param.getValue().getClass().isInterface()
						|| Number.class.isAssignableFrom(param.getValue().getClass())
						|| param.getValue().getClass() == String.class
						|| Boolean.class.isAssignableFrom(param.getValue().getClass())) {
					// System.out.println("class");
					if (apiParam.field().equals(param.getKey())) {
						empty = false;
						// 查询消息
						MessageBean message = getMessage(apiParam);
						ApiCheckInfo info = checkValue(param.getValue(), apiParam, message);
						if (null != info) {
							infos.add(info);
						}
					} else {
						continue;
					}

				}
				// 其他类型
				else {
					// System.out.println("other");
					String[] keys = apiParam.field().split("\\.");
					MessageBean message = getMessage(apiParam);
					KVEntity<Boolean, ApiCheckInfo> info = checkFieldValue(param.getValue(), keys, 0, apiParam, message,
							param.getValue());
					if (null != info) {
						empty = info.getLeft();
						if (null != info.getRight())
							infos.add(info.getRight());
					}
				}
			}
			// 循环完了，是否还是空的
			if (empty && apiParam.required()) {
				// 查询消息
				logger.error("未找到 field:" + apiParam.field());
				MessageBean message = getMessage(apiParam);
				infos.add(getCheckInfo(apiParam, ApiCheckError.EMPTY, message.getRequired()));
			} else if (empty) {
				// 找不到对应key
				logger.error("未找field:" + apiParam.field() + "  跳过校验");
			}
			if (null != infos && infos.size() > 0 && !config.isAll()) {
				throw new ApiCheckException(infos);
			}

		}
		if (null != infos && infos.size() > 0) {
			throw new ApiCheckException(infos);
		}

	}

	// 处理数组或集合
	@SuppressWarnings("unchecked")
	private KVEntity<Boolean, ApiCheckInfo> checkParamArray(Object value, String[] keys, int index, ApiParam apiParam,
			MessageBean message) throws Exception {

		if (value.getClass().isArray()) {
			Object[] objs = (Object[]) value;
			for (Object obj : objs) {
				// 判断还是不是 数组集合
				if (obj.getClass().isArray() || List.class.isAssignableFrom(obj.getClass())
						|| Set.class.isAssignableFrom(obj.getClass())) {
					return checkParamArray(obj, keys, index, apiParam, message);
				} else {
					KVEntity<Boolean, ApiCheckInfo> info = checkFieldValue(obj, keys, index, apiParam, message, obj);
					if (null != info) {
						return info;
					}
				}
			}
		} else if (List.class.isAssignableFrom(value.getClass())) {
			List<Object> objs = (List<Object>) value;
			for (Object obj : objs) {
				// 判断还是不是 数组集合
				if (obj.getClass().isArray() || List.class.isAssignableFrom(obj.getClass())
						|| Set.class.isAssignableFrom(obj.getClass())) {
					return checkParamArray(obj, keys, index, apiParam, message);
				} else {
					KVEntity<Boolean, ApiCheckInfo> info = checkFieldValue(obj, keys, index, apiParam, message, obj);
					if (null != info) {
						return info;
					}
				}
			}
		} else {
			Set<Object> objs = (Set<Object>) value;
			for (Object obj : objs) {
				// 判断还是不是 数组集合
				if (obj.getClass().isArray() || List.class.isAssignableFrom(obj.getClass())
						|| Set.class.isAssignableFrom(obj.getClass())) {
					return checkParamArray(obj, keys, index, apiParam, message);
				} else {
					KVEntity<Boolean, ApiCheckInfo> info = checkFieldValue(obj, keys, index, apiParam, message, obj);
					if (null != info) {
						return info;
					}
				}
			}
		}
		return null;
	}

	// 校验字段值
	private KVEntity<Boolean, ApiCheckInfo> checkFieldValue(Object value, String[] keys, int index, ApiParam apiParam,
			MessageBean message, Object obj) throws IllegalAccessException, Exception {

		// System.out.println("array 1---------- " + value);

		KVEntity<Boolean, ApiCheckInfo> info = new KVEntity<Boolean, ApiCheckInfo>();
		info.setLeft(true);
		Object value2 = value;
		Field field = null;
		for (int i = index; i < keys.length; i++) {
			// System.out.println("array 2---------- " + keys[i] + " ---- " + value2);
			field = getField(value2.getClass(), keys[i]);
			if (null != field) {
				info.setLeft(true);
				if (i < keys.length - 1) {
					// System.out.println("array 3---------- " + keys[i] + " ---- " + value2);
					field.setAccessible(true);
					value2 = field.get(value2);
					if (value2.getClass().isArray() || List.class.isAssignableFrom(value2.getClass())
							|| Set.class.isAssignableFrom(value2.getClass())) {
						// 递归校验
						return checkParamArray(value2, keys, i + 1, apiParam, message);
					}
				}
			}
		}

		// 校验值
		if (null != field) {
			// System.out.println("array end---------- " + value2);
			info.setLeft(false);
			field.setAccessible(true);
			Object v = field.get(value2);
			// 校验值
			ApiCheckInfo checkInfo = checkValue(v, apiParam, message);
			if (null != checkInfo) {
				info.setRight(checkInfo);
			}
		}
		return info;
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
		} else if (value.getClass().isPrimitive()) {

			if ("boolean".equals(value.getClass().getTypeName())) {
				// 只支持，是否为空
				return null;
			} else if ("char".equals(value.getClass().getTypeName())) {
				String regexp = getRegexp(apiParam.regexp());
				if (!isEmpty(regexp) && !value.toString().matches(regexp)) {
					return getCheckInfo(apiParam, ApiCheckError.REGEXP, bean.getRegexp());
				}
			} else {
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
			}
		} else if (Character.class.isAssignableFrom(value.getClass())) {
			// char 
			String regexp = getRegexp(apiParam.regexp());
			if (!isEmpty(regexp) && !value.toString().matches(regexp)) {
				return getCheckInfo(apiParam, ApiCheckError.REGEXP, bean.getRegexp());
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
		} else if (Boolean.class.isAssignableFrom(value.getClass())) {
			// 布尔只支持，是否为空
			return null;
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
					if ("custom".equals(key)) {
						sdf = new SimpleDateFormat(config.getDateFormat());
					} else if (key.startsWith("default")) {
						// 默认
						sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					} else if (key.startsWith("time")) {
						if (key.endsWith("Hm")) {
							sdf = new SimpleDateFormat("HH:mm");
						} else if (key.endsWith("ms")) {
							sdf = new SimpleDateFormat("mm:ss");
						} else {
							sdf = new SimpleDateFormat("HH:mm:ss");
						}
					} else if (key.startsWith("dateTime")) {
						if (key.endsWith("Hm")) {
							sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						} else if (key.endsWith("H")) {
							sdf = new SimpleDateFormat("yyyy-MM-dd HH");
						} else {
							sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						}
					} else {
						if (key.endsWith("M")) {
							sdf = new SimpleDateFormat("yyyy-MM");
						} else if (key.endsWith("Md")) {
							sdf = new SimpleDateFormat("MM-dd");
						} else {
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
