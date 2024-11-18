package com.yfckevin.cms.repository;

import com.yfckevin.cms.entity.ErrorFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ErrorFileRepository extends MongoRepository<ErrorFile, String> {
}
