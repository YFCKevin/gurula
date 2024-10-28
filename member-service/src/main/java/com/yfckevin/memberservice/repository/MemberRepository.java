package com.yfckevin.memberservice.repository;

import com.yfckevin.memberservice.entity.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByUserId(String userId);
}
