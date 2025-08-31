package com.api.service;


import com.api.dto.GradeRequest;
import com.api.dto.SubjectRequest;
import com.api.entity.Grade;
import com.api.entity.Subject;
import com.api.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;


    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public void create(SubjectRequest request){
        Subject subject = new Subject();
        Map<String, Object> meta = new HashMap<>();
        meta.put("level", 3);
        meta.put("tags", List.of("algebra", "geometry"));
        meta.put("extra", Map.of("author", "Seng"));
        subject.setMeta(meta);

        subject.setLabelEn(request.getLabelEn());
        subject.setLabelKm(request.getLabelKm());
        subject.setImage(request.getImage());

        subjectRepository.save(subject);
    }

    public List<Subject> findAll() {
        List<Subject> subjects = subjectRepository.findAll();
        if (!subjects.isEmpty()) {
            System.out.println(subjects.getFirst().getMeta()); // prints meta correctly
        }
        System.out.println("Hello E");
        return subjects;
    }
}
