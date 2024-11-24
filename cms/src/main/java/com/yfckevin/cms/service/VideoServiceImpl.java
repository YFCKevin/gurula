package com.yfckevin.cms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yfckevin.cms.ConfigProperties;
import com.yfckevin.cms.config.RabbitMQConfig;
import com.yfckevin.cms.entity.Book;
import com.yfckevin.cms.entity.Content;
import com.yfckevin.cms.repository.BookRepository;
import com.yfckevin.cms.repository.ContentRepository;
import com.yfckevin.common.dto.inkCloud.WorkFlowDTO;
import com.yfckevin.common.exception.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class VideoServiceImpl implements VideoService {
    private final BookRepository bookRepository;
    private final ConfigProperties configProperties;
    private final ContentRepository contentRepository;
    private final SimpleDateFormat sdf;

    public VideoServiceImpl(ConfigProperties configProperties,
                            ContentRepository contentRepository, @Qualifier("sdf") SimpleDateFormat sdf,
                            BookRepository bookRepository) {
        this.configProperties = configProperties;
        this.contentRepository = contentRepository;
        this.sdf = sdf;
        this.bookRepository = bookRepository;
    }

    @Override
    @RabbitListener(queues = RabbitMQConfig.VIDEO_QUEUE)
    public void generateVideo(WorkFlowDTO workFlowDTO) throws JsonProcessingException {
        ResultStatus resultStatus = new ResultStatus();

        final Book book = bookRepository.findById(workFlowDTO.getBookId()).get();

        final Optional<Content> opt = contentRepository.findById(workFlowDTO.getVideoId());
        if (opt.isEmpty()) {
            resultStatus.setCode("C002");
            resultStatus.setMessage("查無影片");
        } else {
            final Content video = opt.get();
            final String bookName = workFlowDTO.getBookName();
            final String audioPath = workFlowDTO.getAudioPath();
            final String imageNames = workFlowDTO.getImageName();

            String outputPath = configProperties.getVideoSavePath() + bookName + "_" + System.currentTimeMillis() + ".mp4";

            // FFmpeg 命令
            String[] command = {
                    "ffmpeg",
                    "-loop", "1",                      // 循環圖片
                    "-i", configProperties.getPicSavePath() + imageNames, // 輸入圖片
                    "-i", audioPath,                  // 輸入音訊
                    "-c:v", "libx264",                // 設置視頻編碼器
                    "-tune", "stillimage",            // 調整為靜態圖片
                    "-c:a", "aac",                    // 設置音訊編碼器
                    "-b:a", "192k",                   // 設置音訊比特率
                    "-pix_fmt", "yuv420p",            // 設置像素格式
                    "-shortest",                       // 使視頻和音訊長度一致
                    outputPath                         // 輸出文件路徑
            };

            // 执行 FFmpeg 命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            try {
                Process process = processBuilder.start();

                // 输出 FFmpeg 命令的標準輸出
                try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                // 等待 FFmpeg 命令完成
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    video.setCreationDate(sdf.format(new Date()));
                    video.setPath(outputPath);
                    File videoFile = new File(outputPath);
                    if (videoFile.exists()) {
                        video.setTitle(videoFile.getName());
                        video.setSize(Files.size(videoFile.toPath()));
                    }
                    contentRepository.save(video);
                    resultStatus.setCode("C000");
                    resultStatus.setMessage("成功");
                    log.info("影片製作完成");
                } else {
                    resultStatus.setCode("C999");
                    resultStatus.setMessage("FFmpeg失敗");
                    book.setError("FFmpeg失敗");
                    bookRepository.save(book);
                    log.error("FFmpeg失敗，退出指令：{}", exitCode);
                }

            } catch (IOException | InterruptedException e) {
                log.error("例外發生：{}", e.getMessage());
                resultStatus.setCode("C999");
                resultStatus.setMessage("例外發生");
                book.setError("FFmpeg例外發生");
                bookRepository.save(book);
            }
        }
    }
}
