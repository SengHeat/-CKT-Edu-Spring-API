package com.ckt.api.contentManagement.service;

import com.ckt.api.contentManagement.model.dto.ComponentCollapseRequest;
import com.ckt.api.contentManagement.model.dto.ComponentCollapseResponse;
import com.ckt.api.contentManagement.model.entity.ComponentCollapse;
import com.ckt.api.contentManagement.model.entity.Component;
import com.ckt.api.contentManagement.model.entity.Content;
import com.ckt.api.contentManagement.repository.ComponentCollapseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ComponentCollapseService {

    private final ComponentCollapseRepository componentCollapseRepository;
    private final ComponentService componentService;
    private final ContentService contentService;


    public void create(ComponentCollapseRequest request) {

        Content content = contentService.findById(request.getContentId());
        Long position = contentService.countComponentById(request.getContentId()) + 1;
        Component component = componentService.createComponent(content, position);
        ComponentCollapse componentCollapse = new ComponentCollapse();
        componentCollapse.setComponent(component);
        componentCollapse.setData(request.getData());

        componentCollapseRepository.save(componentCollapse);
    }

    @Transactional(readOnly = true)
    public List<ComponentCollapseResponse> findAll() {
        return componentCollapseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    private ComponentCollapseResponse mapToResponse(ComponentCollapse entity) {
        return ComponentCollapseResponse.builder()
                .id(entity.getId())
                .componentId(entity.getComponent().getId())
                .data(entity.getData())
                .build();
    }
}