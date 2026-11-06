package com.recruitment.ai.service;

import com.recruitment.ai.dto.response.JdSearchResultDto;
import com.recruitment.ai.entity.Jd;
import com.recruitment.ai.entity.JdChunk;
import com.recruitment.ai.entity.Recruitment;
import com.recruitment.ai.repository.JdChunkRepository;
import com.recruitment.ai.repository.JdRepository;
import com.recruitment.ai.service.LlmPdfExtractionService.ExtractedJdData;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class JdProcessingService {

    private static final Logger log = LoggerFactory.getLogger(JdProcessingService.class);

    private final FileStorageService fileStorageService;
    private final PdfExtractionService pdfExtractionService;
    private final LlmPdfExtractionService llmPdfExtractionService;
    private final DocumentSplitter documentSplitter;
    private final EmbeddingHelperService embeddingHelper;
    private final JdRepository jdRepository;
    private final JdChunkRepository jdChunkRepository;

    public JdProcessingService(
            FileStorageService fileStorageService,
            PdfExtractionService pdfExtractionService,
            LlmPdfExtractionService llmPdfExtractionService,
            DocumentSplitter documentSplitter,
            EmbeddingHelperService embeddingHelper,
            JdRepository jdRepository,
            JdChunkRepository jdChunkRepository) {
        this.fileStorageService = fileStorageService;
        this.pdfExtractionService = pdfExtractionService;
        this.llmPdfExtractionService = llmPdfExtractionService;
        this.documentSplitter = documentSplitter;
        this.embeddingHelper = embeddingHelper;
        this.jdRepository = jdRepository;
        this.jdChunkRepository = jdChunkRepository;
    }

    @Transactional
    public Jd processAndSaveJdFromSinglePdf(MultipartFile file, Recruitment recruitment) {
        log.info("Xử lý file PDF JD: {} cho nhà tuyển dụng: {}", file.getOriginalFilename(), recruitment.getCompanyName());

        String storedFilePath = fileStorageService.storeFile(file, "jds");
        String extractedText = pdfExtractionService.extractTextFromPdf(storedFilePath);

        ExtractedJdData extractedData = llmPdfExtractionService.extractJdFieldsWithLlm(extractedText);

        String summaryText = extractedData.jobTitle() + " " + extractedData.experience() + "\n" +
                (extractedText.length() > 2000 ? extractedText.substring(0, 2000) : extractedText);
        String jdVector = embeddingHelper.embedToPgVectorString(summaryText);

        Jd jd = new Jd();
        jd.setRecruitment(recruitment);
        jd.setJobTitle(extractedData.jobTitle());
        jd.setDescription(extractedData.description());
        jd.setStatus("OPEN");
        jd.setDeadline(extractedData.deadline());
        jd.setMinSalary(extractedData.minSalary());
        jd.setMaxSalary(extractedData.maxSalary());
        jd.setExperience(extractedData.experience());
        jd.setKhuVuc(extractedData.khuVuc());
        jd.setDomain(extractedData.domain());
        jd.setRawText(extractedText);
        jd.setVector(jdVector);
        jd.setMetaData(String.format("{\"filename\":\"%s\",\"company\":\"%s\"}",
                file.getOriginalFilename(), recruitment.getCompanyName()));
        jd.setCreatedAt(Instant.now());

        Jd savedJd = jdRepository.save(jd);

        Document doc = Document.from(extractedText);
        List<TextSegment> segments = documentSplitter.split(doc);

        List<JdChunk> chunkEntities = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            String chunkVector = embeddingHelper.embedToPgVectorString(segment.text());

            JdChunk chunk = new JdChunk();
            chunk.setJd(savedJd);
            chunk.setChunkIndex(i);
            chunk.setContent(segment.text());
            chunk.setVector(chunkVector);
            chunk.setMetaData(String.format("{\"index\":%d,\"length\":%d}", i, segment.text().length()));
            chunkEntities.add(chunk);
        }

        jdChunkRepository.saveAll(chunkEntities);
        log.info("Đã lưu JD [{}] cùng {} chunks.", savedJd.getJdId(), chunkEntities.size());

        return savedJd;
    }

    public List<JdSearchResultDto> searchJds(String keyword, String domain, String khuVuc, int topK) {
        String queryText = (keyword != null && !keyword.isBlank()) ? keyword : "job opening requirement";
        String queryVector = embeddingHelper.embedToPgVectorString(queryText);
        List<Object[]> rows = jdRepository.searchJds(queryVector, keyword, domain, khuVuc, topK);
        List<JdSearchResultDto> dtos = new ArrayList<>();
        for (Object[] row : rows) {
            UUID jdId = (UUID) row[0];
            String jobTitle = (String) row[1];
            String companyName = (String) row[2];
            String dom = (String) row[3];
            String exp = (String) row[4];
            String kv = (String) row[5];
            BigDecimal minSal = (BigDecimal) row[6];
            BigDecimal maxSal = (BigDecimal) row[7];
            String desc = (String) row[8];
            double similarity = ((Number) row[9]).doubleValue();
            dtos.add(new JdSearchResultDto(jdId, jobTitle, companyName, dom, exp, kv, minSal, maxSal, desc, similarity));
        }
        return dtos;
    }
}
