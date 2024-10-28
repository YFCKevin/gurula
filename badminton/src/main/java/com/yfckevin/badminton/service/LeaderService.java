package com.yfckevin.badminton.service;


import com.yfckevin.badminton.dto.RequestPostDTO;
import com.yfckevin.badminton.entity.Leader;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LeaderService {
    int selectNewLeadersAndSave (List<RequestPostDTO> postDTOList) throws IOException;

    List<Leader> findAllByUserIdIn(Set<String> userIdList);

    List<Leader> findAllAndOrderByCreationDate();

    void save(Leader leader);

    Optional<Leader> findById(String id);

    List<Leader> findLeaderByConditions(String keyword, String startDate, String endDate);

    void deleteById(String id);

    Optional<Leader> findByUserId(String userId);
}
