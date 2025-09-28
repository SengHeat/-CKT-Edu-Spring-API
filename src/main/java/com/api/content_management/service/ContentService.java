package com.api.content_management.service;

import com.api.content_management.model.dto.StoreContentRequest;
import com.api.content_management.model.entity.Content;
import com.api.content_management.model.entity.DataStructure;
import com.api.content_management.repository.ContentRepository;
import com.api.user.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final DataStructureService dataStructureService;
    private final ComponentService componentService;

    public ContentService(ContentRepository contentRepository, DataStructureService dataStructureService, ComponentService componentService) {
        this.contentRepository = contentRepository;
        this.dataStructureService = dataStructureService;
        this.componentService = componentService;
    }

    public void create(StoreContentRequest request, Long dataStructureId) {
        Content content = new Content();

        DataStructure dataStructure = dataStructureService.findEntityById(dataStructureId);

        content.setDescription(request.getDescription());
        content.setTitle(request.getTitle());
        content.setDataStructure(dataStructure);

        contentRepository.save(content);
    }

    public void update() {

    }

    public Content findById(Long id) {
        return contentRepository.findById(id).orElseThrow(() -> new RuntimeException("Content not found"));
    }

    public void delete(Long id) {

    }

    public Long countComponentById(Long id) {
        return contentRepository.countComponentsById(id);
    }


}
