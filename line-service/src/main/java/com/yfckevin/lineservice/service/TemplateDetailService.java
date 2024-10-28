package com.yfckevin.lineservice.service;

import com.yfckevin.lineservice.entity.TemplateDetail;

import java.util.List;
import java.util.Optional;

public interface TemplateDetailService {
    TemplateDetail save(TemplateDetail templateDetail);

    List<TemplateDetail> findByIdIn(List<String> detailIds);

    Optional<TemplateDetail> findById(String detailId);
}
