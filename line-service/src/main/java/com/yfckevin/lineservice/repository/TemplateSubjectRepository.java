package com.yfckevin.lineservice.repository;

import com.yfckevin.lineservice.entity.TemplateSubject;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TemplateSubjectRepository extends MongoRepository<TemplateSubject, String> {
}
