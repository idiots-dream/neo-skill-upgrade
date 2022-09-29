package com.neo.skill.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.neo.skill.core.Desensitization;
import com.neo.skill.serializer.ObjectDesensitizeSerializer;

import java.lang.annotation.*;

/**
 * @author blue-light
 * Date 2022-09-27
 * Description 脱敏实现策略
 * -@JsonSerialize注解：表示使用自定义序列化模式
 */
@Documented
@JacksonAnnotationsInside
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@JsonSerialize(using = ObjectDesensitizeSerializer.class)
public @interface Desensitize {
    /**
     * 使用的脱敏器具体实现
     */
    @SuppressWarnings("all")
    Class<? extends Desensitization<?>> desensitization();
}
