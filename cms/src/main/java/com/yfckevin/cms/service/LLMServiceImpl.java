package com.yfckevin.cms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yfckevin.cms.ConfigProperties;
import com.yfckevin.cms.config.RabbitMQConfig;
import com.yfckevin.cms.dto.ChatCompletionResponse;
import com.yfckevin.cms.entity.Book;
import com.yfckevin.cms.entity.Content;
import com.yfckevin.cms.repository.BookRepository;
import com.yfckevin.cms.repository.ContentRepository;
import com.yfckevin.common.dto.inkCloud.NarrationMsgDTO;
import com.yfckevin.common.dto.inkCloud.WorkFlowDTO;
import com.yfckevin.common.exception.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class LLMServiceImpl implements LLMService{
    private final BookRepository bookRepository;
    private final ContentRepository contentRepository;
    private final ConfigProperties configProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SimpleDateFormat sdf;
    private final RabbitTemplate rabbitTemplate;

    public LLMServiceImpl(ConfigProperties configProperties, RestTemplate restTemplate, ObjectMapper objectMapper,
                          ContentRepository contentRepository, @Qualifier("sdf") SimpleDateFormat sdf, RabbitTemplate rabbitTemplate,
                          BookRepository bookRepository) {
        this.configProperties = configProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.contentRepository = contentRepository;
        this.sdf = sdf;
        this.rabbitTemplate = rabbitTemplate;
        this.bookRepository = bookRepository;
    }

    @Override
    public ResultStatus<String> constructBookEntity(String rawText) {
        ResultStatus<String> resultStatus = new ResultStatus<>();
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(configProperties.getOpenAIApiKey());

        String data = createPayload(rawText + "\n" + prompt_book);

        HttpEntity<String> entity = new HttpEntity<>(data, headers);

        ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ChatCompletionResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("OpenAI回傳的status code: {}", response);
            ChatCompletionResponse responseBody = response.getBody();
            String content = extractJsonContent(responseBody);
            System.out.println("GPT回傳資料 ======> " + content);

            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(content);
        } else {
            log.error("openAI錯誤發生");
            resultStatus.setCode("C999");
            resultStatus.setMessage("異常發生");
        }
        return resultStatus;
    }

    @Override
    @RabbitListener(queues = RabbitMQConfig.LLM_QUEUE)
    public void constructNarration(NarrationMsgDTO dto) {
        final Book book = bookRepository.findById(dto.getBookId()).get();

        WorkFlowDTO workFlowDTO = new WorkFlowDTO();

        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(configProperties.getOpenAIApiKey());

        String data = createPayload(dto.getText() + "\n" + prompt_video);

        HttpEntity<String> entity = new HttpEntity<>(data, headers);

        ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ChatCompletionResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("OpenAI回傳的status code: {}", response);
            ChatCompletionResponse responseBody = response.getBody();
            String content = extractTextContent(responseBody);
            System.out.println("GPT回傳資料 ======> " + content);

            Content narration = new Content();
            narration.setText(content);
            narration.setCreationDate(sdf.format(new Date()));
            narration.setMemberId(dto.getMemberId());
            narration.setMediaType(com.yfckevin.cms.enums.MediaType.Text);
            Content savedNarration = contentRepository.save(narration);

            if (savedNarration != null) {

                book.setSourceNarrationId(savedNarration.getId());
                bookRepository.save(book);

                workFlowDTO.setCode("C000");
                workFlowDTO.setMsg("成功");
                workFlowDTO.setBookId(dto.getBookId());
                workFlowDTO.setBookName(dto.getBookName());
                workFlowDTO.setNarrationId(savedNarration.getId());
                workFlowDTO.setNarration(savedNarration.getText());
                workFlowDTO.setVideoId(dto.getVideoId());
                workFlowDTO.setMemberId(savedNarration.getMemberId());
                log.info("旁白儲存成功，繼續執行製作mp3");
                rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.audio", workFlowDTO);
            } else {
                workFlowDTO.setCode("C999");
                workFlowDTO.setMsg("旁白儲存失敗");
                book.setError("旁白儲存失敗");
                bookRepository.save(book);
                log.error("旁白儲存失敗，導向錯誤");
                rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.error", workFlowDTO);
            }
        } else {
            workFlowDTO.setCode("C999");
            workFlowDTO.setMsg("openAI錯誤發生");
            book.setError("[旁白] openAI錯誤發生");
            bookRepository.save(book);
            log.error("[旁白] openAI錯誤發生，狀態碼：{}，導向錯誤", response.getStatusCode());
            rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.error", workFlowDTO);
        }
    }


    private String createPayload(String prompt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4o-mini");

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        payload.put("messages", new Object[]{message});

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractJsonContent(ChatCompletionResponse responseBody) {
        if (responseBody != null && !responseBody.getChoices().isEmpty()) {
            ChatCompletionResponse.Choice choice = responseBody.getChoices().get(0);
            if (choice != null && choice.getMessage() != null) {
                String content = choice.getMessage().getContent().trim();

                // 去掉反引號
                if (content != null) {
                    content = content.replace("```json", "").replace("```", "").trim();
                }

                return content;
            }
        }
        return null;
    }

    private String extractTextContent(ChatCompletionResponse responseBody) {
        if (responseBody != null && !responseBody.getChoices().isEmpty()) {
            ChatCompletionResponse.Choice choice = responseBody.getChoices().get(0);
            if (choice != null && choice.getMessage() != null) {
                return choice.getMessage().getContent().trim();
            }
        }
        return null;
    }


    public final String prompt_book =
            "1. 書籍如上，我要把資訊整理出 JSON 檔，欄位包括：\n" +
                    "- title：書名\n" +
                    "- author：作者，書籍的作者名稱，可以是單個或多個作者，若有多個用逗號相連\n" +
                    "- publisher：出版社，出版該書籍的出版社名稱\n" +
                    "2. 內容文字可能包含多本書籍資訊，必須分開成獨立的 JSON 物件。\n" +
                    "每本書籍資訊應用 {} 包含，並且書籍之間應用 , 分隔。\n" +
                    "3. 每本書籍資訊只包括以下三個欄位：\n" +
                    "- title (String)\n" +
                    "- author (String)\n" +
                    "- publisher (String)\n" +
                    "4. 請確保輸出為有效的 JSON 格式，並且滿足如下要求：\n" +
                    "- 若作者有多個，請用逗號分隔\n" +
                    "- 任何其他非書籍的文本內容應忽略\n" +
                    "5. 輸入的書籍資訊文本範例如下：\n" +
                    "書名: 初心\n" +
                    "作者: 江振誠\n" +
                    "出版社: 平安叢書\n\n" +
                    "書名: 舌尖上的古代中國\n" +
                    "作者: 古人很潮\n" +
                    "出版社: 遠流\n\n" +
                    "6. 輸出格式範例如下：\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"title\": \"初心\",\n" +
                    "    \"author\": \"江振誠\",\n" +
                    "    \"publisher\": \"平安叢書\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"舌尖上的古代中國\",\n" +
                    "    \"author\": \"古人很潮\",\n" +
                    "    \"publisher\": \"遠流\"\n" +
                    "  }\n" +
                    "]\n\n" +
                    "7. 確保輸出只有 JSON 格式，無需多餘的描述或文本。\n" +
                    "8. 如果某本書的 title、author 或 publisher 欄位缺失，該欄位設定為空字串。";


    public final String prompt_video =
            "1.書名與作者如上，我要你給我200字的摘要文章，內容包含：\n" +
                    "書籍主題或核心思想\n" +
                    "主要情節\n" +
                    "重點內容\n" +
                    "從中學到什麼或感受到什麼\n" +
                    "寫作風格或特色\n" +
                    "為何值得一讀";
}
