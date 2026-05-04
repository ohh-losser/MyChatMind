package com.study.mychatmind.service;

import com.study.mychatmind.model.request.CreateDocumentRequest;
import com.study.mychatmind.model.request.UpdateDocumentRequest;
import com.study.mychatmind.model.response.CreateDocumentResponse;
import com.study.mychatmind.model.response.GetDocumentsResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentFacadeService {
    /**
     * 获取所有文档
     */
    GetDocumentsResponse getDocuments();

    /**
     * 根据知识库ID获取文档列表
     */
    GetDocumentsResponse getDocumentsByKbId(String kbId);

    /**
     * 创建文档记录（不上传文件）
     */
    CreateDocumentResponse createDocument(CreateDocumentRequest request);

    /**
     * 上传文档文件（核心：解析→分块→嵌入→存储）
     */
    CreateDocumentResponse uploadDocument(String kbId, MultipartFile file);

    /**
     * 删除文档
     */
    void deleteDocument(String documentId);

    /**
     * 更新文档
     */
    void updateDocument(String documentId, UpdateDocumentRequest request);
}
