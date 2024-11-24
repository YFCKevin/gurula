package com.yfckevin.api.api.inkCloud;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.dto.inkCloud.BookResponseDTO;
import com.yfckevin.common.dto.inkCloud.BookDTO;
import com.yfckevin.common.dto.inkCloud.ImageRequestDTO;
import com.yfckevin.common.dto.inkCloud.SearchDTO;
import com.yfckevin.common.dto.inkCloud.VideoRequestDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@BaseRequest(baseURL = "${cmsDomain}")
public interface BookApi {

    @Get(url = "/showBookcase/{memberId}")
    ForestResponse<ResultStatus<List<BookResponseDTO>>> showBookcase(@Var("memberId") String memberId);

    @Delete(url = "deleteBook/{bookId}")
    ForestResponse<ResultStatus<?>> deleteBook(@Var("bookId") String bookId);

    @Post(url = "/editBook", contentType = "application/json")
    ForestResponse<ResultStatus<?>> editBook(@Body BookDTO bookDTO);

    @Post(url = "/searchBook", contentType = "application/json")
    ForestResponse<ResultStatus<List<BookResponseDTO>>> searchBook(@Body SearchDTO searchDTO);

    @Post(url = "/saveMultiBook", contentType = "application/json")
    ForestResponse<ResultStatus<Map<String, Integer>>> saveMultiBook(@Body List<ImageRequestDTO> imageRequestDTOS);

    @Get(url = "/getVideoId/{bookId}")
    ForestResponse<ResultStatus<String>> getVideoId(@Var("bookId") String bookId);

    @Get(url = "/previewBook/{bookId}")
    ForestResponse<ResultStatus<String>> previewBook(@Var("bookId") String bookId);

    @Post(url = "/constructVideo", contentType = "application/json")
    ForestResponse<ResultStatus<?>> constructVideo(@Body VideoRequestDTO dto);

    @Get(url = "/getPreviewStatus")
    ForestResponse<ResultStatus<List<String>>> getPreviewStatus();
}
