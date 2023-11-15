package com.stark.jackson.datatype.page;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 Page 的序列化和反序列化解析器。
 * 解决由于默认实现 PageImpl 没有无参构造方法造成无法序列化的问题。
 *
 * @author Ben
 * @version 1.0.0
 * @since 2023/11/15
 */
public class PageCombinedSerializer {

    @SuppressWarnings("rawtypes")
    public static class PageSerializer extends JsonSerializer<Page> {

        @Override
        public void serialize(Page page, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            // 泛型类型
            gen.writeStringField("type", page.getContent().isEmpty() ? Object.class.getName() : page.getContent().get(0).getClass().getName());
            // 分页数据
            gen.writeObjectField("content", page.getContent());
            // 分页信息
            gen.writeFieldName("pageable");
            gen.writeStartObject();
            gen.writeNumberField("pageNumber", page.getPageable().getPageNumber());
            gen.writeNumberField("pageSize", page.getPageable().getPageSize());
            // 排序
            gen.writeFieldName("sort");
            gen.writeStartArray();
            page.getPageable().getSort().forEach(order -> {
                try {
                    gen.writeStartObject();
                    gen.writeStringField("property", order.getProperty());
                    gen.writeStringField("direction", order.getDirection().name());
                    gen.writeEndObject();
                } catch (IOException e) {
                    throw new RuntimeException("org.springframework.data.domain.Sort.Order 序列化失败", e);
                }
            });
            gen.writeEndArray();
            gen.writeEndObject();
            // 总数
            gen.writeNumberField("totalElements", page.getTotalElements());
            // 总页数
            gen.writeNumberField("totalPages", page.getTotalPages());
            gen.writeEndObject();
        }

    }

    @SuppressWarnings("rawtypes")
    public static class PageDeserializer extends JsonDeserializer<Page> {

        @SuppressWarnings("unchecked")
        @Override
        public Page deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode root = p.getCodec().readTree(p);
            // 泛型类型
            String type = root.get("type").asText();
            // 分页数据
            List content = new ArrayList<>();
            if (!root.get("content").isEmpty()) {
                Class<?> clazz;
                try {
                    clazz = Class.forName(type);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("找不到 " + type + " 类", e);
                }
                JsonParser parser;
                for (JsonNode object : root.get("content")) {
                    try {
                        parser = p.getCodec().getFactory().createParser(object.toString());
                        content.add(p.getCodec().readValue(parser, clazz));
                    } catch (Exception e) {
                        throw new RuntimeException(type + " 反序列化失败", e);
                    }
                }
            }
            // 分页信息
            int pageNumber = root.get("pageable").get("pageNumber").asInt();
            int pageSize = root.get("pageable").get("pageSize").asInt();
            // 排序
            List<Sort.Order> orders = new ArrayList<>();
            root.get("pageable").get("sort").forEach(order ->
                    orders.addAll(Sort.by(Sort.Direction.valueOf(order.get("direction").asText()), order.get("property").asText()).toList()));
            Sort sort = Sort.by(orders);
            // 总数
            int total = root.get("totalElements").asInt();

            return new PageImpl<>(content, PageRequest.of(pageNumber, pageSize, sort), total);
        }

    }

}