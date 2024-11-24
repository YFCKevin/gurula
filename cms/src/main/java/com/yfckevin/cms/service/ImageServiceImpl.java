package com.yfckevin.cms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfckevin.cms.ConfigProperties;
import com.yfckevin.cms.config.RabbitMQConfig;
import com.yfckevin.cms.dto.ImageCompletionResponse;
import com.yfckevin.cms.entity.Book;
import com.yfckevin.cms.entity.Content;
import com.yfckevin.cms.repository.BookRepository;
import com.yfckevin.cms.repository.ContentRepository;
import com.yfckevin.common.dto.inkCloud.WorkFlowDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService{
    private final ContentRepository contentRepository;
    private final BookRepository bookRepository;
    private final ConfigProperties configProperties;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final SimpleDateFormat sdf;

    public ImageServiceImpl(BookRepository bookRepository, ConfigProperties configProperties, RestTemplate restTemplate, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper,
                            ContentRepository contentRepository, @Qualifier("sdf") SimpleDateFormat sdf) {
        this.bookRepository = bookRepository;
        this.configProperties = configProperties;
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.contentRepository = contentRepository;
        this.sdf = sdf;
    }

    @Override
    @RabbitListener(queues = RabbitMQConfig.IMAGE_QUEUE)
    public void generateImage(WorkFlowDTO workFlowDTO) throws IOException {

        final Book book = bookRepository.findById(workFlowDTO.getBookId()).get();

        final String bookName = workFlowDTO.getBookName();
        final String content = workFlowDTO.getNarration();

        String url = "https://api.openai.com/v1/images/generations";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(configProperties.getOpenAIApiKey());

        String payload = createImagePayload(content);

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<ImageCompletionResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ImageCompletionResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("OpenAI回傳的status code: {}", response);
            List<Path> downloadedPaths = new ArrayList<>();
            List<String> fileNames = new ArrayList<>();
            ImageCompletionResponse responseBody = response.getBody();
            for (ImageCompletionResponse.DataDTO imgUrl : responseBody.getData()) {
                try {
                    URL imageUrl = new URL(imgUrl.getUrl());
                    try (InputStream inputStream = imageUrl.openStream()) {
                        String fileName = Paths.get(imageUrl.getPath()).getFileName().toString();
                        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : ".jpg";
                        String newFileName = bookName + "_" + System.currentTimeMillis() + extension;
                        Path filePath = Paths.get(configProperties.getPicSavePath()).resolve(newFileName);
                        Files.copy(inputStream, filePath);
                        System.out.println("圖片已下載: " + filePath);

                        downloadedPaths.add(filePath);
                        fileNames.add(newFileName);
                    }
                } catch (IOException e) {
                    workFlowDTO.setCode("C999");
                    workFlowDTO.setMsg("下載圖片時發生錯誤");
                    book.setError("下載圖片時發生錯誤");
                    bookRepository.save(book);
                    log.error("下載圖片時發生錯誤: {}", e.getMessage());
                    rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.error", workFlowDTO);
                }
            }

            boolean allFilesValid = downloadedPaths.stream()
                    .allMatch(path -> {
                        try {
                            return Files.exists(path) && Files.size(path) > 0;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            List<Content> images = new ArrayList<>();
            for (Path path : downloadedPaths) {
                Content image = new Content();
                image.setTitle(path.getFileName().toString());
                image.setMediaType(com.yfckevin.cms.enums.MediaType.Image);
                image.setPath(path.toString());
                image.setSize(Files.size(path));
                image.setCreationDate(sdf.format(new Date()));
                images.add(image);
            }
            contentRepository.saveAll(images);

            if (allFilesValid) {
                log.info("圖片儲存成功，繼續執行製作影片");
                workFlowDTO.setCode("C000");
                workFlowDTO.setMsg("成功");
                workFlowDTO.setImageName(fileNames.get(0)); //只取第一張 String.join(",", fileNames)
                System.out.println("fileNames.get(0): " + fileNames.get(0));
                rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.video", workFlowDTO);
            } else {
                workFlowDTO.setCode("C999");
                workFlowDTO.setMsg("圖片儲存失敗");
                book.setError("圖片儲存失敗");
                bookRepository.save(book);
                log.error("圖片儲存失敗，導向錯誤");
                rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.error", workFlowDTO);
            }

        } else {
            workFlowDTO.setCode("C999");
            workFlowDTO.setMsg("openAI錯誤發生");
            book.setError("[產圖] openAI錯誤發生");
            bookRepository.save(book);
            log.error("openAI錯誤發生，狀態碼：{}，導向錯誤", response.getStatusCode());
            rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.error", workFlowDTO);
        }
    }


    private String createImagePayload(String prompt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "dall-e-2");
        payload.put("prompt", prompt);
        payload.put("n", 1);
        payload.put("size", "512x512");

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
