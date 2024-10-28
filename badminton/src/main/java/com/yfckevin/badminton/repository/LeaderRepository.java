package com.yfckevin.badminton.repository;

import com.yfckevin.badminton.entity.Leader;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LeaderRepository extends MongoRepository<Leader, String> {
    List<Leader> findAllByUserIdIn(Set<String> userIdList);
    List<Leader> findAll(Sort sort);

    Optional<Leader> findByUserId(String userId);
}
