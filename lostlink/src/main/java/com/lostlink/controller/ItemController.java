
package com.lostlink.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lostlink.model.ItemStatus;
import com.lostlink.model.ItemType;
import com.lostlink.model.LostFoundItem;
import com.lostlink.repository.LostFoundItemRepository;

import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.Optional;

import com.lostlink.service.ItemImageService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class ItemController {

    private final LostFoundItemRepository itemRepository;
private final ItemImageService imageService;

public ItemController(
        LostFoundItemRepository itemRepository,
        ItemImageService imageService) {

    this.itemRepository = itemRepository;
    this.imageService = imageService;
}

    @GetMapping("/items/report")
    public String showReportForm(Model model) {
        model.addAttribute("types", ItemType.values());
        return "report-item";
    }


@PostMapping("/items/report")
public String submitReport(
        @RequestParam String title,
        @RequestParam ItemType type,
        @RequestParam String category,
        @RequestParam String description,
        @RequestParam String location,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate eventDate,
        @RequestParam(value = "image", required = false)
        MultipartFile image,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    if (authentication == null) {
        return "redirect:/login";
    }
    
// Remember form values if validation or upload fails.
redirectAttributes.addFlashAttribute("oldTitle", title);
redirectAttributes.addFlashAttribute("oldType", type.name());
redirectAttributes.addFlashAttribute("oldCategory", category);
redirectAttributes.addFlashAttribute("oldDescription", description);
redirectAttributes.addFlashAttribute("oldLocation", location);
redirectAttributes.addFlashAttribute(
        "oldEventDate", eventDate.toString()
);

    if (title.isBlank() || title.length() > 120
            || category.isBlank() || category.length() > 80
            || description.isBlank() || description.length() > 2000
            || location.isBlank() || location.length() > 150) {

        redirectAttributes.addFlashAttribute(
                "error", "Please enter valid item details."
        );
        return "redirect:/items/report";
    }

    if (eventDate.isAfter(LocalDate.now())) {
        redirectAttributes.addFlashAttribute(
                "error", "Date cannot be in the future."
        );
        return "redirect:/items/report";
    }

    String filename = null;

    try {
        filename = imageService.saveImage(image);

        LostFoundItem item = new LostFoundItem();

        item.setTitle(title.trim());
        item.setType(type);
        item.setCategory(category.trim());
        item.setDescription(description.trim());
        item.setLocation(location.trim());
        item.setEventDate(eventDate);
        item.setStatus(ItemStatus.OPEN);
        item.setReportedBy(authentication.getName());
        item.setImageFilename(filename);

        itemRepository.save(item);

        redirectAttributes.addFlashAttribute(
                "success", "Item reported successfully!"
        );

        return "redirect:/items/report";

    } catch (IllegalArgumentException e) {

        redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
        );

    } catch (IOException e) {

        redirectAttributes.addFlashAttribute(
                "error", "Could not upload image. Please try again."
        );
    } catch (RuntimeException e) {

        redirectAttributes.addFlashAttribute(
                "error", "Could not save the report. Please try again."
        );
    }

    if (filename != null) {
        try {
            imageService.deleteImage(filename);
        } catch (IOException ignored) {
            // Log cleanup failures in production.
        }
    }

    return "redirect:/items/report";
}


