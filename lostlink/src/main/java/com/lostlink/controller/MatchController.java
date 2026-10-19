
package com.lostlink.controller;

import com.lostlink.model.LostFoundItem;
import com.lostlink.repository.LostFoundItemRepository;
import com.lostlink.service.ItemMatchingService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MatchController {

    private final ItemMatchingService matchingService;
    private final LostFoundItemRepository itemRepository;

    public MatchController(
            ItemMatchingService matchingService,
            LostFoundItemRepository itemRepository) {

        this.matchingService = matchingService;
        this.itemRepository = itemRepository;
    }

    @GetMapping("/items/{id}/matches")
    public String showMatches(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        if (authentication == null) {
            return "redirect:/login";
        }

        LostFoundItem source = itemRepository
                .findById(id)
                .orElse(null);

        if (source == null) {
            return "redirect:/items/mine";
        }

        // Only the report owner can access
        // its matching results.
        if (!source.getReportedBy().equals(
                authentication.getName())) {

            return "redirect:/items/mine";
        }

        model.addAttribute("sourceItem", source);

        model.addAttribute(
                "matches",
                matchingService.findMatches(id));

        return "item-matches";
    }
}
