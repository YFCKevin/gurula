package com.yfckevin.cms.repository;

import com.yfckevin.cms.entity.Content;
import com.yfckevin.cms.enums.MediaType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContentRepository extends MongoRepository<Content, String> {
    List<Content> findByMemberIdAndDeletionDateIsNullAndPathIsNullAndMediaType(String memberId, MediaType mediaType);
}
