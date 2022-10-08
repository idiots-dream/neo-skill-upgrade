package com.neo.skill.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author blue-light
 * Date 2022-09-27
 * Description 脱敏工厂类
 * 创建脱敏对象的实例
 */
public class DesensitizationFactory {
    private DesensitizationFactory() {
    }

    private static final Map<Class<?>, Desensitization<?>> MAP = new HashMap<>();

    @SuppressWarnings("all")
    public static Desensitization<?> getDesensitization(Class<?> clazz) {
        if (clazz.isInterface()) {
            throw new UnsupportedOperationException("desensitization is interface, what is expected is an implementation class !");
        }
        /**
         * computeIfAbsent(K key, Function remappingFunction) 方法对 hashMap 中指定 key 的值进行重新计算，如果不存在这个 key，则添加到 hashMap
         * 参数说明：
         * key - 键
         * remappingFunction - 重新映射函数，用于重新计算值
         */
        return MAP.computeIfAbsent(clazz, k -> {
            try {
                // newInstance()是使用类加载机制创建一个实例。
                // newInstance(): 弱类型。低效率。用于实现IOC、反射、面对接口编程和依赖倒置等技术方法
                // new: 强类型。相对高效。能调用任何public构造。new只能实现具体类的实例化，不适合于接口编程
                return (Desensitization<?>) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new UnsupportedOperationException(e.getMessage(), e);
            }
        });
    }
}
