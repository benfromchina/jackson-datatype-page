package com.stark.jackson.datatype.page;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.data.domain.Page;

/**
 * Page 模块，解决由于默认实现 PageImpl 没有无参构造方法造成无法序列化的问题。
 * <ul>
 *     <li>需要在 <i>ServiceLoader</i> 配置文件中添加全类名 <i>com.stark.jackson.datatype.page.PageModule</i>，文件路径：<i>META-INF/services/com.fasterxml.jackson.databind.Module</i></li>
 * </ul>
 *
 * @author Ben
 * @version 1.0.0
 * @since 2023/11/15
 * @see PageCombinedSerializer
 */
public class PageModule extends SimpleModule {

    private static final long serialVersionUID = 2065578859832380310L;

    public PageModule() {
        addSerializer(Page.class, new PageCombinedSerializer.PageSerializer());
        addDeserializer(Page.class, new PageCombinedSerializer.PageDeserializer());
    }

}
