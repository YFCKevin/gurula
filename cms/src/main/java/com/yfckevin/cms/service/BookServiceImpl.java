package com.yfckevin.cms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;
import com.yfckevin.cms.ConfigProperties;
import com.yfckevin.cms.entity.ErrorFile;
import com.yfckevin.cms.repository.ContentRepository;
import com.yfckevin.cms.repository.ErrorFileRepository;
import com.yfckevin.common.dto.inkCloud.ImageRequestDTO;
import com.yfckevin.common.dto.inkCloud.SearchDTO;
import com.yfckevin.cms.entity.Book;
import com.yfckevin.cms.repository.BookRepository;
import com.yfckevin.common.dto.inkCloud.BookDTO;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.common.utils.MemberContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final ErrorFileRepository errorFileRepository;
    private final ContentRepository contentRepository;
    private final LLMService llmService;
    private final OCRService ocrService;
    private final MongoTemplate mongoTemplate;
    private final SimpleDateFormat sdf;
    private final ConfigProperties configProperties;
    private final ObjectMapper objectMapper;

    public BookServiceImpl(BookRepository bookRepository, ErrorFileRepository errorFileRepository, ContentRepository contentRepository, LLMService llmService, OCRService ocrService, MongoTemplate mongoTemplate, @Qualifier("sdf") SimpleDateFormat sdf, ConfigProperties configProperties, ObjectMapper objectMapper) {
        this.bookRepository = bookRepository;
        this.errorFileRepository = errorFileRepository;
        this.contentRepository = contentRepository;
        this.llmService = llmService;
        this.ocrService = ocrService;
        this.mongoTemplate = mongoTemplate;
        this.sdf = sdf;
        this.configProperties = configProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Book> findBook(SearchDTO searchDTO) {
        final String keyword = searchDTO.getKeyword().trim();
        List<Criteria> orCriterias = new ArrayList<>();
        List<Criteria> andCriterias = new ArrayList<>();

        Criteria criteria = Criteria.where("deletionDate").exists(false);

        if (StringUtils.isNotBlank(keyword)) {
            Criteria criteria_author = Criteria.where("author").regex(keyword, "i");
            Criteria criteria_publisher = Criteria.where("publisher").regex(keyword, "i");
            Criteria criteria_title = Criteria.where("title").regex(keyword, "i");
            orCriterias.add(criteria_author);
            orCriterias.add(criteria_publisher);
            orCriterias.add(criteria_title);
        }

        final String visionType = searchDTO.getVisionType();
        if (StringUtils.isNotBlank(visionType)) {
            Criteria criteria_vision = Criteria.where("visionType").is(visionType);
            orCriterias.add(criteria_vision);
        }

        if (StringUtils.isNotBlank(searchDTO.getMemberId())) {
            Criteria criteria_memberId = Criteria.where("memberId").is(searchDTO.getMemberId());
            andCriterias.add(criteria_memberId);
        }

        if (!orCriterias.isEmpty()) {
            criteria = criteria.orOperator(orCriterias.toArray(new Criteria[0]));
        }
        if (!andCriterias.isEmpty()) {
            criteria = criteria.andOperator(andCriterias.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);

        return mongoTemplate.find(query, Book.class);
    }

    @Transactional
    @Override
    public int deleteBook(String bookId) {
        return bookRepository.findById(bookId)
                .map(
                        book -> {
                            book.setDeletionDate(sdf.format(new Date()));
                            bookRepository.save(book);
                            return 1;
                        }).orElse(0);
    }

    @Transactional
    @Override
    public int editBook(BookDTO bookDTO) {
        return bookRepository.findById(bookDTO.getId())
                .map(book -> {
                    book.setTitle(bookDTO.getTitle());
                    book.setAuthor(bookDTO.getAuthor());
                    book.setPublisher(bookDTO.getPublisher());
                    book.setModifier(MemberContext.getMember());
                    book.setModificationDate(sdf.format(new Date()));
                    bookRepository.save(book);
                    return 1;
                }).orElse(0);
    }

    @Override
    public Map<String, Integer> saveMultiBook(List<ImageRequestDTO> imageRequestDTOs, String memberId) {
        // 成功和失敗計數
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        for (ImageRequestDTO imageDTO : imageRequestDTOs) {
            final String originalFileName = imageDTO.getFileName(); // 使用傳送過來的檔案名稱

            for (String base64Image : imageDTO.getImages()) {
                try {
                    final byte[] decodeImg = Base64.getDecoder().decode(base64Image);

                    // 儲存檔案並生成唯一名稱
                    String fileName = generateUniqueFileName(originalFileName);
                    String filePath = configProperties.getAiPicSavePath() + fileName;
                    try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        fos.write(decodeImg);
                    }

                    // 使用google OCR AI取得圖片中的文字資訊
                    ByteString imgBytes = ByteString.copyFrom(decodeImg);
                    Image img = Image.newBuilder().setContent(imgBytes).build();
                    Feature feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
                    AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                            .addFeatures(feature)
                            .setImage(img)
                            .build();

                    List<AnnotateImageRequest> requests = new ArrayList<>();
                    requests.add(request);
                    String rawText = ocrService.extractText(requests);
                    log.info("圖轉文：{}", rawText);

                    // 使用gpt-4o-mini分析書籍，並組成存入DB內的json file
                    final ResultStatus<String> result = llmService.constructBookEntity(rawText);

                    if ("C000".equals(result.getCode())) {
                        List<Book> bookList = objectMapper.readValue(result.getData(), new TypeReference<>() {
                        });
                        bookList.forEach(book -> {
                            book.setCreationDate(sdf.format(new Date()));
                            book.setCreator(memberId);
                            book.setMemberId(memberId);
                            book.setSourceCoverName(fileName);
                        });

                        bookRepository.saveAll(bookList);
                        log.info("成功保存書籍: {}", filePath);
                        successCount.incrementAndGet();

                    } else {
                        handleErrorFile(result.getCode(), result.getMessage(), fileName, memberId);
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    handleErrorFile("ERROR", e.getMessage(), originalFileName, memberId);
                    errorCount.incrementAndGet();
                    log.error("處理檔案 {} 時發生異常: {}", originalFileName, e.getMessage(), e);
                }
            }
        }

        Map<String, Integer> dataMap = new HashMap<>();
        dataMap.put("successCount", successCount.get());
        dataMap.put("errorCount", errorCount.get());
        System.out.println("dataMap = " + dataMap);
        return dataMap;
    }

    private void handleErrorFile(String code, String message, String fileName, String memberId) {
        ErrorFile errorFile = new ErrorFile();
        errorFile.setErrorCode(code);
        errorFile.setErrorMsg(message);
        errorFile.setCoverName(fileName);
        errorFile.setMemberId(memberId);
        errorFileRepository.save(errorFile);
    }


    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }

        String baseName = originalFileName.substring(0, dotIndex);

        return baseName + "_" + System.currentTimeMillis() + extension;
    }
}
