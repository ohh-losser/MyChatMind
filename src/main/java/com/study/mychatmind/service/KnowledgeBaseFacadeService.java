package com.study.mychatmind.service;

import com.study.mychatmind.model.request.CreateKnowledgeBaseRequest;
import com.study.mychatmind.model.request.UpdateKnowledgeBaseRequest;
import com.study.mychatmind.model.response.CreateKnowledgeBaseResponse;
import com.study.mychatmind.model.response.GetKnowledgeBasesResponse;

public interface KnowledgeBaseFacadeService {

    /**
     * 获取所有知识库
     */
    GetKnowledgeBasesResponse getKnowledgeBases();

    /**
     * 创建知识库
     */
    CreateKnowledgeBaseResponse createKnowledgeBase(CreateKnowledgeBaseRequest request);

    /**
     * 删除知识库
     */
    void deleteKnowledgeBase(String knowledgeBaseId);

    /**
     * 更新知识库
     */
    void updateKnowledgeBase(String knowledgeBaseId, UpdateKnowledgeBaseRequest request);
}
