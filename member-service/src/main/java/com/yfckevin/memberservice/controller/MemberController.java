package com.yfckevin.memberservice.controller;

import com.yfckevin.common.dto.member.MemberDTO;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.memberservice.entity.Member;
import com.yfckevin.memberservice.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }


    /**
     * 取得會員資訊
     * @param memberId
     * @return
     */
    @GetMapping("/getMemberInfo/{memberId}")
    public ResponseEntity<?> getMemberInfo (@PathVariable String memberId){
        ResultStatus resultStatus = new ResultStatus();
        Optional<Member> opt = memberService.findById(memberId);
        if (opt.isEmpty()) {
            resultStatus.setCode("C014");
            resultStatus.setMessage("查無會員");
        } else {
            final Member member = opt.get();
            final MemberDTO dto = constructMemberDTO(member);
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(dto);
        }
        return ResponseEntity.ok(resultStatus);
    }

    private static MemberDTO constructMemberDTO(Member member) {
        MemberDTO dto = new MemberDTO();
        dto.setId(member.getId());
        dto.setName(member.getName());
        dto.setUserId(member.getUserId());
        dto.setModificationDate(member.getModificationDate());
        dto.setCreationDate(member.getCreationDate());
        dto.setPictureUrl(member.getPictureUrl());
        dto.setEmail(member.getEmail());
        dto.setSuspendDate(member.getSuspendDate());
        dto.setProvider(member.getProvider().toString());
        dto.setRole(member.getRole().toString());
        return dto;
    }
}
