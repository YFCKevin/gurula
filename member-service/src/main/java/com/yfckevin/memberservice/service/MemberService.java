package com.yfckevin.memberservice.service;

import com.yfckevin.memberservice.entity.Member;

import java.util.Optional;

public interface MemberService {
    Optional<Member> findByEmail(String email);

    Member save(Member member);

    Optional<Member> findByUserId(String userId);
}
