package com.lostlink.service;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import com.lostlink.exception.ItemNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.lostlink.dto.ItemRequest;
import com.lostlink.model.ItemStatus;
import com.lostlink.model.LostFoundItem;
import com.lostlink.repository.ItemClaimRepository;
import com.lostlink.repository.LostFoundItemRepository;
@Service
public class ItemApiService {

    private final LostFoundItemRepository itemRepository;
private final ItemClaimRepository claimRepository;

   public ItemApiService(
        LostFoundItemRepository itemRepository,
        ItemClaimRepository claimRepository) {

    this.itemRepository = itemRepository;
    this.claimRepository = claimRepository;
}

    @Transactional
    public LostFoundItem createItem(
            ItemRequest request,
            String campusId) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Item details are required.");
        }

        String title = validateText(
                request.title(), "Title", 120);

        String category = validateText(
                request.category(), "Category", 80);

        String description = validateText(
                request.description(), "Description", 2000);

        String location = validateText(
                request.location(), "Location", 150);

        if (request.type() == null) {
            throw new IllegalArgumentException(
                    "Item type is required.");
        }

        if (request.eventDate() == null) {
            throw new IllegalArgumentException(
                    "Event date is required.");
        }

        if (request.eventDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Event date cannot be in the future.");
        }

        if (campusId == null || campusId.isBlank()) {
            throw new IllegalArgumentException(
                    "Authenticated campus ID is required.");
        }

        LostFoundItem item = new LostFoundItem();

        item.setTitle(title);
        item.setType(request.type());
        item.setCategory(category);
        item.setDescription(description);
        item.setLocation(location);
        item.setEventDate(request.eventDate());

        // Backend-controlled values
        item.setStatus(ItemStatus.OPEN);
        item.setReportedBy(campusId);

        return itemRepository.save(item);
    }

    private String validateText(
            String value,
            String fieldName,
            int maxLength) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required.");
        }

        String cleaned = value.trim();

        if (cleaned.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " must not exceed "
                    + maxLength + " characters.");
        }

        return cleaned;
    }
    
@Transactional
public LostFoundItem updateItem(
        Long id,
        ItemRequest request,
        String campusId) {

    LostFoundItem item = itemRepository.findById(id)
        .orElseThrow(() -> new ItemNotFoundException(id));

    // Only the student who reported the item can edit it.
    if (!item.getReportedBy().equals(campusId)) {
        throw new org.springframework.security.access.AccessDeniedException(
                "You can update only your own reports.");
    }

    if (request == null) {
        throw new IllegalArgumentException(
                "Item details are required.");
    }

    // Validate the updated details.
    String title = validateText(
            request.title(), "Title", 120);

    String category = validateText(
            request.category(), "Category", 80);

    String description = validateText(
            request.description(), "Description", 2000);

    String location = validateText(
            request.location(), "Location", 150);

    if (request.type() == null) {
        throw new IllegalArgumentException(
                "Item type is required.");
    }

    if (request.eventDate() == null) {
        throw new IllegalArgumentException(
                "Event date is required.");
    }

    if (request.eventDate().isAfter(java.time.LocalDate.now())) {
        throw new IllegalArgumentException(
                "Event date cannot be in the future.");
    }

    // Update only the editable fields.
    item.setTitle(title);
    item.setType(request.type());
    item.setCategory(category);
    item.setDescription(description);
    item.setLocation(location);
    item.setEventDate(request.eventDate());

    // Preserve owner, status, image, and creation date.
    return itemRepository.save(item);
}


@Transactional
public void deleteItem(Long id, String campusId) {

    LostFoundItem item = itemRepository.findById(id)
        .orElseThrow(() -> new ItemNotFoundException(id));

    // Students can delete only their own reports.
    if (!item.getReportedBy().equals(campusId)) {
        throw new AccessDeniedException("You can delete only your own reports.");
    }

    // Do not delete an item that has claim records.
    if (claimRepository.existsByItem_Id(id)) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This item has existing claims and cannot be deleted.");
    }

    itemRepository.delete(item);
}


}
