package com.yfckevin.cms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;
import com.yfckevin.cms.ConfigProperties;
import com.yfckevin.cms.config.RabbitMQConfig;
import com.yfckevin.cms.entity.Content;
import com.yfckevin.cms.entity.ErrorFile;
import com.yfckevin.cms.enums.MediaType;
import com.yfckevin.cms.repository.ContentRepository;
import com.yfckevin.cms.repository.ErrorFileRepository;
import com.yfckevin.common.dto.inkCloud.*;
import com.yfckevin.cms.entity.Book;
import com.yfckevin.cms.repository.BookRepository;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.common.utils.MemberContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import java.util.stream.Collectors;

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
    private final RabbitTemplate rabbitTemplate;

    public BookServiceImpl(BookRepository bookRepository, ErrorFileRepository errorFileRepository, ContentRepository contentRepository, LLMService llmService, OCRService ocrService, MongoTemplate mongoTemplate, @Qualifier("sdf") SimpleDateFormat sdf, ConfigProperties configProperties, ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.bookRepository = bookRepository;
        this.errorFileRepository = errorFileRepository;
        this.contentRepository = contentRepository;
        this.llmService = llmService;
        this.ocrService = ocrService;
        this.mongoTemplate = mongoTemplate;
        this.sdf = sdf;
        this.configProperties = configProperties;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public List<Book> findBook(SearchDTO searchDTO) {
        System.out.println("searchDTO = " + searchDTO);
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
    public int editBook(String memberId, BookDTO bookDTO) {
        return bookRepository.findById(bookDTO.getId())
                .map(book -> {
                    book.setTitle(bookDTO.getTitle());
                    book.setAuthor(bookDTO.getAuthor());
                    book.setPublisher(bookDTO.getPublisher());
                    book.setModifier(memberId);
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

    @Override
    public ResultStatus previewBookStatus(String bookId) {
        ResultStatus resultStatus = new ResultStatus();
        final Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            resultStatus.setCode("C001");
            resultStatus.setMessage("查無書籍");
        } else {
            final Book book = bookOpt.get();
            if (StringUtils.isBlank(book.getSourceVideoId())) {
                resultStatus.setCode("C005");
                resultStatus.setMessage("尚未生成試閱影片");
            } else {
                final Optional<Content> videoOpt = contentRepository.findById(book.getSourceVideoId());
                if (videoOpt.isEmpty()) {
                    resultStatus.setCode("C006");
                    resultStatus.setMessage("查無影片");
                } else {
                    final Content video = videoOpt.get();
                    System.out.println("video = " + video);
                    if (StringUtils.isBlank(video.getPath()) && StringUtils.isNotBlank(book.getError())) {
                        //製作影片過程有錯誤
                        resultStatus.setCode("C003");
                        resultStatus.setMessage("製作影片過程有錯誤");
                    } else if (StringUtils.isBlank(video.getPath()) && StringUtils.isBlank(book.getError())) {
                        //影片製作中
                        resultStatus.setCode("C004");
                        resultStatus.setMessage("試閱影片製作中");
                    } else if (StringUtils.isNotBlank(video.getPath()) && StringUtils.isBlank(book.getError())) {
                        resultStatus.setCode("C000");
                        resultStatus.setMessage("成功");
                        resultStatus.setData(configProperties.getVideoShowPath() + video.getTitle());
                    }
                }
            }
        }
        return resultStatus;
    }

    @Override
    public ResultStatus constructVideo(String memberId, VideoRequestDTO dto) {
        ResultStatus resultStatus = new ResultStatus();
        final Optional<Book> opt = bookRepository.findById(dto.getBookId());
        if (opt.isEmpty()) {
            resultStatus.setCode("C001");
            resultStatus.setMessage("查無書籍");
        } else {
            final Book book = opt.get();
            String text = "書名:" + book.getTitle() + "," + "作者:" + book.getAuthor();
            NarrationMsgDTO narrationMsgDTO = new NarrationMsgDTO();
            narrationMsgDTO.setText(text);
            narrationMsgDTO.setBookId(dto.getBookId());
            narrationMsgDTO.setVideoId(dto.getVideoId());
            narrationMsgDTO.setBookName(book.getTitle());
            narrationMsgDTO.setMemberId(memberId);
            rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.llm", narrationMsgDTO);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
        }
        return resultStatus;
    }

    @Override
    public ResultStatus getVideoId(String bookId, String memberId) {
        ResultStatus resultStatus = new ResultStatus();
        final Optional<Book> opt = bookRepository.findById(bookId);
        if (opt.isEmpty()) {
            resultStatus.setCode("C001");
            resultStatus.setMessage("查無書籍");
        } else {
            Content video = new Content();
            video.setMemberId(memberId);
            video.setMediaType(MediaType.Video);
            Content savedVideo = contentRepository.save(video);

            final Book book = opt.get();
            book.setSourceVideoId(savedVideo.getId());
            bookRepository.save(book);

            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(savedVideo.getId());
        }
        return resultStatus;
    }

    @Override
    public ResultStatus getPreviewStatus(String memberId) {
        ResultStatus resultStatus = new ResultStatus();
        List<Content> contentList = contentRepository.findByMemberIdAndDeletionDateIsNullAndPathIsNullAndMediaType(memberId, MediaType.Video);
        final List<String> videoIds = contentList.stream().map(Content::getId).collect(Collectors.toList());
        List<Book> bookList = bookRepository.findBySourceVideoIdInAndErrorIsNull(videoIds);
        final List<String> bookIds = bookList.stream().map(Book::getId).collect(Collectors.toList());
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(bookIds);
        return resultStatus;
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
