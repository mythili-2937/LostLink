package com.lostlink.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.lostlink.model.ItemStatus;
import com.lostlink.model.ItemType;
import com.lostlink.model.LostFoundItem;

public record ItemResponse(
        Long id,
        String title,
        ItemType type,
        String category,
        String description,
        String location,
        LocalDate eventDate,
        ItemStatus status,
        String reportedBy,
        LocalDateTime createdAt,
        String imageFilename
) {
    public static ItemResponse from(LostFoundItem item) {
        return new ItemResponse(
                item.getId(),
                item.getTitle(),
                item.getType(),
                item.getCategory(),
                item.getDescription(),
                item.getLocation(),
                item.getEventDate(),
                item.getStatus(),
                item.getReportedBy(),
                item.getCreatedAt(),
                item.getImageFilename()
        );
    }
}
