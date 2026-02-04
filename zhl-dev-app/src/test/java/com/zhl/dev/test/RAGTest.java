package com.zhl.dev.test;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.antlr.runtime.Token;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * @author zhl
 * @since 2026-02-03 13:12
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RAGTest {

    @Resource
    private OllamaChatClient chatClient;
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private SimpleVectorStore simpleVectorStore;
    @Resource
    private PgVectorStore pgVectorStore;


    /**
     * 上传到向量库
     */
    @Test
    public void testUpload(){
        TikaDocumentReader documentReader = new TikaDocumentReader("./data/file.text");
        List<Document> documents = documentReader.get();
        // 切割文件
        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

        // 给知识库做标记
        documents.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));
        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));

        // 将切割后的知识库存储到向量库
        pgVectorStore.accept(documentSplitterList);

        log.info("上传完成");
    }

    /**
     * 测试对话
     */
    @Test
    public void testChat(){
        // 1.用户提问阶段
        String message = "威龙，哪年出生？";
        // 2.系统提示词模板
        String SYSTEM_PROMPT = """
                    Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                    If unsure, simply state that you don't know.
                    Another thing you need to note is that your reply must be in Chinese!
                    DOCUMENTS:
                        {documents}
            """;
        // 3.向量检索阶段
        // 筛选特定的知识库
        SearchRequest request = SearchRequest.query(message)
            .withTopK(5)
            .withFilterExpression("knowledge == '知识库名称'");
        List<Document> documents = pgVectorStore.similaritySearch(request);

        // 4. 文档处理阶段
        // 将检索到的多个文档内容合并成一个字符串
        String documentCollectors  = documents.stream().map(Document::getContent).collect(Collectors.joining());

        // 5. 消息组装阶段
        // 将 SYSTEM_PROMPT 中的 {documents} 占位符替换为 documentCollectors
        Message ragMessages = new SystemPromptTemplate(SYSTEM_PROMPT)
            .createMessage(Map.of("documents", documentCollectors));

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(message));
        messages.add(ragMessages);

        // 6. LLM 调用阶段
        ChatResponse chatResponse = chatClient.call(
            new Prompt(messages,
                OllamaOptions.create().withModel("deepseek-r1:1.5b"))
        );

        log.info("测试结果：{}", JSON.toJSONString(chatResponse));

    }


}
