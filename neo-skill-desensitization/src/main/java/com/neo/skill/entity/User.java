package com.neo.skill.entity;

import com.neo.skill.annotation.PhoneDesensitize;
import lombok.Builder;
import lombok.Data;

/**
 * @author blue-light
 * Date 2022-09-27
 * Description
 */
@Data
@Builder
public class User {
    private String username;

    @PhoneDesensitize
    private String phone;
}
