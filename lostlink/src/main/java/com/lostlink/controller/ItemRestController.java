
package com.lostlink.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lostlink.dto.ItemRequest;
import com.lostlink.dto.ItemResponse;
import com.lostlink.exception.ItemNotFoundException;
import com.lostlink.model.LostFoundItem;
import com.lostlink.repository.LostFoundItemRepository;
import com.lostlink.service.ItemApiService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/items")
public class ItemRestController {

    private final LostFoundItemRepository itemRepository;
    private final ItemApiService itemApiService;

    // Constructor Dependency Injection
    public ItemRestController(
            LostFoundItemRepository itemRepository,
            ItemApiService itemApiService) {

        this.itemRepository = itemRepository;
        this.itemApiService = itemApiService;
    }

    // =====================================
    // 1. GET ALL ITEMS
    // GET /api/items
    // =====================================

    @GetMapping
    public List<ItemResponse> getAllItems() {

        return itemRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ItemResponse::from)
                .toList();
    }

    // =====================================
    // 2. GET ITEM BY ID
    // GET /api/items/{id}
    // =====================================

    @GetMapping("/{id}")
    public ItemResponse getItemById(
            @PathVariable Long id) {

        LostFoundItem item = itemRepository.findById(id)
                .orElseThrow(() ->
                        new ItemNotFoundException(id));

        return ItemResponse.from(item);
    }

    // =====================================
    // 3. CREATE ITEM
    // POST /api/items
    // =====================================

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody ItemRequest request,
            Authentication authentication) {

        LostFoundItem savedItem =
                itemApiService.createItem(
                        request,
                        authentication.getName());

        URI location = URI.create(
                "/api/items/" + savedItem.getId());

        return ResponseEntity
                .created(location)
                .body(ItemResponse.from(savedItem));
    }

    // =====================================
    // 4. UPDATE ITEM
    // PUT /api/items/{id}
    // =====================================

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request,
            Authentication authentication) {

        LostFoundItem updatedItem =
                itemApiService.updateItem(
                        id,
                        request,
                        authentication.getName());

        return ResponseEntity
                .ok(ItemResponse.from(updatedItem));
    }

    // =====================================
    // 5. DELETE ITEM
    // DELETE /api/items/{id}
    // =====================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            Authentication authentication) {

        itemApiService.deleteItem(
                id,
                authentication.getName());

        return ResponseEntity
                .noContent()
                .build();
    }
}
