package com.yfckevin.cms.service;

import com.yfckevin.common.dto.inkCloud.ImageRequestDTO;
import com.yfckevin.common.dto.inkCloud.SearchDTO;
import com.yfckevin.cms.entity.Book;
import com.yfckevin.common.dto.inkCloud.BookDTO;
import com.yfckevin.common.dto.inkCloud.VideoRequestDTO;
import com.yfckevin.common.exception.ResultStatus;

import java.util.List;
import java.util.Map;

public interface BookService {
    List<Book> findBook(SearchDTO searchDTO);

    int deleteBook(String id);

    int editBook(String memberId, BookDTO bookDTO);

    Map<String, Integer> saveMultiBook(List<ImageRequestDTO> imageRequestDTOs, String memberId);

    ResultStatus previewBookStatus(String bookId);

    ResultStatus constructVideo(String memberId, VideoRequestDTO dto);

    ResultStatus getVideoId(String bookId, String memberId);

    ResultStatus getPreviewStatus(String memberId);
}
