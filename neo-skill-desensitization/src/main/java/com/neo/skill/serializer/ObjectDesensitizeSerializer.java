package com.neo.skill.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.neo.skill.annotation.Desensitize;
import com.neo.skill.core.Desensitization;
import com.neo.skill.core.DesensitizationFactory;
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

    /**
     * 自定义注解被拦截后的回调函数
     * 第一个参数：表示序列化器提供者，用于获取序列化配置或者其他序列化器
     * 第二个参数：表示代表这个属性的方法或者字段，用于获取要序列化的值。
     * 返回：该方法的返回结果是一个序列化器，根据所要实现的序列化行为来决定是返回当前序列化器还是新建一个序列化器，从而改变序列化时的行为。
     */
    @Override
    public JsonSerializer<Object> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property == null) {
            return prov.findNullValueSerializer(null);
        }
        Desensitize annotation = property.getAnnotation(Desensitize.class);
        // 注解使用的序列化策略
        // annotation.desensitization():脱敏注解中指定的脱敏策略
        return createContextual(annotation.desensitization());
    }

    @SuppressWarnings("unchecked")
    public JsonSerializer<Object> createContextual(Class<? extends Desensitization<?>> clazz) {
        ObjectDesensitizeSerializer serializer = new ObjectDesensitizeSerializer();
        // 为序列化对象设置脱敏策略
        serializer.setDesensitization((Desensitization<Object>) DesensitizationFactory.getDesensitization(clazz));
        return serializer;
    }

    /**
     * 序列化的逻辑处理
     * 第一个参数：表示的是被序列化的类型的值
     * 第二个参数：表示的是用于输出生成的Json内容
     * 第三个参数：表示序列化器提供者，用于获取序列化配置或者其他序列化器
     * 将想要序列化的字符串传入 jsonGenerator.writeString()方法的参数中
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
