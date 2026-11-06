package com.recruitment.ai.config;

import com.recruitment.ai.entity.*;
import com.recruitment.ai.repository.*;
import com.recruitment.ai.service.EmbeddingHelperService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final CandidateRepository candidateRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final JdRepository jdRepository;
    private final JdChunkRepository jdChunkRepository;
    private final CvRepository cvRepository;
    private final CvChunkRepository cvChunkRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmbeddingHelperService embeddingHelper;
    private final DocumentSplitter documentSplitter;

    public DataInitializer(
            UserRepository userRepository,
            AdminRepository adminRepository,
            CandidateRepository candidateRepository,
            RecruitmentRepository recruitmentRepository,
            JdRepository jdRepository,
            JdChunkRepository jdChunkRepository,
            CvRepository cvRepository,
            CvChunkRepository cvChunkRepository,
            PasswordEncoder passwordEncoder,
            EmbeddingHelperService embeddingHelper,
            DocumentSplitter documentSplitter) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.candidateRepository = candidateRepository;
        this.recruitmentRepository = recruitmentRepository;
        this.jdRepository = jdRepository;
        this.jdChunkRepository = jdChunkRepository;
        this.cvRepository = cvRepository;
        this.cvChunkRepository = cvChunkRepository;
        this.passwordEncoder = passwordEncoder;
        this.embeddingHelper = embeddingHelper;
        this.documentSplitter = documentSplitter;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            seedAdmin();
            Recruitment recruiter = seedRecruiter();
            seedSampleJd(recruiter);
            Candidate candidate = seedCandidate();
            seedSampleCv(candidate);
            seedDiverseData();
        } catch (Exception e) {
            log.warn("Lỗi khi khởi tạo dữ liệu mẫu: {}", e.getMessage());
        }
    }

    private void seedAdmin() {
        if (userRepository.findByEmail("admin@system.com").isEmpty()) {
            Admin admin = new Admin();
            admin.setEmail("admin@system.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            admin.setDepartment("System Administration");
            admin.setPermissions("ALL_PRIVILEGES");
            admin.setCreatedAt(Instant.now());
            admin.setUpdatedAt(Instant.now());
            adminRepository.save(admin);
            log.info("Initialized Admin: admin@system.com");
        }
    }

    private Recruitment seedRecruiter() {
        return recruitmentRepository.findByEmail("recruiter@techcorp.com").orElseGet(() -> {
            Recruitment recruiter = new Recruitment();
            recruiter.setEmail("recruiter@techcorp.com");
            recruiter.setPassword(passwordEncoder.encode("password123"));
            recruiter.setRole("ROLE_RECRUITMENT");
            recruiter.setCompanyName("TechCorp Vietnam");
            recruiter.setPhone("0987654321");
            recruiter.setAddress("Keangnam Landmark 72, Ha Noi");
            recruiter.setContactName("Nguyen Van Quan");
            recruiter.setCreatedAt(Instant.now());
            recruiter.setUpdatedAt(Instant.now());
            Recruitment saved = recruitmentRepository.save(recruiter);
            log.info("Initialized Recruiter: recruiter@techcorp.com");
            return saved;
        });
    }

    private void seedSampleJd(Recruitment recruiter) {
        if (jdRepository.findByRecruitment(recruiter).isEmpty()) {
            String title = "Senior Java Backend Engineer (Spring Boot, AI & pgvector)";
            String description = """
                Yêu cầu công việc:
                - Tối thiểu 3 năm kinh nghiệm phát triển hệ thống với Java và Spring Boot.
                - Thành thạo Spring Data JPA, Hibernate, PostgreSQL và các kỹ thuật tối ưu CSDL.
                - Có kinh nghiệm với pgvector, RAG pipeline hoặc tích hợp các mô hình LLM (Gemini, LangChain4j).
                - Sử dụng tốt Docker, Kubernetes, CI/CD và kiến trúc Microservices.
                - Khả năng tư duy giải thuật tốt, làm việc nhóm chủ động.
                """;

            String vector = safeEmbed(title + " " + description);

            Jd jd = new Jd();
            jd.setRecruitment(recruiter);
            jd.setJobTitle(title);
            jd.setDescription(description);
            jd.setStatus("OPEN");
            jd.setDeadline(LocalDate.now().plusMonths(2));
            jd.setMinSalary(new BigDecimal("30000000"));
            jd.setMaxSalary(new BigDecimal("55000000"));
            jd.setExperience("3-5 năm");
            jd.setKhuVuc("Ha Noi");
            jd.setDomain("Information Technology");
            jd.setRawText(description);
            jd.setVector(vector);
            jd.setMetaData("{\"skills\":[\"Java\", \"Spring Boot\", \"PostgreSQL\", \"pgvector\", \"LangChain4j\", \"Gemini\"]}");
            jd.setCreatedAt(Instant.now());

            Jd savedJd = jdRepository.save(jd);

            Document doc = Document.from(description);
            List<TextSegment> segments = documentSplitter.split(doc);
            List<JdChunk> chunks = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                TextSegment seg = segments.get(i);
                JdChunk chunk = new JdChunk();
                chunk.setJd(savedJd);
                chunk.setChunkIndex(i);
                chunk.setContent(seg.text());
                chunk.setVector(vector);
                chunks.add(chunk);
            }
            jdChunkRepository.saveAll(chunks);
            log.info("Initialized sample JD: {}", savedJd.getJobTitle());
        }
    }

    private Candidate seedCandidate() {
        return candidateRepository.findByEmail("candidate@example.com").orElseGet(() -> {
            Candidate candidate = new Candidate();
            candidate.setEmail("candidate@example.com");
            candidate.setPassword(passwordEncoder.encode("password123"));
            candidate.setRole("ROLE_CANDIDATE");
            candidate.setFullName("Nguyen Van A");
            candidate.setBirthDay(LocalDate.of(1998, 5, 20));
            candidate.setAddress("Ha Noi");
            candidate.setPhone("0912345678");
            candidate.setStatus("ACTIVE");
            candidate.setCreatedAt(Instant.now());
            candidate.setUpdatedAt(Instant.now());
            Candidate saved = candidateRepository.save(candidate);
            log.info("Initialized Candidate: candidate@example.com");
            return saved;
        });
    }

    private void seedSampleCv(Candidate candidate) {
        if (cvRepository.findByCandidate(candidate).isEmpty()) {
            String title = "Senior Java Backend Developer CV";
            String rawText = """
                HO SO UNG VIEN: NGUYEN VAN A
                Vi tri: Senior Java Backend Developer
                Kinh nghiem: 4 nam lam viec voi Spring Boot, PostgreSQL, pgvector, Microservices
                Ky nang: Java, Spring Data JPA, JWT, LangChain4j, Gemini AI, Docker, Kubernetes
                """;

            String vector = safeEmbed(title + " " + rawText);

            Cv cv = new Cv();
            cv.setCandidate(candidate);
            cv.setFileUrl("uploads/cvs/sample_candidate_cv.pdf");
            cv.setCvTitle(title);
            cv.setDomain("Information Technology");
            cv.setExperience("4 năm");
            cv.setKhuVuc("Ha Noi");
            cv.setRawText(rawText);
            cv.setVector(vector);
            cv.setMetaData("{\"skills\":[\"Java\", \"Spring Boot\", \"PostgreSQL\", \"pgvector\", \"LangChain4j\", \"Gemini AI\"]}");
            cv.setCreatedAt(Instant.now());

            Cv savedCv = cvRepository.save(cv);

            Document doc = Document.from(rawText);
            List<TextSegment> segments = documentSplitter.split(doc);
            List<CvChunk> chunks = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                TextSegment seg = segments.get(i);
                CvChunk chunk = new CvChunk();
                chunk.setCv(savedCv);
                chunk.setChunkIndex(i);
                chunk.setContent(seg.text());
                chunk.setVector(vector);
                chunks.add(chunk);
            }
            cvChunkRepository.saveAll(chunks);
            log.info("Initialized sample CV: {}", savedCv.getCvTitle());
        }
    }

    @Transactional
    public void seedDiverseData() {
        log.info("Bắt đầu bổ sung thêm CV và JD mẫu vào cơ sở dữ liệu...");

        // 1. Nhà tuyển dụng VNG
        Recruitment vngRecruiter = recruitmentRepository.findByEmail("tuyendung@vng.vn").orElseGet(() -> {
            Recruitment r = new Recruitment();
            r.setEmail("tuyendung@vng.vn");
            r.setPassword(passwordEncoder.encode("password123"));
            r.setRole("ROLE_RECRUITMENT");
            r.setCompanyName("VNG Corporation");
            r.setPhone("02839998888");
            r.setAddress("Z06 Đường số 13, Tân Thuận Đông, Quận 7, TP Hồ Chí Minh");
            r.setContactName("Trần Bảo Ngọc");
            r.setCreatedAt(Instant.now());
            r.setUpdatedAt(Instant.now());
            return recruitmentRepository.save(r);
        });

        // 2. Nhà tuyển dụng Viettel
        Recruitment viettelRecruiter = recruitmentRepository.findByEmail("hr@viettel.vn").orElseGet(() -> {
            Recruitment r = new Recruitment();
            r.setEmail("hr@viettel.vn");
            r.setPassword(passwordEncoder.encode("password123"));
            r.setRole("ROLE_RECRUITMENT");
            r.setCompanyName("Tập đoàn Viettel Telecom");
            r.setPhone("02462556789");
            r.setAddress("Số 1 Giang Văn Minh, Ba Đình, Hà Nội");
            r.setContactName("Lê Thu Trang");
            r.setCreatedAt(Instant.now());
            r.setUpdatedAt(Instant.now());
            return recruitmentRepository.save(r);
        });

        // 3. TechCorp recruiter
        Recruitment techcorp = recruitmentRepository.findByEmail("recruiter@techcorp.com").orElse(null);

        // --- BỔ SUNG JDs VÀO DATABASE ---
        createJdIfNotExists(
            vngRecruiter,
            "Chuyên Gia Trí Tuệ Nhân Tạo & LLM (Generative AI Specialist)",
            "AI / Machine Learning",
            "2-4 năm",
            "Ho Chi Minh",
            new BigDecimal("35000000"),
            new BigDecimal("55000000"),
            """
            Yêu cầu công việc:
            - Tối thiểu 2 năm kinh nghiệm làm việc với Python và các framework AI/ML: PyTorch, Hugging Face, LangChain.
            - Có kinh nghiệm thực chiến phát triển ứng dụng Generative AI, RAG (Retrieval-Augmented Generation) và tích hợp các LLM (OpenAI, Gemini).
            - Nắm vững các kỹ thuật Vector Search và cơ sở dữ liệu vector: pgvector, Milvus hoặc Qdrant.
            - Xây dựng và triển khai RESTful API tốc độ cao bằng FastAPI, Docker, tối ưu hóa latency cho mô hình AI.
            - Tinh thần nghiên cứu công nghệ mới, chủ động giải quyết bài toán phức tạp.
            """,
            "{\"skills\":[\"Python\", \"PyTorch\", \"LangChain\", \"Generative AI\", \"RAG\", \"pgvector\", \"Gemini LLM\", \"FastAPI\"]}"
        );

        createJdIfNotExists(
            viettelRecruiter,
            "Senior Big Data Platform Engineer (Spark, Kafka, Data Pipeline)",
            "Data Engineering",
            "3-5 năm",
            "Da Nang",
            new BigDecimal("30000000"),
            new BigDecimal("50000000"),
            """
            Yêu cầu công việc:
            - Từ 3 năm kinh nghiệm phát triển hệ thống xử lý dữ liệu lớn (Big Data).
            - Thành thạo Apache Spark, Apache Kafka, Apache Airflow và ngôn ngữ lập trình Python/Scala.
            - Hiểu biết sâu về cơ sở dữ liệu quan hệ (PostgreSQL) và NoSQL, kỹ năng tối ưu truy vấn SQL nâng cao.
            - Có kinh nghiệm thiết kế kiến trúc Data Lake, Data Warehouse và xây dựng pipeline ETL/ELT chịu tải cao.
            - Khả năng phối hợp liên phòng ban, tư duy phân tích dữ liệu nhạy bén.
            """,
            "{\"skills\":[\"Apache Spark\", \"Kafka\", \"Airflow\", \"Python\", \"PostgreSQL\", \"Data Lake\", \"ETL Pipeline\"]}"
        );

        if (techcorp != null) {
            createJdIfNotExists(
                techcorp,
                "Senior Frontend React / Next.js Engineer",
                "Frontend",
                "3-5 năm",
                "Ha Noi",
                new BigDecimal("25000000"),
                new BigDecimal("38000000"),
                """
                Yêu cầu công việc:
                - Tối thiểu 3 năm kinh nghiệm phát triển Frontend chuyên sâu với ReactJS, Next.js và TypeScript.
                - Nắm vững kiến trúc Component, State Management (Redux Toolkit, Zustand, React Query).
                - Sử dụng thành thạo Tailwind CSS, tối ưu hiệu năng hiển thị và trải nghiệm người dùng (UX).
                - Có kinh nghiệm làm việc với RESTful API, WebSocket và CI/CD Frontend deployment.
                - Kỹ năng giao tiếp tốt, sẵn sàng hỗ trợ thành viên trong nhóm.
                """,
                "{\"skills\":[\"ReactJS\", \"Next.js\", \"TypeScript\", \"Tailwind CSS\", \"Redux Toolkit\", \"REST API\"]}"
            );

            createJdIfNotExists(
                techcorp,
                "Cloud DevOps & SRE Engineer (AWS, Kubernetes, Terraform)",
                "DevOps & Cloud",
                "3-5 năm",
                "Ha Noi",
                new BigDecimal("30000000"),
                new BigDecimal("45000000"),
                """
                Yêu cầu công việc:
                - Tối thiểu 3 năm kinh nghiệm ở vị trí DevOps / SRE trên nền tảng AWS hoặc GCP.
                - Quản trị thành thạo hạ tầng Kubernetes (EKS, GKE), viết Helm chart và tối ưu hóa tài nguyên cụm.
                - Sử dụng thành thạo Terraform (Infrastructure as Code) để tự động hóa việc cấp phát hạ tầng.
                - Xây dựng pipeline CI/CD ổn định, bảo mật với GitHub Actions hoặc GitLab CI.
                - Có kinh nghiệm cấu hình hệ thống giám sát và cảnh báo: Prometheus, Grafana, ELK Stack.
                """,
                "{\"skills\":[\"Docker\", \"Kubernetes\", \"AWS Cloud\", \"Terraform\", \"GitHub Actions\", \"Prometheus\", \"Grafana\"]}"
            );
        }

        // --- BỔ SUNG ỨNG VIÊN VÀ CVs VÀO DATABASE ---
        Candidate candMai = candidateRepository.findByEmail("mai.nguyen@frontend.dev").orElseGet(() -> {
            Candidate c = new Candidate();
            c.setEmail("mai.nguyen@frontend.dev");
            c.setPassword(passwordEncoder.encode("password123"));
            c.setRole("ROLE_CANDIDATE");
            c.setFullName("Nguyễn Thị Mai");
            c.setBirthDay(LocalDate.of(2000, 8, 15));
            c.setAddress("Hà Nội");
            c.setPhone("0934567890");
            c.setStatus("ACTIVE");
            c.setCreatedAt(Instant.now());
            c.setUpdatedAt(Instant.now());
            return candidateRepository.save(c);
        });

        createCvIfNotExists(
            candMai,
            "Chuyên Viên Frontend React & Next.js Developer",
            "Frontend",
            "3 năm",
            "Ha Noi",
            """
            HỒ SƠ ỨNG VIÊN: NGUYỄN THỊ MAI
            Vị trí: Chuyên viên Frontend React / Next.js Developer
            Kinh nghiệm: 3 năm phát triển ứng dụng Web SPA và SSR với ReactJS, Next.js, TypeScript.
            Kỹ năng: ReactJS, Next.js, TypeScript, Redux Toolkit, Tailwind CSS, Responsive Web Design, RESTful API, Vite.
            Dự án tiêu biểu: 
            - Xây dựng hệ thống E-commerce và Portal B2B phục vụ 50.000 người dùng hoạt động hàng ngày.
            - Tối ưu Core Web Vitals, tăng điểm Lighthouse từ 65 lên 95.
            """,
            "{\"skills\":[\"ReactJS\", \"Next.js\", \"TypeScript\", \"Tailwind CSS\", \"Redux Toolkit\", \"HTML5\", \"CSS3\"]}"
        );

        Candidate candLong = candidateRepository.findByEmail("long.le@data.vn").orElseGet(() -> {
            Candidate c = new Candidate();
            c.setEmail("long.le@data.vn");
            c.setPassword(passwordEncoder.encode("password123"));
            c.setRole("ROLE_CANDIDATE");
            c.setFullName("Lê Hoàng Long");
            c.setBirthDay(LocalDate.of(1996, 11, 10));
            c.setAddress("Đà Nẵng");
            c.setPhone("0945678901");
            c.setStatus("ACTIVE");
            c.setCreatedAt(Instant.now());
            c.setUpdatedAt(Instant.now());
            return candidateRepository.save(c);
        });

        createCvIfNotExists(
            candLong,
            "Kỹ Sư Dữ Liệu & Big Data Platform (Spark, Kafka, Python)",
            "Data Engineering",
            "4 năm",
            "Da Nang",
            """
            HỒ SƠ ỨNG VIÊN: LÊ HOÀNG LONG
            Vị trí: Kỹ Sư Dữ Liệu & Big Data Engineer
            Kinh nghiệm: 4 năm xây dựng hạ tầng Data Pipeline và Data Warehouse.
            Kỹ năng: Python, Apache Spark, Apache Kafka, Apache Airflow, PostgreSQL, ClickHouse, Docker, ETL/ELT pipelines.
            Dự án tiêu biểu:
            - Xây dựng hệ thống Streaming Data Pipeline xử lý 10 triệu sự kiện/ngày với Kafka và Spark Streaming.
            - Tối ưu hóa truy vấn phân tích dữ liệu trên kho dữ liệu lớn, giảm thời gian xử lý báo cáo 60%.
            """,
            "{\"skills\":[\"Python\", \"Apache Spark\", \"Kafka\", \"Airflow\", \"PostgreSQL\", \"Data Pipeline\", \"ETL\"]}"
        );

        Candidate candDuc = candidateRepository.findByEmail("duc.pham@devops.vn").orElseGet(() -> {
            Candidate c = new Candidate();
            c.setEmail("duc.pham@devops.vn");
            c.setPassword(passwordEncoder.encode("password123"));
            c.setRole("ROLE_CANDIDATE");
            c.setFullName("Phạm Minh Đức");
            c.setBirthDay(LocalDate.of(1997, 4, 25));
            c.setAddress("TP Hồ Chí Minh");
            c.setPhone("0956789012");
            c.setStatus("ACTIVE");
            c.setCreatedAt(Instant.now());
            c.setUpdatedAt(Instant.now());
            return candidateRepository.save(c);
        });

        createCvIfNotExists(
            candDuc,
            "Kỹ Sư DevOps & Cloud Platform (AWS, Kubernetes, CI/CD)",
            "DevOps & Cloud",
            "3.5 năm",
            "Ho Chi Minh",
            """
            HỒ SƠ ỨNG VIÊN: PHẠM MINH ĐỨC
            Vị trí: Kỹ Sư DevOps & Cloud Platform
            Kinh nghiệm: 3.5 năm triển khai hạ tầng đám mây và tự động hóa CI/CD.
            Kỹ năng: Docker, Kubernetes (EKS/GKE), AWS Cloud (EC2, S3, RDS, IAM), Terraform, CI/CD GitHub Actions, Prometheus & Grafana.
            Dự án tiêu biểu:
            - Chuyển đổi toàn bộ hệ thống monolithic sang microservices chạy trên Kubernetes cụm 20 nodes.
            - Thiết lập pipeline CI/CD tự động build, test và deploy zero-downtime cho 15 microservices.
            """,
            "{\"skills\":[\"Docker\", \"Kubernetes\", \"AWS Cloud\", \"Terraform\", \"GitHub Actions\", \"Prometheus\", \"CI/CD\"]}"
        );

        log.info("Hoàn tất bổ sung dữ liệu mẫu đa dạng vào cơ sở dữ liệu!");
    }

    private void createJdIfNotExists(
            Recruitment recruiter,
            String title,
            String domain,
            String exp,
            String khuVuc,
            BigDecimal minSal,
            BigDecimal maxSal,
            String description,
            String metaData) {

        boolean exists = jdRepository.findAll().stream()
                .anyMatch(j -> j.getJobTitle().equalsIgnoreCase(title));
        if (exists) return;

        String vector = safeEmbed(title + " " + description);

        Jd jd = new Jd();
        jd.setRecruitment(recruiter);
        jd.setJobTitle(title);
        jd.setDescription(description);
        jd.setStatus("OPEN");
        jd.setDeadline(LocalDate.now().plusMonths(2));
        jd.setMinSalary(minSal);
        jd.setMaxSalary(maxSal);
        jd.setExperience(exp);
        jd.setKhuVuc(khuVuc);
        jd.setDomain(domain);
        jd.setRawText(description);
        jd.setVector(vector);
        jd.setMetaData(metaData);
        jd.setCreatedAt(Instant.now());

        Jd saved = jdRepository.save(jd);

        Document doc = Document.from(description);
        List<TextSegment> segments = documentSplitter.split(doc);
        List<JdChunk> chunks = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            TextSegment seg = segments.get(i);
            JdChunk chunk = new JdChunk();
            chunk.setJd(saved);
            chunk.setChunkIndex(i);
            chunk.setContent(seg.text());
            chunk.setVector(vector);
            chunks.add(chunk);
        }
        jdChunkRepository.saveAll(chunks);
        log.info("Created sample JD: {}", title);
    }

    private void createCvIfNotExists(
            Candidate candidate,
            String title,
            String domain,
            String exp,
            String khuVuc,
            String rawText,
            String metaData) {

        boolean exists = cvRepository.findByCandidate(candidate).stream()
                .anyMatch(c -> c.getCvTitle().equalsIgnoreCase(title));
        if (exists) return;

        String vector = safeEmbed(title + " " + rawText);

        Cv cv = new Cv();
        cv.setCandidate(candidate);
        cv.setFileUrl("uploads/cvs/" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
        cv.setCvTitle(title);
        cv.setDomain(domain);
        cv.setExperience(exp);
        cv.setKhuVuc(khuVuc);
        cv.setRawText(rawText);
        cv.setVector(vector);
        cv.setMetaData(metaData);
        cv.setCreatedAt(Instant.now());

        Cv saved = cvRepository.save(cv);

        Document doc = Document.from(rawText);
        List<TextSegment> segments = documentSplitter.split(doc);
        List<CvChunk> chunks = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            TextSegment seg = segments.get(i);
            CvChunk chunk = new CvChunk();
            chunk.setCv(saved);
            chunk.setChunkIndex(i);
            chunk.setContent(seg.text());
            chunk.setVector(vector);
            chunks.add(chunk);
        }
        cvChunkRepository.saveAll(chunks);
        log.info("Created sample CV: {}", title);
    }

    private String safeEmbed(String text) {
        try {
            Thread.sleep(600);
            return embeddingHelper.embedToPgVectorString(text);
        } catch (Exception e) {
            log.warn("Gemini embedding error: {}, generating fallback vector", e.getMessage());
            return generateFallbackVector(text);
        }
    }

    private String generateFallbackVector(String text) {
        float[] vec = new float[768];
        java.util.Random rnd = new java.util.Random(text.hashCode());
        float sumSq = 0;
        for (int i = 0; i < 768; i++) {
            vec[i] = (float) (rnd.nextGaussian() * 0.05);
            sumSq += vec[i] * vec[i];
        }
        float norm = (float) Math.sqrt(sumSq);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 768; i++) {
            sb.append(vec[i] / norm);
            if (i < 767) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