@GetMapping("/items")
public String browseItems(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String status,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fromDate,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate toDate,
        Model model) {

    // 1. Search by keyword
    List<LostFoundItem> items;

    String searchKeyword =
            keyword == null ? "" : keyword.trim();

    if (!searchKeyword.isEmpty()) {
        items = itemRepository.searchItems(searchKeyword);
    } else {
        items = itemRepository.findAllByOrderByCreatedAtDesc();
    }

    // 2. Filter by LOST or FOUND
    String selectedType = "ALL";

    if (type != null && !type.isBlank()) {
        try {
            ItemType itemType =
                    ItemType.valueOf(type.toUpperCase());

            items = items.stream()
                    .filter(item -> item.getType() == itemType)
                    .toList();

            selectedType = itemType.name();

        } catch (IllegalArgumentException e) {
            selectedType = "ALL";
        }
    }

    // 3. Filter by category
    String selectedCategory =
            category == null ? "" : category.trim();

    if (!selectedCategory.isEmpty()) {
        items = items.stream()
                .filter(item ->
                        item.getCategory().equalsIgnoreCase(
                                selectedCategory))
                .toList();
    }

    // 4. Filter by status
    String selectedStatus =
            status == null ? "" : status.trim();

    if (!selectedStatus.isEmpty()) {
        try {
            ItemStatus itemStatus =
                    ItemStatus.valueOf(
                            selectedStatus.toUpperCase());

            items = items.stream()
                    .filter(item ->
                            item.getStatus() == itemStatus)
                    .toList();

            selectedStatus = itemStatus.name();

        } catch (IllegalArgumentException e) {
            selectedStatus = "";
        }
    }

    // 5. Filter from date
    if (fromDate != null) {
        items = items.stream()
                .filter(item ->
                        !item.getEventDate().isBefore(fromDate))
                .toList();
    }

    // 6. Filter to date
    if (toDate != null) {
        items = items.stream()
                .filter(item ->
                        !item.getEventDate().isAfter(toDate))
                .toList();
    }

    // 7. Send results to Thymeleaf
    model.addAttribute("items", items);
    model.addAttribute("keyword", searchKeyword);
    model.addAttribute("selectedType", selectedType);
    model.addAttribute("selectedCategory", selectedCategory);
    model.addAttribute("selectedStatus", selectedStatus);
    model.addAttribute("fromDate", fromDate);
    model.addAttribute("toDate", toDate);
    model.addAttribute("resultCount", items.size());

    return "items";
}


@GetMapping("/items/mine")
public String myReports(
        Authentication authentication,
        Model model) {

    if (authentication == null) {
        return "redirect:/login";
    }

    model.addAttribute(
        "items",
        itemRepository.findByReportedByOrderByCreatedAtDesc(
            authentication.getName()
        )
    );

    return "my-reports";
}

@GetMapping("/items/images/{filename}")
@ResponseBody
public ResponseEntity<Resource> viewImage(
        @PathVariable String filename) throws IOException {

    Path path;

    try {
        path = imageService.getImagePath(filename);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.notFound().build();
    }

    if (!Files.isRegularFile(path)) {
        return ResponseEntity.notFound().build();
    }

    Resource resource = new UrlResource(path.toUri());

    String contentType = Files.probeContentType(path);

    if (contentType == null) {
        contentType = "application/octet-stream";
    }

    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource);
}

@GetMapping("/items/{id}/edit")
public String showEditForm(
        @PathVariable Long id,
        Authentication authentication,
        Model model,
        RedirectAttributes redirectAttributes) {

    Optional<LostFoundItem> optionalItem =
            itemRepository.findById(id);

    if (optionalItem.isEmpty() ||
            !optionalItem.get().getReportedBy()
                    .equals(authentication.getName())) {

        redirectAttributes.addFlashAttribute(
                "error", "Report not found or access denied."
        );
        return "redirect:/items/mine";
    }

    model.addAttribute("item", optionalItem.get());

    return "edit-item";
}

@PostMapping("/items/{id}/resolve")
public String resolveReport(
        @PathVariable Long id,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    Optional<LostFoundItem> optionalItem =
            itemRepository.findById(id);

    if (optionalItem.isEmpty() ||
            !optionalItem.get().getReportedBy()
                    .equals(authentication.getName())) {

        redirectAttributes.addFlashAttribute(
                "error", "You cannot modify this report."
        );
        return "redirect:/items/mine";
    }

    LostFoundItem item = optionalItem.get();

    item.setStatus(ItemStatus.RESOLVED);
    itemRepository.save(item);

    redirectAttributes.addFlashAttribute(
            "success", "Report marked as resolved."
    );

    return "redirect:/items/mine";
}

