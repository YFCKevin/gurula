package com.yfckevin.lineservice.repository;

import com.yfckevin.lineservice.entity.Follower;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FollowerRepository extends MongoRepository<Follower, String> {
    Optional<Follower> findByUserId(String userId);

    List<Follower> findByIdIn(List<String> userIds);
}
