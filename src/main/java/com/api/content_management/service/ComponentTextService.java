package com.api.content_management.service;

import com.api.content_management.model.dto.StoreComponentTextRequest;
import com.api.content_management.model.entity.Component;
import com.api.content_management.model.entity.ComponentText;
import com.api.content_management.model.entity.Content;
import com.api.content_management.repository.ComponentTextRepository;
import org.springframework.stereotype.Service;

@Service
public class ComponentTextService {

    private final ComponentTextRepository componentTextRepository;
    private final ComponentService componentService;
    private final ContentService contentService;

    public ComponentTextService(ComponentTextRepository componentTextRepository, ComponentService componentService, ContentService contentService) {
        this.componentTextRepository = componentTextRepository;
        this.componentService = componentService;
        this.contentService = contentService;
    }

    public void createComponentText(StoreComponentTextRequest request, Long contentId) {

        Content content = contentService.findById(contentId);
        Long position = contentService.countComponentById(contentId) + 1;
        Component component = componentService.createComponent(content, position);

        ComponentText componentText = new ComponentText();
        componentText.setDataType(request.getDataType());
        componentText.setData(request.getData());
        componentText.setComponent(component);
        componentTextRepository.save(componentText);
    }
}
