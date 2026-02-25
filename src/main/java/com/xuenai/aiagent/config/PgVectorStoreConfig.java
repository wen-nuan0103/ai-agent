package com.xuenai.aiagent.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class PgVectorStoreConfig {
    
    @Bean
    public VectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        return PgVectorStore.builder(jdbcTemplate,dashscopeEmbeddingModel)
                .dimensions(1536)                    // 向量维度，通义千问embedding模型输出1536维向量
                .distanceType(COSINE_DISTANCE)       // 使用余弦距离计算向量相似度
                .indexType(HNSW)                     // 使用HNSW索引算法，提高检索速度
                .initializeSchema(true)              // 启动时自动创建向量表结构
                .schemaName("public")                // PostgreSQL的模式名称
                .vectorTableName("vector_store")     // 存储向量的表名
                .maxDocumentBatchSize(10000)         // 批量插入的最大文档数
                .build();
    }
    
    
    
}
