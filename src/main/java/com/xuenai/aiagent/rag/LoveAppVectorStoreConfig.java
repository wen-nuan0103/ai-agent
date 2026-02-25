package com.xuenai.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader documentLoader;
    
    @Resource
    private VectorStore pgVectorStore;

    /**
     * 将 Markdown 文件存储入向量数据库
     * @return
     */
    @Bean
    VectorStore loveAppVectorStore() {
//        List<Document> documents = documentLoader.loadMarkdowns();
//        if (documents != null && !documents.isEmpty()) {
//            int batchSize = 20; // 灵机模型单次最多处理 25 条，需要进行限制
//            for (int i = 0; i < documents.size(); i += batchSize) {
//                int end = Math.min(i + batchSize, documents.size());
//                List<Document> batch = documents.subList(i, end);
//                this.pgVectorStore.add(batch);
//                System.out.println("成功处理批次: " + i + " 到 " + end);
//            }
//        }
        return pgVectorStore;
    }

}