@PostMapping("/items/{id}/delete")
public String deleteReport(
        @PathVariable Long id,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    Optional<LostFoundItem> optionalItem =
            itemRepository.findById(id);

    if (optionalItem.isEmpty() ||
            !optionalItem.get().getReportedBy()
                    .equals(authentication.getName())) {

        redirectAttributes.addFlashAttribute(
                "error", "You cannot delete this report."
        );
        return "redirect:/items/mine";
    }

    LostFoundItem item = optionalItem.get();

    itemRepository.delete(item);

    // Remove the associated local image after DB deletion.
    if (item.getImageFilename() != null) {
        try {
            imageService.deleteImage(item.getImageFilename());
        } catch (IOException e) {
            // Report is deleted; orphaned image can be cleaned later.
        }
    }

    redirectAttributes.addFlashAttribute(
            "success", "Report deleted successfully."
    );

    return "redirect:/items/mine";
}

@PostMapping("/items/{id}/edit")
public String updateReport(
        @PathVariable Long id,
        @RequestParam String title,
        @RequestParam ItemType type,
        @RequestParam String category,
        @RequestParam String description,
        @RequestParam String location,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate eventDate,
        @RequestParam(value = "image", required = false)
        MultipartFile image,
        @RequestParam(value = "removeImage", defaultValue = "false")
        boolean removeImage,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    if (authentication == null) {
        return "redirect:/login";
    }

    Optional<LostFoundItem> optionalItem =
            itemRepository.findById(id);

    // Only the report owner can modify it.
    if (optionalItem.isEmpty()
            || !optionalItem.get().getReportedBy()
                    .equals(authentication.getName())) {

        redirectAttributes.addFlashAttribute(
                "error", "You cannot edit this report."
        );

        return "redirect:/items/mine";
    }

    LostFoundItem item = optionalItem.get();

    // Validate entered details.
    if (title.isBlank() || title.length() > 120
            || category.isBlank() || category.length() > 80
            || description.isBlank() || description.length() > 2000
            || location.isBlank() || location.length() > 150
            || eventDate.isAfter(LocalDate.now())) {

        redirectAttributes.addFlashAttribute(
                "error", "Please enter valid report details."
        );

        return "redirect:/items/" + id + "/edit";
    }

    // Do not allow change and remove at the same time.
    if (removeImage && image != null && !image.isEmpty()) {

        redirectAttributes.addFlashAttribute(
                "error",
                "Choose either Change Photo or Remove Photo."
        );

        return "redirect:/items/" + id + "/edit";
    }

    String oldFilename = item.getImageFilename();
    String newFilename = null;

    try {
        // Save the new image, if selected.
        if (image != null && !image.isEmpty()) {
            newFilename = imageService.saveImage(image);
        }

        // Update the report details.
        item.setTitle(title.trim());
        item.setType(type);
        item.setCategory(category.trim());
        item.setDescription(description.trim());
        item.setLocation(location.trim());
        item.setEventDate(eventDate);

        // Choose which image filename to store.
        if (removeImage) {
            item.setImageFilename(null);
        } else if (newFilename != null) {
            item.setImageFilename(newFilename);
        }

        // Save database changes first.
        itemRepository.save(item);

    } catch (IllegalArgumentException e) {

        // Clean up newly uploaded image on failure.
        if (newFilename != null) {
            try {
                imageService.deleteImage(newFilename);
            } catch (IOException ignored) {
            }
        }

        redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
        );

        return "redirect:/items/" + id + "/edit";

    } catch (IOException | RuntimeException e) {

        if (newFilename != null) {
            try {
                imageService.deleteImage(newFilename);
            } catch (IOException ignored) {
            }
        }

        redirectAttributes.addFlashAttribute(
                "error",
                "Could not update the report. Please try again."
        );

        return "redirect:/items/" + id + "/edit";
    }

    // Database update succeeded.
    // Delete the previous photo only after saving the report.
    if ((removeImage || newFilename != null)
            && oldFilename != null) {

        try {
            imageService.deleteImage(oldFilename);
        } catch (IOException e) {
            // Old unused image can be cleaned up later.
        }
    }

    redirectAttributes.addFlashAttribute(
            "success", "Report updated successfully!"
    );

    return "redirect:/items/mine";
}

}
