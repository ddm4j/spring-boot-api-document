package com.github.ddm4j.api.document.utils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.github.ddm4j.api.document.bean.ParamBaseVo;
import com.github.ddm4j.api.document.bean.ParamChildrenVo;
import com.github.ddm4j.api.document.common.model.FieldType;

public class FieldUtil {

	public static Type extractGenType(Type type, Type genType, FieldType ct) {
		if (null == type) {
			return null;
		}
		switch (ct) {
		case T:
		case ArrayT:
		case ClassT:
			if (type instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) type;
				if (null != pt.getActualTypeArguments() && pt.getActualTypeArguments().length >= 1) {

					Type t1 = pt.getActualTypeArguments()[0];
					if (t1 instanceof Class<?>) {
						if ((Class<?>) t1 == Object.class) {
							return null;
						} else {
							return t1;
						}
					} else if (t1 instanceof ParameterizedType) {

						return t1;
					} else {
						if (null != genType) {
							if (genType instanceof Class<?>) {
								return (Class<?>) genType;
							} else if (genType instanceof ParameterizedType) {
								pt = (ParameterizedType) genType;
								if (null == pt.getOwnerType()) {
									return pt.getRawType();
								} else {
									return pt.getActualTypeArguments()[0];
								}
							}
						}

						return genType;
					}
				}
			} else if (genType instanceof ParameterizedType) {

				ParameterizedType pt = (ParameterizedType) genType;
				if (null != pt.getActualTypeArguments() && pt.getActualTypeArguments().length >= 1) {
					return pt.getActualTypeArguments()[0];
				}
				return null;
			}
			break;
		case ArrayClassT:
			GenericArrayType gat = (GenericArrayType) type;
			ParameterizedType pt = (ParameterizedType) gat.getGenericComponentType();

			pt = (ParameterizedType) pt.getRawType();
			if (null != pt.getActualTypeArguments() && pt.getActualTypeArguments().length >= 1) {
				Type t1 = pt.getActualTypeArguments()[0];
				if (t1 instanceof Class) {
					if ((Class<?>) t1 == Object.class) {
						return null;
					} else {
						return t1;
					}
				} else {
					return null;
				}
			}
			return null;
		default:
			return null;
		}
		return null;
	}

	public static Class<?> extractClassByType(Type type, Type genType, FieldType ct) {
		Class<?> cla = null;
		if (FieldType.Clazz == ct) {
			cla = (Class<?>) type;
		} else if (FieldType.ArrayClass == ct) {
			cla = (Class<?>) type;
			// 取出具体类型： String[] 中的 String
			cla = cla.getComponentType();
		} else if (FieldType.ArrayClassT == ct) {
			// 数组
			GenericArrayType gat = (GenericArrayType) type;
			ParameterizedType pt = (ParameterizedType) gat.getGenericComponentType();
			cla = (Class<?>) pt.getRawType();
		} else if (FieldType.ClassT == ct) {
			// 自定义泛型
			if (type instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) type;
				Type t1 = pt.getActualTypeArguments()[0];

				if (t1 instanceof Class) {

					if ((Class<?>) t1 == Object.class) {

						return null;
					} else {
						return (Class<?>) t1;
					}
				} else {
					return null;
				}
			} else if (genType instanceof ParameterizedType) {
				// 使用的泛型采用类上面指定的
				ParameterizedType pt = (ParameterizedType) genType;
				cla = (Class<?>) pt.getActualTypeArguments()[0];
			}

		} else if (FieldType.T == ct) {

			if (null != genType) {
				if (genType instanceof Class<?>) {
					return (Class<?>) genType;
				} else if (genType instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) genType;
					return (Class<?>) pt.getRawType();

				}
				if (genType.getTypeName().equals("?")) {
					return null;
				}
				return (Class<?>) genType;
			}
		} else if (FieldType.ArrayT == ct) {
			if (null != genType) {
				if (genType instanceof Class<?>) {
					return (Class<?>) genType;
				} else if (genType instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) genType;
					if (null == pt.getOwnerType()) {
						return (Class<?>) pt.getRawType();
					} else {
						cla = (Class<?>) pt.getActualTypeArguments()[0];
					}
				}
			}
		}
		return cla;
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
	@SuppressWarnings("unchecked")
	public static <T extends ParamChildrenVo<?>> void removeField(List<T> vos, String field) {

		String[] keys = field.split("\\.");
		List<T> tempChildren = vos;

		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			for (int j = 0; j < tempChildren.size(); j++) {
				if (tempChildren.get(j).getField().equals(key)) {
					if (i == keys.length - 1) {
						tempChildren.remove(j);
						return;
					} else {
						tempChildren = (List<T>) tempChildren.get(j).getChildren();
					}
					break;
				}
			}
		}
	}

	/**
	 * 判断是否重复
	 * 
	 * @param vos
	 *            集合数据
	 * @param vo
	 *            查询对象
	 * @param <T>
	 *            具体数据
	 * @return 处理后的数据
	 */
	public static <T extends ParamBaseVo> boolean isDuplicate(List<T> vos, ParamBaseVo vo) {
		if (null != vos && vos.size() > 0 && null != vo) {
			for (int i = 0; i < vos.size(); i++) {
				if (vos.get(i).getField().equals(vo.getField())) {
					return true;
				}
			}
		}

		return false;
	}

	public static FieldType checkFieldType(Type type) {

		if (type instanceof ParameterizedType) {
			// MyClass<T> 普通泛型
			return FieldType.ClassT;
		} else if (type instanceof GenericArrayType) {
			// MyClass<T>[] 或 T[]
			GenericArrayType gat = (GenericArrayType) type;

			if (gat.getGenericComponentType() instanceof ParameterizedType) {
				// MyClass<T>[] 泛型数组
				return FieldType.ArrayClassT;
			} else {
				// T[] 纯泛型数组
				return FieldType.ArrayT;
			}
		} else if (type instanceof Class) {
			// MyClass
			if (((Class<?>) type).isArray()) {
				// MyClass[] 普通数组
				return FieldType.ArrayClass;
			}
			// MyClass 不是泛型
			return FieldType.Clazz;
		} else {
			// T 纯泛型
			return FieldType.T;
		}
		//
	}

	public static int checkBaseClass(Class<?> cla) {
		if (null == cla) {
			return -2;
		}
		if (Number.class.isAssignableFrom(cla)) {
			return 1;
		} else if (String.class.isAssignableFrom(cla)) {
			return 2;
		} else if (Enum.class.isAssignableFrom(cla)) {
			return 3;
		} else if (List.class.isAssignableFrom(cla)) {
			return 4;
		} else if (Set.class.isAssignableFrom(cla)) {
			return 6;
		} else if (Date.class.isAssignableFrom(cla)) {
			return 7;
		} else if (cla.isInterface()) {
			return 0;
		}
		return -1;
	}

	public static boolean isEmpty(String str) {
		if (null == str || "".equals(str.trim())) {
			return true;
		}
		return false;
	}
}
