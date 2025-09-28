package com.api.content_management.controller;


import com.api.content_management.model.dto.StoreContentRequest;
import com.api.content_management.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping("/{dataStructureId}")
    public ResponseEntity<Void> create(@PathVariable Long dataStructureId, @RequestBody StoreContentRequest request) {
        contentService.create(request, dataStructureId);

        return ResponseEntity.status(204).build();
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ContentDetailDto> findById(@PathVariable Long id) {
//        ContentDetailDto contentDto = contentService.findById(id);
//        return ResponseEntity.ok(contentDto);
//    }

}
