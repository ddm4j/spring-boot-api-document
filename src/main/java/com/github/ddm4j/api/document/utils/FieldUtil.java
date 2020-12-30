package com.github.ddm4j.api.document.utils;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.multipart.MultipartFile;

import com.github.ddm4j.api.document.annotation.ApiEnum;
import com.github.ddm4j.api.document.annotation.ApiField;
import com.github.ddm4j.api.document.annotation.ApiIgnore;
import com.github.ddm4j.api.document.bean.ParamChildrenVo;
import com.github.ddm4j.api.document.common.model.FieldInfo;
import com.github.ddm4j.api.document.common.model.KVEntity;

public class FieldUtil {

	private static final int LOVEL = 4;

	/**
	 * 提取具体Field
	 * 
	 * @param type
	 *            要提取的对象
	 * @return 提取结果
	 */

	public static KVEntity<String, List<FieldInfo>> extract(Type type) {
		return extract(type, 0);
	}

	private static KVEntity<String, List<FieldInfo>> extract(Type type, int lovel) {
		//System.out.println("开始：" + type);
		if (null == type) {
			return null;
		}
		boolean isArray = false;
		boolean isT = false;
		Class<?> cla = null;
		if (type instanceof Class) {
			cla = (Class<?>) type;
			//System.out.println(" --- "+cla);
			if (cla.isArray()) {
				isArray = true;
				cla = cla.getComponentType();
				KVEntity<Class<?>, Type> ct = extractGenType(type);
				type = ct.getRight();
				//System.out.println(" ----- 222 "+ct.getRight());
			} else {
				type = null;
			}
		} else {
			isT = true;
			if (type instanceof GenericArrayType) {
				isArray = true;
			}
			//System.out.println("T -- " + isArray + " -- " + type.getTypeName());
			KVEntity<Class<?>, Type> ct = extractGenType(type);
			cla = ct.getLeft();
			type = ct.getRight();
			//System.out.println("cla=" + cla + " --- " + type);
		}

		if (null == cla) {
			return null;
		}

		KVEntity<String, List<FieldInfo>> kv = new KVEntity<String, List<FieldInfo>>();
		String typeStr = "";

		if (Number.class.isAssignableFrom(cla)) {
			typeStr = cla.getSimpleName();
		} else if (String.class.isAssignableFrom(cla)) {
			typeStr = "String";
		} else if (Character.class.isAssignableFrom(cla)) {
			typeStr = "Char";
		} else if (Date.class.isAssignableFrom(cla)) {
			typeStr = "Date";
		} else if (Enum.class.isAssignableFrom(cla)) {
			typeStr = "Enum";

			kv.setRight(extractEnum(cla, null));
		} else if (Boolean.class.isAssignableFrom(cla)) {
			typeStr = "Boolean";
		} else if (cla.isPrimitive()) {
			typeStr = cla.getTypeName();
		} else if (cla.isInterface()) {
			if (cla.isAssignableFrom(MultipartFile.class)) {
				typeStr = "File";
			} else if (Map.class.isAssignableFrom(cla)) {
				typeStr = "Map";
			} else if (List.class.isAssignableFrom(cla) || Set.class.isAssignableFrom(cla)) {
				// System.out.println(cla + " ---- " + type);
				KVEntity<String, List<FieldInfo>> kv2 = null;
				// System.out.println(" isT:" + isT);
				if (isT) {
					kv2 = extract(type, lovel);
				} else {
					KVEntity<Class<?>, Type> ct = extractGenType(type);
					kv2 = extract(ct.getRight(), lovel);
				}

				if (null == kv2) {
					return null;
				}
				typeStr = "Array<" + kv2.getLeft() + ">";
				kv.setRight(kv2.getRight());
			}
		} else {
			//System.out.println(" 解析："+cla.getTypeName()+" --- "+cla.getName());
			if (cla.getTypeName().equals(cla.getName())) {
				typeStr = "Object";
				
				kv.setRight(extractField(cla, type, lovel));
			} else {
				typeStr = "Object<?>";
			}

		}
		if (isArray) {
			typeStr = "Array<" + typeStr + ">";
		}
		kv.setLeft(typeStr);
		return kv;
	}

