package com.study.mychatmind.service.impl;

import com.study.mychatmind.mapper.ChunkBgeM3Mapper;
import com.study.mychatmind.model.entity.ChunkBgeM3;
import com.study.mychatmind.service.RagService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class RagServiceImpl implements RagService {
    private final WebClient webClient;
    private final ChunkBgeM3Mapper chunkBgeM3Mapper;
    private final String embeddingModel;
    public RagServiceImpl(
            WebClient.Builder builder,
            ChunkBgeM3Mapper chunkBgeM3Mapper,
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.embedding-model}") String embeddingModel
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.chunkBgeM3Mapper = chunkBgeM3Mapper;
        this.embeddingModel = embeddingModel;
    }
    @Override
    public float[] embed(String text) {
        EmbeddingResponse resp = webClient.post()
                .uri("/api/embeddings")
                .bodyValue(Map.of(
                        "model", embeddingModel,
                        "prompt", text
                ))
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .block();
        Assert.notNull(resp, "Embedding response cannot be null");
        return resp.getEmbedding();
    }
    @Override
    public List<String> similaritySearch(String kbId, String query) {
        float[] queryEmbedding = embed(query);
        String vectorLiteral = toPgVector(queryEmbedding);
        List<ChunkBgeM3> chunks = chunkBgeM3Mapper.similaritySearch(kbId,
                vectorLiteral, 3);
        return chunks.stream().map(ChunkBgeM3::getContent).toList();
    }
    /**
     * 把 float[] 转成 pgvector 的字符串格式 [0.1,0.2,...]
     */
    private String toPgVector(float[] v) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < v.length; i++) {
            sb.append(v[i]);
            if (i < v.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Ollama embedding API 的响应结构
     */
    @Data
    private static class EmbeddingResponse {
        private float[] embedding;
    }
}