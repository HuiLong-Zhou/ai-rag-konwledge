package com.zhl.dev.trigger.http;

import com.zhl.dev.api.IRAGService;
import com.zhl.dev.api.resource.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *
 * @author zhl
 * @since 2026-02-04 10:44
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/rag/")
public class RAGController implements IRAGService {
    @Resource
    private OllamaChatClient chatClient;
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private SimpleVectorStore simpleVectorStore;
    @Resource
    private PgVectorStore pgVectorStore;

    @Resource
    private RedissonClient redissonClient;

    @RequestMapping(value = "query_rag_tag_list", method = RequestMethod.GET)
    @Override
    public Response<List<String>> queryRAGTagList() {
        RList<String> elements = redissonClient.getList("ragTag");
        return Response.<List<String>>builder()
            .code("000")
            .data(elements)
            .info("调用成功")
            .build();
    }

    @RequestMapping(value = "upload_file", method = RequestMethod.GET, headers = "content-type=multipart/form-data")
    @Override
    public Response<String> uploadRAGFile(@RequestParam String ragTag, @RequestParam List<MultipartFile> files) {

        log.info("上传知识库开始: {}", ragTag);
        for (MultipartFile file : files) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = documentReader.get();
            List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

            // 给RAG做标记
            documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));
            RList<String> elements = redissonClient.getList("ragTag");
            // 暂时存放到 Redis 中
            if(!elements.contains(ragTag)) {
                elements.add(ragTag);
            }

        }
        log.info("上传知识库完成: {}", ragTag);
        return Response.<String>builder()
            .code("000")
            .info("调用完成")
            .build();
    }
}
