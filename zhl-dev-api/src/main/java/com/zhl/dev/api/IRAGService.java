package com.zhl.dev.api;

import com.zhl.dev.api.resource.Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *
 * @author zhl
 * @since 2026-02-04 10:38
 */
public interface IRAGService {
    /**
     * 查询 RAGTag
     * @return 一系列RAG知识库Tag
     */
    Response<List<String>> queryRAGTagList();

    /**
     * 上传一系列RAG知识库
     * @param ragTag RAG知识库标签
     * @param files RAG知识库
     * @return RAG知识库标签
     */
    Response<String> uploadRAGFile(String ragTag, List<MultipartFile> files);
}
