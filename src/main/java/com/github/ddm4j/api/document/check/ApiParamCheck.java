package com.github.ddm4j.api.document.check;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import com.github.ddm4j.api.document.annotation.ApiField;
import com.github.ddm4j.api.document.common.check.ApiParamCheckFailHandler;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import com.github.ddm4j.api.document.annotation.ApiIgnore;
import com.github.ddm4j.api.document.annotation.ApiParam;
import com.github.ddm4j.api.document.annotation.ApiParams;
import com.github.ddm4j.api.document.common.exception.ApiCheckError;
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

    static Logger logger = LoggerFactory.getLogger(ApiParamCheck.class);

    @Autowired
    CheckConfig config;

    @Autowired
    ApiParamCheckFailHandler apiParamCheckFailHandler;

    /**
     * 全局校验
     *
     * @param jp
     * @param apiParam
     * @return
     * @throws Throwable
     */
    @Around("execution(public * *(..)) && @annotation(apiParam)")
    public Object checkParam(ProceedingJoinPoint jp, ApiParam apiParam) throws Throwable {
        if (null == config || !config.isEnable()) {
            // 未打开校验功能
            return jp.proceed();
        }

        if (null == apiParam) {
            // 未配置
            return jp.proceed();
        }
        Long time = System.currentTimeMillis();// 开始时间
        try {
            ApiParam[] params = new ApiParam[1];
            params[0] = apiParam;

            return checkParamHandler(jp, params);
        } catch (Exception exception) {
            throw exception;
        } finally {
            logger.debug("校验结束,花费时间：{}毫秒", System.currentTimeMillis() - time);
        }
    }

    /**
     * 全局校验
     *
     * @param jp        参数对象
     * @param apiParams 注解
     * @throws Exception 异常信息
     */
    @Around("execution(public * *(..)) && @annotation(apiParams)")
    public Object checkParam(ProceedingJoinPoint jp, ApiParams apiParams) throws Throwable {
        if (null == config || !config.isEnable()) {
            // 未打开校验功能
            return jp.proceed();
        }
        ApiParam[] params = apiParams.value();
        if (null == params || params.length == 0) {
            // 未配置
            return jp.proceed();
        }
        Long time = System.currentTimeMillis();// 开始时间
        try {
            return checkParamHandler(jp, params);
        } catch (Exception exception) {
            throw exception;
        } finally {
            logger.debug("校验结束,花费时间：{}毫秒", System.currentTimeMillis() - time);
        }
    }

    /**
     * 校验参数处理
     *
     * @param jp
     * @param params
     * @return
     * @throws Throwable
     */
    private Object checkParamHandler(ProceedingJoinPoint jp, ApiParam[] params) throws Throwable {
        // 提取参数
        Map<String, Object> paramObjs = extractParam(jp);
        if (null != paramObjs && paramObjs.size() > 0) {
            // 校验
            List<ApiCheckInfo> infos = checkParam(paramObjs, params);
            Object result = null;
            if (null == infos || infos.isEmpty()) {
                return jp.proceed();
            } else {
                result = apiParamCheckFailHandler.checkApiParamFail(jp, findResultObject(jp), infos);
                if (null == result) {
                    result = jp.proceed();
                }
            }
            return result;
        } else {
            if (params.length > 0) {
                logger.warn("未找到方法上的参数，不进行校验");
            }
        }
        return jp.proceed();
    }

    /**
     * 获取返回值对象
     *
     * @param jp
     * @return
     */
    private Object findResultObject(ProceedingJoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Class returnType = signature.getReturnType();

        if (!returnType.equals(Void.class)) {
            try {
                return returnType.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 提取参数
     *
     * @param jp
     * @return
     */
    public Map<String, Object> extractParam(JoinPoint jp) {
        if (null == jp.getArgs() || jp.getArgs().length == 0) {
            // 没有参数
            return new HashMap<>(0);
        }
        // 判断是不是JSON
        MethodSignature signature = (MethodSignature) jp.getSignature();
        // 获取参数参数名
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] names = u.getParameterNames(signature.getMethod());
        // 获取参数上的注解
        Annotation[][] parameterAnnotations = signature.getMethod().getParameterAnnotations();
        // 循环处理取出
        Map<String, Object> paramObjs = new HashMap<>();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            boolean ignore = false;
            String name = names[i];
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof ApiIgnore) {
                    ignore = true;
                    break;
                }
                // 请求头数据
                if (annotation instanceof RequestHeader) {
                    RequestHeader rh = (RequestHeader) annotation;
                    if (!isEmpty(rh.value())) {
                        name = rh.value();
                    } else if (!isEmpty(rh.name())) {
                        name = rh.name();
                    }
                }
                // uri 上的数据
                if (annotation instanceof PathVariable) {
                    PathVariable pv = (PathVariable) annotation;
                    if (!isEmpty(pv.value())) {
                        name = pv.value();
                    } else if (!isEmpty(pv.name())) {
                        name = pv.name();
                    }
                }
            }
            if (null != jp.getArgs()[i]) {
                Class<?> cla = jp.getArgs()[i].getClass();
                if (cla.isAssignableFrom(ServletRequest.class) || cla.isAssignableFrom(ServletResponse.class)
                        || cla.isAssignableFrom(HttpSession.class) || cla.isAssignableFrom(Servlet.class)) {
                    ignore = true;
                }
            }
            if (!ignore) {
                Object obj = jp.getArgs()[i];
                paramObjs.put(name, obj);
            }

        }
        return paramObjs;
    }

    /**
     * 校验参数
     *
     * @param params
     * @param apiParams
     * @throws Exception
     */
    public List<ApiCheckInfo> checkParam(Map<String, Object> params, ApiParam[] apiParams) throws Exception {
        List<ApiCheckInfo> infos = new ArrayList<>();

        for (ApiParam apiParam : apiParams) {
            if ("".equals(apiParam.field())) {
                continue;
            }
            String[] keys = apiParam.field().split("\\.");
            boolean empty = true;

            Object value = params.get(keys[0]);

            if (null != value) {
                empty = checkType(keys[0], value, apiParam, keys, 0, infos);
            }
            // 如果上一个，找不到，那就下个参数中查找
            if (empty) {
                // 当前不存在，是否在下一级
                for (Entry<String, Object> param : params.entrySet()) {
                    if (!param.getKey().equals(keys[0])) {
                        empty = checkType(param.getKey(), param.getValue(), apiParam, keys, 0, infos);
                        // 找到了，就不循环了
                        if (!empty) {
                            break;
                        }
                    }
                }
            }
            // 循环完了，是否还是空的
            if (empty && apiParam.required()) {
                // 查询消息
                logger.error("未找到 field:{}", apiParam.field());
                MessageBean message = getMessage(apiParam);
                infos.add(getCheckInfo(apiParam, ApiCheckError.EMPTY, message.getRequired(), null));
            } else if (empty) {
                // 找不到对应key
                logger.error("未找field:{}  跳过校验", apiParam.field());
            }

            if (null != infos && !infos.isEmpty() && !config.isAll()) {
                return infos;
            }
        }
        return infos;
    }

    /**
     * 校验类型
     *
     * @param fieldName
     * @param value
     * @param apiParam
     * @param keys
     * @param index
     * @param infos
     * @return
     * @throws Exception
     */
    private boolean checkType(String fieldName, Object value, ApiParam apiParam, String[] keys, int index,
                              List<ApiCheckInfo> infos) throws Exception {
        boolean empty = false;
        MessageBean message = getMessage(apiParam);
        // 判断是否为空
        if (null == value) {
            if (fieldName.equals(keys[index])) {
                if (apiParam.required()) {
                    infos.add(getCheckInfo(apiParam, ApiCheckError.EMPTY, message.getRequired(), null));
                }
            } else {
                empty = true;
            }
        } else
            // 判断是不是文件
            if (MultipartFile.class.isAssignableFrom(value.getClass())) {
                if (fieldName.equals(keys[index])) {
                    ApiCheckInfo checkInfo = checkValue(value, apiParam, message, null);
                    if (null != checkInfo)
                        infos.add(checkInfo);
                } else {
                    empty = true;
                }
            }
            // 判断是否是接口类型或是否是基本类型
            else if (value.getClass().isInterface() || Number.class.isAssignableFrom(value.getClass())
                    || value.getClass() == String.class || Boolean.class.isAssignableFrom(value.getClass())) {
                // 查询消息
                if (fieldName.equals(keys[index])) {
                    ApiCheckInfo info = checkValue(value, apiParam, message, null);
                    if (null != info) {
                        infos.add(info);
                    }
                } else {
                    empty = true;
                }

            }
            // 判断是不是数组和集合
            else if (value.getClass().isArray() || Collection.class.isAssignableFrom(value.getClass())) {
                // 数组或集合
                KVEntity<Boolean, ApiCheckInfo> info = checkParamArray(fieldName, value, keys, index, apiParam, message, null);
                if (null != info) {
                    empty = info.getLeft();
                    if (null != info.getRight())
                        infos.add(info.getRight());
                }
            }
            // 其他类型
            else {
                KVEntity<Boolean, ApiCheckInfo> info = checkFieldValue(fieldName, value, keys, index, apiParam, message);
                if (null != info) {
                    empty = info.getLeft();
                    if (null != info.getRight())
                        infos.add(info.getRight());
                }
            }
        return empty;
    }

    /**
     * 获取空对象
     *
     * @param apiParam
     * @param message
     * @param apiField
     * @return
     */
    private KVEntity<Boolean, ApiCheckInfo> getEmptyInfo(ApiParam apiParam, MessageBean message, ApiField apiField) {
        KVEntity<Boolean, ApiCheckInfo> result = new KVEntity<>();
        result.setLeft(false);
        result.setRight(getCheckInfo(apiParam, ApiCheckError.EMPTY, message.getRequired(), apiField));
        return result;
    }

    /**
     * 处理数组或集合
     *
     * @param value
     * @param keys
     * @param index
     * @param apiParam
     * @param message
     * @param apiField
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private KVEntity<Boolean, ApiCheckInfo> checkParamArray(String paramFieldName,Object value, String[] keys, int index, ApiParam apiParam,
                                                            MessageBean message, ApiField apiField) throws Exception {
        if (value.getClass().isArray()) {
            Object[] objs = (Object[]) value;
            if (objs.length > 0) {
                for (Object obj : objs) {
                    KVEntity<Boolean, ApiCheckInfo> info = checkArrayValue(paramFieldName, obj, keys, index, apiParam, message, apiField);
                    if (null != info) {
                        return info;
                    }
                }
            } else if (apiParam.required()) {
                return getEmptyInfo(apiParam, message, apiField);
            }
            return null;
        }
        Collection<Object> collection;
        if (List.class.isAssignableFrom(value.getClass())) {
            collection = (List<Object>) value;
        } else {
            collection = (Set<Object>) value;
        }
        if (!collection.isEmpty()) {
            for (Object obj : collection) {
                KVEntity<Boolean, ApiCheckInfo> info = checkArrayValue(paramFieldName, obj, keys, index, apiParam, message, apiField);
                if (null != info) {
                    return info;
                }
            }
        } else if (apiParam.required()) {
            return getEmptyInfo(apiParam, message, apiField);
        }

        return null;
    }

    /**
     * 校验集合值
     *
     * @param obj
     * @param keys
     * @param index
     * @param apiParam
     * @param message
     * @param apiField
     * @return
     * @throws Exception
     */
    private KVEntity<Boolean, ApiCheckInfo> checkArrayValue(String paramFieldName,Object obj, String[] keys, int index, ApiParam apiParam,
                                                            MessageBean message, ApiField apiField) throws Exception {
        // 判断还是不是 数组集合
        if (obj.getClass().isArray() || List.class.isAssignableFrom(obj.getClass())
                || Set.class.isAssignableFrom(obj.getClass())) {
            return checkParamArray(paramFieldName, obj, keys, index, apiParam, message, apiField);
        } else {
            KVEntity<Boolean, ApiCheckInfo> info = checkFieldValue(paramFieldName, obj, keys, index, apiParam, message);
            if (null != info) {
                return info;
            }
        }
        return null;
    }

    /**
     * 校验字段值
     *
     * @param value
     * @param keys
     * @param index
     * @param apiParam
     * @param message
     * @return
     * @throws Exception
     */
    private KVEntity<Boolean, ApiCheckInfo> checkFieldValue(String paramFieldName, Object value, String[] keys, int index, ApiParam apiParam,
                                                            MessageBean message) throws Exception {
        KVEntity<Boolean, ApiCheckInfo> info = new KVEntity<>();
        info.setLeft(true);

        if(!isEmpty(paramFieldName) && paramFieldName.equals(apiParam.field()) && index == 0 && keys.length == 1){
            info.setLeft(false);
            ApiCheckInfo checkInfo = checkValue(value, apiParam, message, null);
            if (null != checkInfo) {
                info.setRight(checkInfo);
            }
        }

        Object value2 = value;
        Field field = null;
        for (int i = index; i < keys.length; i++) {
            field = getField(value2.getClass(), keys[i]);
            if (null != field && i < keys.length - 1) {
                field.setAccessible(true);
                value2 = field.get(value2);
                ApiField afi = AnnotationUtils.getAnnotation(field, ApiField.class);
                if (null == value2) {
                    if (apiParam.required()) {
                        info.setRight(getCheckInfo(apiParam, ApiCheckError.EMPTY, message.getRequired(), afi));
                        info.setLeft(false);
                    }
                    return info;
                }
                if (field.getType().isArray() || Collection.class.isAssignableFrom(field.getType())) {
                    // 递归校验
                    return checkParamArray(paramFieldName, value2, keys, i + 1, apiParam, message, afi);
                }
            }
        }
        // 校验值
        if (null != field) {
            info.setLeft(false);
            field.setAccessible(true);
            Object v = field.get(value2);
            ApiField afi = AnnotationUtils.getAnnotation(field, ApiField.class);
            // 校验值
            ApiCheckInfo checkInfo = checkValue(v, apiParam, message, afi);
            if (null != checkInfo) {
                info.setRight(checkInfo);
            }
        }
        return info;
    }

    /**
     * 校验值
     *
     * @param value    值
     * @param apiParam 注解对象
     */
    private ApiCheckInfo checkValue(Object value, ApiParam apiParam, MessageBean bean, ApiField apiField) {
        if (null == value) {
            if (apiParam.required()) {
                return getCheckInfo(apiParam, ApiCheckError.EMPTY, bean.getRequired(), apiField);
            }
            return null;
        }
        if (MultipartFile.class.isAssignableFrom(value.getClass())) {
            MultipartFile file = (MultipartFile) value;
            if (file.isEmpty() && apiParam.required()) {
                return getCheckInfo(apiParam, ApiCheckError.EMPTY, bean.getRequired(), apiField);
            }
        } else if (value.getClass().isInterface()) {
            // 判断是不是接口类型，接口类型只判断是为空
            if (apiParam.required()) {
                return getCheckInfo(apiParam, ApiCheckError.EMPTY, bean.getRequired(), apiField);
            }
        } else if (value.getClass().isPrimitive()) {
            // 基本数据类型
            if ("boolean".equals(value.getClass().getTypeName())) {
                // 只支持，是否为空
                return null;
            } else if ("char".equals(value.getClass().getTypeName())) {
                String regexp = getRegexp(apiParam.regexp());
                if (!isEmpty(regexp) && !value.toString().matches(regexp)) {
                    return getCheckInfo(apiParam, ApiCheckError.REGEXP, bean.getRegexp(), apiField);
                }
            } else {
                ApiCheckInfo info = checkValueByNumber(value, apiParam, bean, apiField);
                if (info != null) {
                    return info;
                }
            }
        } else if (Character.class.isAssignableFrom(value.getClass())) {
            // char
            String regexp = getRegexp(apiParam.regexp());
            if (!isEmpty(regexp) && !value.toString().matches(regexp)) {
                return getCheckInfo(apiParam, ApiCheckError.REGEXP, bean.getRegexp(), apiField);
            }
        } else if (Number.class.isAssignableFrom(value.getClass())) {
            return checkValueByNumber(value, apiParam, bean, apiField);
        } else if (Boolean.class.isAssignableFrom(value.getClass())) {
            // 布尔只支持，是否为空
            return null;
        } else if (value.getClass() == String.class) {
            // 字符串类型
            return checkValueByString(value, apiParam, bean, apiField);
        } else if (Date.class.isAssignableFrom(value.getClass())) {
            return checkValueByDate((Date) value, apiParam, bean, apiField);
        } else if (Collection.class.isAssignableFrom(value.getClass())) {
            return checkValueByArray(value, apiParam, bean, apiField);
        } else {
            // 未找到,非基本数据类型或String类型只能校验是否为空
        }
        return null;
    }

    /**
     * 校验值，String 类型
     *
     * @param value
     * @param apiParam
     * @param bean
     * @param apiField
     * @return
     */
    private ApiCheckInfo checkValueByString(Object value, ApiParam apiParam, MessageBean bean, ApiField apiField) {
        // 字符串类型
        String str = value.toString().trim();

        if (str.equals("")) {
            if (apiParam.required()) {
                return getCheckInfo(apiParam, ApiCheckError.EMPTY, bean.getRequired(), apiField);
            }
            return null;
        }

        if (str.length() < apiParam.min()) {
            return getCheckInfo(apiParam, ApiCheckError.MIN, bean.getMin(), apiField);
        }

        if (str.length() > apiParam.max()) {
            return getCheckInfo(apiParam, ApiCheckError.MAX, bean.getMax(), apiField);
        }

        // 字符串类型
        String regexp = getRegexp(apiParam.regexp());
        if (!isEmpty(regexp) && !str.matches(regexp)) {
            return getCheckInfo(apiParam, ApiCheckError.REGEXP, bean.getRegexp(), apiField);
        }
        return null;
    }

    /**
     * 校验数组及集合
     *
     * @param value
     * @param apiParam
     * @param bean
     * @param apiField
     * @return
     */
    private ApiCheckInfo checkValueByArray(Object value, ApiParam apiParam, MessageBean bean, ApiField apiField) {
        if (value.getClass().isArray()) { // 判断是不是数组
            Object[] values = (Object[]) value;
            for (Object obj : values) {
                ApiCheckInfo info = checkValue(obj, apiParam, bean, apiField);
                if (null != info) {
                    return info;
                }
            }
            return null;
        }
        Collection<Object> collection = null;
        if (List.class.isAssignableFrom(value.getClass())) {
            collection = (List<Object>) value;
        } else if (Set.class.isAssignableFrom(value.getClass())) {// 判断是不是 set
            collection = (Set<Object>) value;
        }
        if (null == collection || collection.isEmpty()) {
            if (apiParam.required()) {
                return getCheckInfo(apiParam, ApiCheckError.EMPTY, bean.getRequired(), apiField);
            }
            return null;
        }
        for (Object obj : collection) {
            ApiCheckInfo info = checkValue(obj, apiParam, bean, apiField);
            if (null != info) {
                return info;
            }
        }

        return null;
    }


    /**
     * 校验数据类型
     *
     * @param value
     * @param apiParam
     * @param bean
     * @param apiField
     * @return
     */
    private ApiCheckInfo checkValueByNumber(Object value, ApiParam apiParam, MessageBean bean, ApiField apiField) {
        // 数字类型：转 double 就够了
        Double dou = Double.parseDouble(value.toString());

        if (dou < apiParam.min()) {
            return getCheckInfo(apiParam, ApiCheckError.MIN, bean.getMin(), apiField);
        }

        if (dou > apiParam.max()) {
            return getCheckInfo(apiParam, ApiCheckError.MAX, bean.getMax(), apiField);
        }
        String regexp = getRegexp(apiParam.regexp());
        if (!isEmpty(regexp) && !value.toString().matches(regexp)) {
            return getCheckInfo(apiParam, ApiCheckError.REGEXP, bean.getRegexp(), apiField);
        }
        return null;
    }

    /**
     * 校验日期类型
     *
     * @param value
     * @param apiParam
     * @param bean
     * @param apiField
     * @return
     */
    private ApiCheckInfo checkValueByDate(Date value, ApiParam apiParam, MessageBean bean, ApiField apiField) {
        String regexp = getRegexp(apiParam.regexp());
        if (!isEmpty(regexp)) {
            SimpleDateFormat sdf = null;
            String key = apiParam.regexp();
            if (key.startsWith("${") && key.endsWith("}") && key.length() > 3) {
                key = key.substring(2, key.length() - 1);
            } else {
                // 判断是否自定义了 格式化方式
                if (isEmpty(config.getDateFormat())) {
                    key = MessageBean.DEFAULT;
                } else {
                    key = MessageBean.CUSTOM;
                }
            }
            try {
                // 区分是否是自定义，或默认
                if (MessageBean.CUSTOM.equals(key)) {
                    sdf = new SimpleDateFormat(config.getDateFormat());
                } else if (key.startsWith(MessageBean.DEFAULT)) {
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

                Date date = value;

                String str = sdf.format(date);
                if (!str.matches(regexp)) {
                    return getCheckInfo(apiParam, ApiCheckError.REGEXP, bean.getRegexp(), apiField);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    /**
     * 获致正则表达式
     *
     * @param regexp
     * @return
     */
    private String getRegexp(String regexp) {
        if (isEmpty(regexp)) {
            return null;
        }
        if ((regexp.startsWith("${") || regexp.startsWith("#{")) && regexp.endsWith("}")) {
            String key = regexp.substring(2, regexp.length() - 1);
            String reg = config.getRegexps().get(key);
            if (isEmpty(reg)) {
                logger.error("参数校验：{} 正则表达式,不存在", regexp);
                return null;
            }
            return reg;
        }
        return regexp;
    }

    /**
     * 获取错误消息
     *
     * @param apiParam
     * @return
     */
    private MessageBean getMessage(ApiParam apiParam) {
        MessageBean bean = null;
        if (!isEmpty(apiParam.message())) {
            String key = apiParam.message();
            if (key.startsWith("${") && key.endsWith("}")) {
                if (key.length() <= 3) {
                    key = MessageBean.DEFAULT;
                } else {
                    key = key.substring(2, key.length() - 1);
                }
            }
            bean = config.getMessages().get(key);
        }

        if (null == bean) {
            bean = config.getMessages().get(MessageBean.DEFAULT);
        }
        return bean;
    }

    /**
     * 生成校验详情
     *
     * @param apiParam
     * @param error
     * @param message
     * @param apiField
     * @return
     */
    private ApiCheckInfo getCheckInfo(ApiParam apiParam, ApiCheckError error, String message, ApiField apiField) {
        ApiCheckInfo info = new ApiCheckInfo();
        info.setApiParam(apiParam);
        info.setError(error);
        info.setField(apiParam.field());
        info.setMessage(message);
        info.setName(apiParam.name());
        if (null == apiParam.name() || apiParam.name().trim().equals("")) {
            if (null == apiField) {
                info.setName(apiParam.field());
            } else {
                info.setName(apiField.name());
            }
        }
        return info;
    }

    /**
     * 获取属性
     *
     * @param cla
     * @param key
     * @return
     */
    public static Field getField(Class<?> cla, String key) {
        Field field = null;
        try {
            field = cla.getDeclaredField(key);
        } catch (NoSuchFieldException ex) {
            logger.debug("获取Field异常", ex);
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
     * @param str 字符串
     * @return 结果： true 为空 ,false 不为空
     */
    public static boolean isEmpty(String str) {
        return null == str || "".equals(str.trim());
    }
}
