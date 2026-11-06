package com.recruitment.ai.service;

import com.recruitment.ai.dto.response.CvSearchResultDto;
import com.recruitment.ai.entity.Candidate;
import com.recruitment.ai.entity.Cv;
import com.recruitment.ai.entity.CvChunk;
import com.recruitment.ai.repository.CandidateRepository;
import com.recruitment.ai.repository.CvChunkRepository;
import com.recruitment.ai.repository.CvRepository;
import com.recruitment.ai.service.LlmPdfExtractionService.ExtractedCvData;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CvProcessingService {

    private static final Logger log = LoggerFactory.getLogger(CvProcessingService.class);

    private final FileStorageService fileStorageService;
    private final PdfExtractionService pdfExtractionService;
    private final LlmPdfExtractionService llmPdfExtractionService;
    private final DocumentSplitter documentSplitter;
    private final EmbeddingHelperService embeddingHelper;
    private final CvRepository cvRepository;
    private final CvChunkRepository cvChunkRepository;
    private final CandidateRepository candidateRepository;

    public CvProcessingService(
            FileStorageService fileStorageService,
            PdfExtractionService pdfExtractionService,
            LlmPdfExtractionService llmPdfExtractionService,
            DocumentSplitter documentSplitter,
            EmbeddingHelperService embeddingHelper,
            CvRepository cvRepository,
            CvChunkRepository cvChunkRepository,
            CandidateRepository candidateRepository) {
        this.fileStorageService = fileStorageService;
        this.pdfExtractionService = pdfExtractionService;
        this.llmPdfExtractionService = llmPdfExtractionService;
        this.documentSplitter = documentSplitter;
        this.embeddingHelper = embeddingHelper;
        this.cvRepository = cvRepository;
        this.cvChunkRepository = cvChunkRepository;
        this.candidateRepository = candidateRepository;
    }

    @Transactional
    public Cv processAndSaveCvFromSinglePdf(MultipartFile file, Candidate candidate) {
        log.info("Xử lý file PDF CV: {} cho ứng viên: {}", file.getOriginalFilename(), candidate.getEmail());

        String storedFilePath = fileStorageService.storeFile(file, "cvs");
        String extractedText = pdfExtractionService.extractTextFromPdf(storedFilePath);

        ExtractedCvData extractedData = llmPdfExtractionService.extractCvFieldsWithLlm(extractedText);

        if (extractedData.candidateFullName() != null && !extractedData.candidateFullName().isBlank()) {
            candidate.setFullName(extractedData.candidateFullName());
            candidateRepository.save(candidate);
        }

        String summaryText = extractedData.cvTitle() + " " + extractedData.experience() + "\n" +
                (extractedText.length() > 2000 ? extractedText.substring(0, 2000) : extractedText);
        String cvVector = embeddingHelper.embedToPgVectorString(summaryText);

        Cv cv = new Cv();
        cv.setCandidate(candidate);
        cv.setFileUrl(storedFilePath);
        cv.setCvTitle(extractedData.cvTitle());
        cv.setDomain(extractedData.domain());
        cv.setExperience(extractedData.experience());
        cv.setKhuVuc(extractedData.khuVuc());
        cv.setRawText(extractedText);
        cv.setVector(cvVector);
        cv.setMetaData(String.format("{\"filename\":\"%s\",\"skills\":%s}",
                file.getOriginalFilename(), extractedData.skills().toString()));
        cv.setCreatedAt(Instant.now());

        Cv savedCv = cvRepository.save(cv);

        Document doc = Document.from(extractedText);
        List<TextSegment> segments = documentSplitter.split(doc);

        List<CvChunk> chunkEntities = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            String chunkVector = embeddingHelper.embedToPgVectorString(segment.text());

            CvChunk chunk = new CvChunk();
            chunk.setCv(savedCv);
            chunk.setChunkIndex(i);
            chunk.setContent(segment.text());
            chunk.setVector(chunkVector);
            chunk.setMetaData(String.format("{\"index\":%d,\"length\":%d}", i, segment.text().length()));
            chunkEntities.add(chunk);
        }

        cvChunkRepository.saveAll(chunkEntities);
        log.info("Đã lưu CV [{}] cùng {} chunks.", savedCv.getCvId(), chunkEntities.size());

        return savedCv;
    }

    public List<CvSearchResultDto> searchCvs(String keyword, String domain, String khuVuc, int topK) {
        String queryText = (keyword != null && !keyword.isBlank()) ? keyword : "software engineer candidate";
        String queryVector = embeddingHelper.embedToPgVectorString(queryText);
        List<Object[]> rows = cvRepository.searchCvs(queryVector, keyword, domain, khuVuc, topK);
        List<CvSearchResultDto> dtos = new ArrayList<>();
        for (Object[] row : rows) {
            UUID cvId = (UUID) row[0];
            String cvTitle = (String) row[1];
            String candName = (String) row[2];
            String dom = (String) row[3];
            String exp = (String) row[4];
            String kv = (String) row[5];
            String fileUrl = (String) row[6];
            String metaData = (String) row[7];
            double similarity = ((Number) row[8]).doubleValue();
            dtos.add(new CvSearchResultDto(cvId, cvTitle, candName, dom, exp, kv, fileUrl, metaData, similarity));
        }
        return dtos;
    }
}