	/**
	 * 提取具体属性
	 * 
	 * @param cla
	 *            类
	 * @param genType
	 *            指定的泛型
	 * @return 提取结果
	 */
	private static List<FieldInfo> extractField(Class<?> cla, Type genType, int lovel) {
		List<FieldInfo> infos = new ArrayList<FieldInfo>();
		if (lovel >= LOVEL) {
			return null;
		}
		lovel++;

		Field[] fis = cla.getDeclaredFields();
		if (null != fis && fis.length > 0) {
			for (Field field : fis) {
				// 判断是否忽略了，下一个
				ApiIgnore ignore = field.getAnnotation(ApiIgnore.class);
				if (null != ignore) {
					continue;
				}
				// System.out.println("字段："+field.getName());
				// 属性是静态的或Final 修饰的，不处理
				if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				FieldInfo info = null;

				// TODO ApiField afi = field.getAnnotation(ApiField.class);
				ApiField afi = AnnotationUtils.getAnnotation(field, ApiField.class);
				if (null != afi) {
					if (afi.hide()) {
						continue;
					}
					info = new FieldInfo();
					info.setDescribe(afi.value());
				} else {
					info = new FieldInfo();
				}

				info.setName(field.getName());

				Class<?> fie = field.getType();
				String typeStr = null;
				boolean isArray = false;
				// System.out.println("field:" + field.getName() + " --- " + fie);
				if (fie.isArray()) {
					isArray = true;
					fie = fie.getComponentType();
					// System.out.println(" field array:" + field.getName() + " --- " + fie);
				}

				if (Number.class.isAssignableFrom(fie)) {
					typeStr = fie.getSimpleName();
				} else if (String.class.isAssignableFrom(fie)) {
					typeStr = "String";
				} else if (Character.class.isAssignableFrom(fie)) {
					typeStr = "Char";
				} else if (fie.isPrimitive()) {
					typeStr = fie.getTypeName();
				} else if (Boolean.class.isAssignableFrom(fie)) {
					typeStr = "Boolean";
				} else if (Date.class.isAssignableFrom(fie)) {
					typeStr = "Date";
				} else if (Enum.class.isAssignableFrom(fie)) {
					typeStr = "Enum";
					info.setChildren(extractEnum(fie, AnnotationUtils.getAnnotation(field, ApiEnum.class)));
				} else {
					if (MultipartFile.class.isAssignableFrom(fie)) {
						typeStr = "File";
					} else if (Map.class.isAssignableFrom(fie)) {
						typeStr = "Map";
					} else {

						if (lovel >= LOVEL) {
							typeStr = "Loop - Limit";
						} else if (List.class.isAssignableFrom(fie) || Set.class.isAssignableFrom(fie)) {
							// System.out.println("list --- " + field.getName());
							if (null != field.getGenericType()) {

								if (field.getGenericType() instanceof Class<?>) {

									// System.out.println(" class");
									KVEntity<Class<?>, Type> ct = extractGenType(field.getGenericType());
									if (null == ct) {
										// 未指定泛型
										continue;
									}
									KVEntity<String, List<FieldInfo>> kv2 = extract(ct.getRight(), lovel);
									if (null == kv2) {
										continue;
									}
									typeStr = "Array<" + kv2.getLeft() + ">";
									info.setChildren(kv2.getRight());

								} else if (field.getGenericType() instanceof ParameterizedType
										|| field.getGenericType() instanceof GenericArrayType) {
									// System.out.println(" pt type:" + genType);

									KVEntity<Class<?>, Type> ct = extractGenType(field.getGenericType());
									if (null == ct) {
										// 未指定泛型
										continue;
									}

									KVEntity<String, List<FieldInfo>> kv2 = null;
									if (null == ct.getRight()) {
										kv2 = extract(genType, lovel);
									} else {
										kv2 = extract(ct.getRight(), lovel);
									}
									if (null == kv2) {
										continue;
									}
									typeStr = "Array<" + kv2.getLeft() + ">";
									if (field.getGenericType() instanceof GenericArrayType) {
										typeStr = "Array<" + typeStr + ">";
									}
									info.setChildren(kv2.getRight());

								} else {
									// System.out.println("未知 1:" + field.getName());
								}
							}
						} else if (field.getGenericType() instanceof Class<?>) {
							// System.out.println("不是泛型 ---" + field.getName());
							if (Object.class == fie) {
								typeStr = "Object<?>";
							} else {

								typeStr = "Object";
								info.setChildren(extractField(fie, genType, lovel));

							}
						} else if (field.getGenericType() instanceof ParameterizedType
								|| field.getGenericType() instanceof GenericArrayType) {
							// System.out.println("是泛型 ---" + field.getName());
							KVEntity<Class<?>, Type> ct = extractGenType(field.getGenericType());
							if (null == ct) {
								// 未指定泛型
								continue;
							}
							// System.out.println(ct.getLeft()+" --- "+ct.getRight());

							if (null == ct.getLeft()) {
								KVEntity<String, List<FieldInfo>> kv2 = null;
								if (null == ct.getRight()) {

									kv2 = extract(genType, lovel);
								} else {
									kv2 = extract(ct.getRight(), lovel);
								}
								if (null == kv2) {
									continue;
								}
								typeStr = kv2.getLeft();
								// if(field.getGenericType() instanceof GenericArrayType) {
								// typeStr = "Array<" + typeStr + ">";
								// }

								info.setChildren(kv2.getRight());
							} else {
								List<FieldInfo> tList = extractField(ct.getLeft(), genType, lovel);
								typeStr = field.getName();
								info.setChildren(tList);
							}

						} else if (!field.getGenericType().getTypeName().equals(field.getType().getTypeName())) {
							// System.out.println("纯泛型：" + field.getName()+"---
							// "+field.getGenericType()+"genType:"+genType);
							KVEntity<String, List<FieldInfo>> kv2 = extract(genType, lovel);
							if (null == kv2) {
								continue;
							}
							typeStr = kv2.getLeft();
							info.setChildren(kv2.getRight());
						} else {
							// System.out.println("未知 2:" + field.getName());
							typeStr = "Object<?>";
						}
					}
				}
				if (isArray) {
					typeStr = "Array<" + typeStr + ">";
				}
				info.setField(info.getName());
				info.setType(typeStr);
				infos.add(info);
				// System.out.println("提取属性1：" + info.getName() + " --- " + typeStr);
			}
		}

		// 处理父类
		if (null == cla.getSuperclass()) {
			return infos;
		}

		if (Object.class != cla.getSuperclass() && !cla.getSuperclass().isInterface()) {
			List<FieldInfo> list2 = extractField(cla.getSuperclass(), genType, lovel - 1);
			if (null != list2) {
				for (FieldInfo field : list2) {
					infos.add(field);
				}
			}
		}
		return infos;

	}

