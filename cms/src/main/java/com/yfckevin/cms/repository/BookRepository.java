package com.yfckevin.cms.repository;

import com.yfckevin.cms.entity.Book;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository extends MongoRepository<Book, String> {
}
