package com.yfckevin.cms.service;

import com.yfckevin.common.dto.inkCloud.ImageRequestDTO;
import com.yfckevin.common.dto.inkCloud.SearchDTO;
import com.yfckevin.cms.entity.Book;
import com.yfckevin.common.dto.inkCloud.BookDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public interface BookService {
    List<Book> findBook(SearchDTO searchDTO);

    int deleteBook(String id);

    int editBook(BookDTO bookDTO);

    Map<String, Integer> saveMultiBook(List<ImageRequestDTO> imageRequestDTOs, String memberId);
}
