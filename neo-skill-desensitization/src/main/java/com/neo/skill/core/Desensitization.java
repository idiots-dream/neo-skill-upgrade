package com.neo.skill.core;

/**
 * @author blue-light
 * Date 2022-09-27
 * Description 顶级脱敏器
 */
public interface Desensitization<T> {
    /**
     * 脱敏实现
     *
     * @param target 脱敏对象
     * @return 脱敏返回结果
     */
    T desensitize(T target);
}
