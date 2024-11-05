package com.example.pdfrag.config;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class PDFDataLoader {

    private final VectorStore vectorStore;
    private final JdbcClient db;
    @Value("classpath:/대한민국 헌법.pdf")
    private Resource pdfResource;

    public PDFDataLoader(VectorStore vectorStore,
                         JdbcClient db) {
        this.vectorStore = vectorStore;
        this.db = db;
    }
    // 자동호출
    @PostConstruct
    public void init() {
        Integer count =
                db.sql("select COUNT(*) from vector_store")
                        .query(Integer.class)
                        .single();

        System.out.println("PGvector에 저장된 레코드의 수 = " + count);

        if(count == 0) {
            System.out.println("Loading PG Vector Store");
            PdfDocumentReaderConfig config
                    = PdfDocumentReaderConfig.builder()
                    .withPagesPerDocument(1)
                    .build();

            PagePdfDocumentReader reader
                    = new PagePdfDocumentReader(pdfResource, config);

            var textSplitter = new TokenTextSplitter();
            vectorStore.accept(textSplitter.apply(reader.get()));

            System.out.println("Application is ready to Serve the Requests");
        }
    }
}
