
package com.lostlink.dto;

import java.time.LocalDate;

import com.lostlink.model.ItemType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record ItemRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title must not exceed 120 characters")
        String title,

        @NotNull(message = "Item type is required")
        ItemType type,

        @NotBlank(message = "Category is required")
        @Size(max = 80, message = "Category must not exceed 80 characters")
        String category,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotBlank(message = "Location is required")
        @Size(max = 150, message = "Location must not exceed 150 characters")
        String location,

        @NotNull(message = "Event date is required")
        @PastOrPresent(message = "Event date cannot be in the future")
        LocalDate eventDate

) {}
