package com.yfckevin.api.badminton.api.inkCloud;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import com.yfckevin.api.badminton.dto.inkCloud.BookResponseDTO;
import com.yfckevin.common.dto.inkCloud.BookDTO;
import com.yfckevin.common.dto.inkCloud.ImageRequestDTO;
import com.yfckevin.common.dto.inkCloud.SearchDTO;
import com.yfckevin.common.exception.ResultStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@BaseRequest(baseURL = "${cmsDomain}")
public interface BookApi {

    @Get(url = "/showBookcase")
    ForestResponse<ResultStatus<List<BookResponseDTO>>> showBookcase(@Query("memberId") String memberId);

    @Delete(url = "deleteBook/{bookId}")
    ForestResponse<ResultStatus<?>> deleteBook(@Var("bookId") String bookId);

    @Post(url = "/editBook", contentType = "application/json")
    ForestResponse<ResultStatus<?>> editBook(@Body BookDTO bookDTO);

    @Post(url = "/searchBook", contentType = "application/json")
    ForestResponse<ResultStatus<List<BookResponseDTO>>> searchBook(@Body SearchDTO searchDTO);

    @Post(url = "/saveMultiBook", contentType = "application/json")
    ForestResponse<ResultStatus<Map<String, Integer>>> saveMultiBook(@Body List<ImageRequestDTO> imageRequestDTOS);
}
