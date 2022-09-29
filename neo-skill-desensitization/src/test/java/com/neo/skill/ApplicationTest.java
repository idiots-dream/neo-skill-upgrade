package com.neo.skill;

import com.neo.skill.entity.User;
import com.neo.skill.util.JsonMapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author blue-light
 * Date 2022-09-27
 * Description
 */
@Slf4j
@SpringBootTest
public class ApplicationTest {
    @Test
    public void test1() {
        User user = User.builder().phone("15452983019").username("汪家杰").build();
        log.info("{}", JsonMapperUtil.nonNullMapper().toJson(user));
    }
}
