package com.neo.skill.core;

import com.neo.skill.util.Symbol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author blue-light
 * Date 2022-09-27
 * Description 手机号脱敏器 默认只保留前3位和后4位
 */
public class PhoneDesensitization implements StringDesensitization {

    /**
     * 手机号正则
     * Pattern类用于创建一个正则表达式（创建一个匹配模式）
     * compile(String regex)和compile(String regex,int flags)
     * regex是正则表达式
     * flags为可选模式
     */
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}");

    /**
     * 手机号脱敏 只保留前3位和后4位
     */
    @Override
    public String desensitize(String target) {
        // 符合匹配模式的手机号才能被被脱敏
        Matcher matcher = DEFAULT_PATTERN.matcher(target);
        while (matcher.find()) {
            String group = matcher.group();
            target = target.replace(group, group.substring(0, 3) + Symbol.getSymbol(4, Symbol.STAR) + group.substring(7, 11));
        }
        return target;
    }
}
