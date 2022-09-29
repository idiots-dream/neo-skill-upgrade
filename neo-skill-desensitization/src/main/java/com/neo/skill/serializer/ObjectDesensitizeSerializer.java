package com.neo.skill.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.neo.skill.annotation.Desensitize;
import com.neo.skill.core.Desensitization;
import com.neo.skill.core.DesensitizationFactory;
import com.neo.skill.core.StringDesensitization;
import com.neo.skill.util.Symbol;

import java.io.IOException;

/**
 * @author blue-light
 * Date 2022-09-27
 * Description 脱敏序列化器
 * 自定义json序列化需要实现 StdSerializer<T> 或者 JsonSerializer<T>
 */
public class ObjectDesensitizeSerializer extends StdSerializer<Object> implements ContextualSerializer {

    /**
     * 使用transient关键字修饰的对象不允许被序列化
     */
    private transient Desensitization<Object> desensitization;

    protected ObjectDesensitizeSerializer() {
        super(Object.class);
    }

    public Desensitization<Object> getDesensitization() {
        return desensitization;
    }

    public void setDesensitization(Desensitization<Object> desensitization) {
        this.desensitization = desensitization;
    }

    @Override
    public JsonSerializer<Object> createContextual(SerializerProvider prov, BeanProperty property) {
        Desensitize annotation = property.getAnnotation(Desensitize.class);
        return createContextual(annotation.desensitization());
    }

    @SuppressWarnings("unchecked")
    public JsonSerializer<Object> createContextual(Class<? extends Desensitization<?>> clazz) {
        ObjectDesensitizeSerializer serializer = new ObjectDesensitizeSerializer();
        if (clazz != StringDesensitization.class) {
            serializer.setDesensitization((Desensitization<Object>) DesensitizationFactory.getDesensitization(clazz));
        }
        return serializer;
    }

    /**
     * 自定义序列化逻辑
     * @param value
     * @param gen
     * @param provider
     * @throws IOException
     */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Desensitization<Object> objectDesensitization = getDesensitization();
        if (objectDesensitization != null) {
            try {
                gen.writeObject(objectDesensitization.desensitize(value));
            } catch (Exception e) {
                gen.writeObject(value);
            }
        } else if (value instanceof String) {
            gen.writeString(Symbol.getSymbol(((String) value).length(), Symbol.STAR));
        } else {
            gen.writeObject(value);
        }
    }
}
