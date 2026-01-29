package com.xuenai.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 加载Markdown文档
 */
@Slf4j
@Component
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkdowns() {
        List<Document> documents = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:documents/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        //遇到水平分割线（Horizontal Rule，即 Markdown 中的 ---、*** 或 ___）时，强制拆分并创建一个新的 Document 对象
                        .withHorizontalRuleCreateDocument(true)
                        //否保留 Markdown 中的代码块（即被 ``` 包裹的内容）
                        .withIncludeCodeBlock(false)
                        //否保留 Markdown 中的引用块（即被 > 包裹的内容）
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                documents.addAll(reader.get());
            }
        } catch (Exception e) {
            log.error("Failed to load markdown documents", e);
        }
        return documents;
    }

}