	/**
	 * 提取枚举类型
	 * 
	 * @param cla
	 *            枚举类
	 * @return 提取结果
	 */
	private static List<FieldInfo> extractEnum(Class<?> cla, ApiEnum aem) {

		List<FieldInfo> infos = new ArrayList<FieldInfo>();

		Object[] objs = cla.getEnumConstants();
		Field[] fies = cla.getFields();
		Method[] mes = cla.getMethods();
		StringBuffer sb = null;
		for (int i = 0; i < fies.length; i++) {
			FieldInfo info = new FieldInfo();

			info.setName(fies[i].getName());
			info.setField(fies[i].getName());
			sb = new StringBuffer();
			boolean append = true;
			for (Method m : mes) {
				if (m.getName().startsWith("get") && !"getDeclaringClass".equals(m.getName())
						&& !"getClass".equals(m.getName())) {
					try {
						append = true;
						if (null != aem && !isEmpty(aem.show()) && m.getName().substring(3).toLowerCase().equals(aem.show())) {
							info.setName(m.invoke(objs[i]).toString());
							append = false;
						} 
						
						if (null != aem && !isEmpty(aem.describe()) && m.getName().substring(3).toLowerCase().equals(aem.describe())) {
							info.setDescribe(m.invoke(objs[i]).toString());
							append = false;
						}
						
						if(append){
							sb.append(m.getName().substring(3).toLowerCase() + ": " + m.invoke(objs[i]) + " ;   ");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (null != sb && sb.length() > 1 && isEmpty(info.getDescribe())) {
				info.setDescribe(sb.toString());
			}
			info.setType("enum");

			infos.add(info);
		}
		return infos;
	}

	/**
	 * 提取类，并分解，泛型
	 * 
	 * @param type
	 *            要分解的类
	 * @return 结果
	 */
	private static KVEntity<Class<?>, Type> extractGenType(Type type) {
		KVEntity<Class<?>, Type> kv = new KVEntity<Class<?>, Type>();

		if (null == type) {
			return null;
		}

		if (type instanceof ParameterizedType) {
			// System.out.println("普通泛型");
			// MyClass<T> 普通泛型
			ParameterizedType pt = (ParameterizedType) type;
			// System.out.println(pt.getRawType() + " ---- " + pt.getOwnerType());

			kv.setLeft((Class<?>) pt.getRawType());

			Type[] types = pt.getActualTypeArguments();
			if (types.length > 1) {
				// System.out.println("不支持多泛型");
			} else {
				kv.setRight(types[0]);
			}

		} else if (type instanceof GenericArrayType) {
			// System.out.println("泛型数组");
			GenericArrayType gat = (GenericArrayType) type;

			// System.out.println(gat.getGenericComponentType());

			if (gat.getGenericComponentType() instanceof ParameterizedType) {
				// MyClass<T>[] 泛型数组
				// System.out.println("泛型数组 1");
				ParameterizedType pt = (ParameterizedType) gat.getGenericComponentType();
				// System.out.println("-- " + pt.getOwnerType() + " --- " + pt.getRawType());

				kv.setLeft((Class<?>) pt.getRawType());
				Type[] types = pt.getActualTypeArguments();
				if (types.length > 1) {
					// System.out.println("不支持多泛型");
				} else {
					kv.setRight(types[0]);
				}
			} else {
				// System.out.println("泛型数组 2");
				// T[] 纯泛型数组
			}
		} else if (type instanceof Class) {
			// System.out.println("class");
			kv.setLeft((Class<?>) type);
			if (kv.getLeft().isArray()) {
				// System.out.println(kv.getLeft().getComponentType());
				kv.setLeft(kv.getLeft().getComponentType());
			}

		} else {
			// T 纯泛型
			// System.out.println("T");
			return null;
		}

		if (null != kv.getRight()) {
			if (kv.getRight() instanceof ParameterizedType || kv.getRight() instanceof GenericArrayType
					|| kv.getRight() instanceof Class<?>) {

			} else {
				// System.out.println("取指定类型,不符合 genType:" + kv.getRight());
				kv.setRight(null);
			}
		}

		// System.out.println("取指定类型：" + kv.getLeft() + " ---- " + kv.getRight());
		return kv;
	}

	/**
	 * 删除指定属性
	 * 
	 * @param vos
	 *            list
	 * @param field
	 *            filed
	 * @param <T>
	 *            删除后的数据
	 */
	public static <T extends ParamChildrenVo> List<T> removeField(List<T> vos, String field) {
		if (null == vos) {
			return null;
		}
		String[] keys = field.split("\\.");
		return removeField(vos, keys, 0);
	}

	public static <T extends ParamChildrenVo> List<T> removeField(List<T> vos, String[] keys, int index) {
		int strIndex = keys[index].indexOf("*");
		for (int i = 0; i < vos.size(); i++) {
			T vo = vos.get(i);
			boolean isOk = false;
			if (strIndex > -1) {
				if (strIndex == 0) {
					if (vo.getField().endsWith(keys[index].substring(1))) {
						isOk = true;
					}
				} else if (strIndex == keys[index].length() - 1) {
					if (vo.getField().startsWith(keys[index].substring(0, keys[index].length() - 1))) {
						isOk = true;
					}
				}
			} else// 匹配模式
			if (keys[index].equals(vo.getField())) {
				isOk = true;
			}
			if (isOk && keys.length > index + 1 && null != vo.getChildren() && vo.getChildren().size() > 0) {
				vo.setChildren(removeField(vo.getChildren(), keys, index + 1));
			}
			// 匹配上，就删除
			if (isOk && index >= keys.length - 1) {
				vos.remove(i);
				i--;
			}
		}
		return vos;

	}

	/**
	 * 是否为空
	 * 
	 * @param str
	 *            判断的字符串
	 * @return 处理结果
	 */
	public static boolean isEmpty(String str) {
		if (null == str || "".equals(str.trim())) {
			return true;
		}
		return false;
	}

}
