package com.yfckevin.lineservice.service;

import com.yfckevin.lineservice.entity.TemplateSubject;

import java.util.List;
import java.util.Optional;

public interface TemplateSubjectService {
    Optional<TemplateSubject> findById(String subjectId);

    TemplateSubject save(TemplateSubject templateSubject);
    List<TemplateSubject> findAllAndOrderByCreationDate();

    List<TemplateSubject> templateSearch(String keyword);
}
