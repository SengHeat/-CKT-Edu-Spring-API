package com.api.content_management.service;

import com.api.content_management.model.entity.Component;
import com.api.content_management.model.entity.Content;
import com.api.content_management.repository.ComponentRepository;
import com.api.user.service.AuthService;
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
