package com.yfckevin.badminton.controller;

import com.yfckevin.badminton.dto.LeaderRequestDTO;
import com.yfckevin.badminton.entity.Leader;
import com.yfckevin.badminton.service.LeaderService;
import com.yfckevin.common.exception.ResultStatus;
import com.yfckevin.common.utils.MemberContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class LeaderController {
    private final LeaderService leaderService;
    Logger logger = LoggerFactory.getLogger(LeaderController.class);
    public LeaderController(LeaderService leaderService) {
        this.leaderService = leaderService;
    }

    @GetMapping("/findAllLeader")
    public ResponseEntity<?> findAllLeader (){
        logger.info("[findAllLeader]");
        ResultStatus resultStatus = new ResultStatus();
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(leaderService.findAllAndOrderByCreationDate());
        return ResponseEntity.ok(resultStatus);
    }

    @PostMapping("/allLeaderByUserIds")
    public ResponseEntity<?> allLeaderByUserIds (@RequestBody LeaderRequestDTO dto){
        logger.info("[allLeaderByUserIds]");
        ResultStatus resultStatus = new ResultStatus();
        final List<Leader> leaderList = leaderService.findAllByUserIdIn(dto.getUserIdList());
        resultStatus.setCode("C000");
        resultStatus.setMessage("成功");
        resultStatus.setData(leaderList);
        return ResponseEntity.ok(resultStatus);
    }



    @GetMapping("/getLeaderInfoByUserId/{userId}")
    public ResponseEntity<?> getLeaderInfoByUserId (@PathVariable String userId){
        logger.info("[getLeaderInfoByUserId]");
        ResultStatus resultStatus = new ResultStatus();
        final Optional<Leader> opt = leaderService.findByUserId(userId);
        if (opt.isEmpty()) {
            resultStatus.setCode("C002");
            resultStatus.setMessage("查無團主");
        } else {
            final Leader leader = opt.get();
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(leader);
        }
        return ResponseEntity.ok(resultStatus);
    }


    @GetMapping("/getLeaderInfoById/{id}")
    public ResponseEntity<?> getLeaderInfoById (@PathVariable String id){
        logger.info("[getLeaderInfoByUserId]");
        ResultStatus resultStatus = new ResultStatus();
        final Optional<Leader> opt = leaderService.findById(id);
        if (opt.isEmpty()) {
            resultStatus.setCode("C002");
            resultStatus.setMessage("查無團主");
        } else {
            final Leader leader = opt.get();
            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(leader);
        }
        return ResponseEntity.ok(resultStatus);
    }
}
