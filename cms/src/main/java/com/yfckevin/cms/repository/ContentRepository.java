package com.yfckevin.cms.repository;

import com.yfckevin.cms.entity.Content;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContentRepository extends MongoRepository<Content, String> {
}
