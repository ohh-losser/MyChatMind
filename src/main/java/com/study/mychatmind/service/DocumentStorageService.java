package com.study.mychatmind.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface DocumentStorageService {

    /**
     * 保存上传的文件
     *
     * @param kbId       知识库ID
     * @param documentId 文档ID
     * @param file       上传的文件
     * @return 保存的文件相对路径
     * @throws IOException 文件保存失败
     */

    String saveFile(String kbId, String documentId, MultipartFile file) throws IOException;

    void deleteFile(String filePath) throws IOException;

    Path getFilePath(String filePath);

    boolean fileExists(String filePath);
}
