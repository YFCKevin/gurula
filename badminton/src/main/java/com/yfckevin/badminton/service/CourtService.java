package com.yfckevin.badminton.service;


import com.yfckevin.badminton.entity.Court;

import java.util.List;
import java.util.Optional;

public interface CourtService {

    void save(Court court);

    Optional<Court> findById(String id);

    List<Court> findAllByOrderByCreationDateAsc();

    void delete(Court court);

    List<Court> findCourtByCondition(String keyword);

    void saveAll(List<Court> courtList);

    Optional<Court> findByPostId(String id);
}
