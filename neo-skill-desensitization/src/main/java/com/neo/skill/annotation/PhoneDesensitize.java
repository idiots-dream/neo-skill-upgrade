package com.neo.skill.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.neo.skill.core.PhoneDesensitization;

import java.lang.annotation.*;

/**
 * @author blue-light
 * Date 2022-09-27
 * Description 电话脱敏注解
 */
@Documented
@JacksonAnnotationsInside
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Desensitize(desensitization = PhoneDesensitization.class)
public @interface PhoneDesensitize {

}
