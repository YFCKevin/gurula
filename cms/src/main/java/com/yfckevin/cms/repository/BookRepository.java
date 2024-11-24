package com.yfckevin.cms.repository;

import com.yfckevin.cms.entity.Book;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookRepository extends MongoRepository<Book, String> {
    List<Book> findBySourceVideoIdInAndErrorIsNull(List<String> videoIds);
}
