package com.ckt.api.contentManagement.service;

import com.ckt.api.contentManagement.model.entity.Component;
import com.ckt.api.contentManagement.model.entity.Content;
import com.ckt.api.contentManagement.repository.ComponentRepository;
import org.springframework.stereotype.Service;

@Service
public class ComponentService {

    private final ComponentRepository componentRepository;

    public ComponentService(ComponentRepository componentRepository) {
        this.componentRepository = componentRepository;
    }

    public Component createComponent(Content content, Long position) {
        Component component = new Component();
        component.setPosition(position);
        component.setContent(content);
        component = componentRepository.save(component);

        return component;
    }

    public void updateComponent() {

    }
}
