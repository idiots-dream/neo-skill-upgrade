package com.neo.skill.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author blue-light
 * Date 2022-09-27
 * Description
 */
@Slf4j
public class JsonMapperUtil {
    private final ObjectMapper mapper;

    public JsonMapperUtil() {
        this(null);
    }

    public JsonMapperUtil(JsonInclude.Include include) {
        mapper = new ObjectMapper();
        // 设置输出时包含属性的风格
        if (include != null) {
            mapper.setSerializationInclusion(include);
        }

        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 创建只输出非Null的属性到Json字符串的Mapper.
     *
     * @return jsonMapper
     */
    public static JsonMapperUtil nonNullMapper() {
        return new JsonMapperUtil(JsonInclude.Include.NON_NULL);
    }

    /**
     * Object可以是POJO，也可以是Collection或数组。 如果对象为Null, 返回"null". 如果集合为空集合, 返回"[]".
     *
     * @param object pojo对象
     * @return jsonString
     */
    public String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            log.warn("write to json string error:" + object, e);
            return null;
        }
    }
}
