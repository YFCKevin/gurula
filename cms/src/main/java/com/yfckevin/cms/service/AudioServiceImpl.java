package com.yfckevin.cms.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.yfckevin.cms.ConfigProperties;
import com.yfckevin.cms.config.RabbitMQConfig;
import com.yfckevin.cms.entity.Book;
import com.yfckevin.cms.entity.Content;
import com.yfckevin.cms.repository.BookRepository;
import com.yfckevin.cms.repository.ContentRepository;
import com.yfckevin.common.dto.inkCloud.WorkFlowDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Service
public class AudioServiceImpl implements AudioService{
    private final BookRepository bookRepository;
    private final ContentRepository contentRepository;
    private final ConfigProperties configProperties;
    private final SimpleDateFormat sdf;
    private final RabbitTemplate rabbitTemplate;

    public AudioServiceImpl(ContentRepository contentRepository, ConfigProperties configProperties, @Qualifier("sdf") SimpleDateFormat sdf, RabbitTemplate rabbitTemplate,
                            BookRepository bookRepository) {
        this.contentRepository = contentRepository;
        this.configProperties = configProperties;
        this.sdf = sdf;
        this.rabbitTemplate = rabbitTemplate;
        this.bookRepository = bookRepository;
    }

    @Override
    @RabbitListener(queues = RabbitMQConfig.AUDIO_QUEUE)
    public void textToSpeech(WorkFlowDTO workFlowDTO) {

        final Book book = bookRepository.findById(workFlowDTO.getBookId()).get();

        final String bookName = workFlowDTO.getBookName();
        final String content = workFlowDTO.getNarration();
        final String memberId = workFlowDTO.getMemberId();

        FileSystemResource resource = new FileSystemResource(configProperties.getVisionAIJsonPath() + "text-and-speech-secret-key.json");

        // 取得憑證資料
        try (InputStream inputStream = resource.getInputStream()) {
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> ServiceAccountCredentials.fromStream(inputStream))
                    .build();

            // 使用 TextToSpeechClient 打 API
            try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {

                // construct request
                SynthesisInput input = SynthesisInput.newBuilder().setText(content).build();
                VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                        .setLanguageCode("zh-TW")
                        .setSsmlGender(SsmlVoiceGender.FEMALE)
                        .build();
                AudioConfig audioConfig = AudioConfig.newBuilder()
                        .setAudioEncoding(AudioEncoding.MP3)
                        .build();

                // 打 Text-to-Speech API
                SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

                byte[] audioContents = response.getAudioContent().toByteArray();
                try (OutputStream out = new FileOutputStream(configProperties.getAudioSavePath() + bookName + ".mp3")) {
                    out.write(audioContents);
                    Content audio = new Content();
                    audio.setPath(configProperties.getAudioSavePath() + bookName + ".mp3");
                    final Path filePath = Paths.get(configProperties.getAudioSavePath() + bookName + ".mp3");
                    audio.setSize(Files.size(filePath));
                    audio.setCreationDate(sdf.format(new Date()));
                    audio.setTitle(bookName + "_" + System.currentTimeMillis());
                    audio.setMemberId(memberId);
                    Content savedAudio = contentRepository.save(audio);

                    try {
                        if (savedAudio != null) {

                            book.setSourceAudioId(savedAudio.getId());
                            bookRepository.save(book);

                            workFlowDTO.setCode("C000");
                            workFlowDTO.setMsg("成功");
                            workFlowDTO.setAudioId(savedAudio.getId());
                            workFlowDTO.setAudioPath(savedAudio.getPath());
                            log.info("音訊儲存成功，繼續執行生成圖片");
                            rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.image", workFlowDTO);
                        } else {
                            workFlowDTO.setCode("C999");
                            workFlowDTO.setMsg("音訊儲存失敗");
                            book.setError("音訊儲存失敗");
                            bookRepository.save(book);
                            log.error("音訊儲存失敗，導向錯誤");
                            rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.error", workFlowDTO);
                        }
                    } catch (Exception e) {
                        workFlowDTO.setCode("C999");
                        workFlowDTO.setMsg("保存音訊時發生錯誤");
                        book.setError("保存音訊時發生錯誤");
                        bookRepository.save(book);
                        log.error("保存音訊時發生錯誤: {}", e.getMessage());
                        rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.error", workFlowDTO);
                    }
                }
            }
        } catch (IOException e) {
            workFlowDTO.setCode("C999");
            workFlowDTO.setMsg(e.getMessage());
            book.setError("[音訊] google發生錯誤");
            bookRepository.save(book);
            log.error(e.getMessage(), e);
            rabbitTemplate.convertAndSend(RabbitMQConfig.WORKFLOW_EXCHANGE, "workflow.error", workFlowDTO);
        }
    }
}
