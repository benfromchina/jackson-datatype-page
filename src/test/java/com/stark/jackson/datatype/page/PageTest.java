package com.stark.jackson.datatype.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;

/**
 * 测试 {@link Page} 的序列化和反序列化
 *
 * @author Ben
 * @version 1.0.0
 * @since 2023/11/15
 */
public class PageTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules();

    @Test
    public void testPage() throws JsonProcessingException {
        User user = new User(1L, "张三");
        Page<User> page = new PageImpl<>(Collections.singletonList(user), PageRequest.of(0, 10), 1);
        String json = OBJECT_MAPPER.writeValueAsString(page);

        page = OBJECT_MAPPER.readValue(json, new TypeReference<Page<User>>() {});
        assert !page.getContent().isEmpty();
        User userNew = page.getContent().get(0);
        assert userNew.getId().equals(user.getId());
        assert userNew.getName().equals(user.getName());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class User {

        private Long id;

        private String name;

    }

}
