package com.example.pdfrag.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class PDFController {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    private String prompt = """
            질문에 답변하는 것입니다. 질문에 정확하게 답변하기 위해 DOCUMENTS 에 있는 정보를 사용해야 합니다. 
            만약 정보가 부족하거나 DOCUMENTS 에서 답을 찾을 수 없다면, 알지 못한다고 간단히 답변하세요.               
            질문:
            {input}               
            DOCUMENTS :
            {documents}                                   
            """;
    public PDFController(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    @GetMapping("/api/answer")
    public String simplify(@RequestParam(value = "question",
    defaultValue = "List all the Articles in the South Korea Constitution")
                           String question) {

        PromptTemplate template
                = new PromptTemplate(prompt);
        Map<String, Object> promptsParameters = new HashMap<>();
        promptsParameters.put("input", question);
        promptsParameters.put("documents", findSimilarData(question));

        return chatModel
                .call(template.create(promptsParameters))
                .getResult()
                .getOutput()
                .getContent();
    }

    private String findSimilarData(String question) {
        List<Document> documents =
                vectorStore.similaritySearch(SearchRequest
                .query(question)
                        .withTopK(5));

        return documents
                .stream()
                .map(document -> document.getContent().toString())
                .collect(Collectors.joining());
    }
}
