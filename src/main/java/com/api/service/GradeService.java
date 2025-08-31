package com.api.service;

import com.api.dto.GradeRequest;
import com.api.entity.Grade;
import com.api.repository.GradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;

    public GradeService(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    public void create(GradeRequest request){

        Grade grade = new Grade();

        grade.setLevel(request.getLevel());
        grade.setLabelEn(request.getLabelEn());
        grade.setLabelKm(request.getLabelKm());
        grade.setImage(request.getImage());

        gradeRepository.save(grade);
    }

    public List<Grade> findAll() {
        return gradeRepository.findAll();
    }
}
