
package com.lostlink.service;

import com.lostlink.model.LostFoundItem;
import com.lostlink.model.ItemStatus;
import com.lostlink.model.ItemType;
import com.lostlink.repository.LostFoundItemRepository;

import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemMatchingService {

    private final LostFoundItemRepository itemRepository;

    public ItemMatchingService(
            LostFoundItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // Represents one possible match.
   public record MatchResult(
        LostFoundItem item,
        int score,
        String confidence,
        List<String> reasons
) {}

    public List<MatchResult> findMatches(Long itemId) {

        LostFoundItem source = itemRepository
                .findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Report not found"));

        // Only open reports can be matched.
        if (source.getStatus() != ItemStatus.OPEN) {
            return List.of();
        }

        // A LOST report is matched against FOUND reports
        // and vice versa.
        ItemType oppositeType =
                source.getType() == ItemType.LOST
                        ? ItemType.FOUND
                        : ItemType.LOST;

        return itemRepository
                .findAll()
                .stream()

                // Compare only opposite report types.
                .filter(item ->
                        item.getType() == oppositeType)

                // Ignore resolved or claimed reports.
                .filter(item ->
                        item.getStatus() == ItemStatus.OPEN)

                // Don't match a report with itself.
                .filter(item ->
                        !item.getId().equals(source.getId()))

                .map(item -> {
    int score = calculateScore(source, item);

    return new MatchResult(
            item,
            score,
            getConfidence(score),
            getMatchReasons(source, item)
    );
})

                // Minimum score to be considered a match.
                .filter(result -> result.score() >= 40)

                // Highest score first.
                .sorted(Comparator.comparingInt(
                        MatchResult::score).reversed())

                // Show the top five.
                .limit(5)

                .toList();
    }

    private int calculateScore(
            LostFoundItem first,
            LostFoundItem second) {

        int score = 0;

        // 1. Category similarity - 30 points
        if (first.getCategory() != null
                && second.getCategory() != null
                && first.getCategory().equalsIgnoreCase(
                        second.getCategory())) {

            score += 30;
        }

        // 2. Title and description similarity - 40 points
        String firstText =
                safe(first.getTitle()) + " "
                + safe(first.getDescription());

        String secondText =
                safe(second.getTitle()) + " "
                + safe(second.getDescription());

        double similarity =
                wordSimilarity(firstText, secondText);

        score += (int) Math.round(similarity * 40);

        // 3. Location similarity - 20 points
        double locationSimilarity =
                wordSimilarity(
                        safe(first.getLocation()),
                        safe(second.getLocation()));

        score += (int) Math.round(
                locationSimilarity * 20);

        // 4. Date similarity - 10 points
        if (first.getEventDate() != null
                && second.getEventDate() != null) {

            long days = Math.abs(
                    ChronoUnit.DAYS.between(
                            first.getEventDate(),
                            second.getEventDate()));

            if (days <= 1) {
                score += 10;
            } else if (days <= 3) {
                score += 7;
            } else if (days <= 7) {
                score += 4;
            }
        }

        return Math.min(score, 100);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // Basic word overlap similarity.
    private double wordSimilarity(
            String first,
            String second) {

        Set<String> firstWords = tokenize(first);
        Set<String> secondWords = tokenize(second);

        if (firstWords.isEmpty()
                || secondWords.isEmpty()) {
            return 0;
        }

        Set<String> common = new HashSet<>(firstWords);
        common.retainAll(secondWords);

        Set<String> all = new HashSet<>(firstWords);
        all.addAll(secondWords);

        return (double) common.size() / all.size();
    }

    private Set<String> tokenize(String text) {

        return Arrays.stream(
                        text.toLowerCase(Locale.ROOT)
                                .split("[^a-z0-9]+"))

                .filter(word -> word.length() > 2)

                .collect(Collectors.toSet());
    }
    
private String getConfidence(int score) {

    if (score >= 75) {
        return "HIGH";
    }

    if (score >= 55) {
        return "MEDIUM";
    }

    return "LOW";
}

private List<String> getMatchReasons(
        LostFoundItem first,
        LostFoundItem second) {

    List<String> reasons = new ArrayList<>();

    // Category
    if (first.getCategory() != null
            && second.getCategory() != null
            && first.getCategory().equalsIgnoreCase(
                    second.getCategory())) {

        reasons.add("Same item category");
    }

    // Title and description
    String firstText =
            safe(first.getTitle()) + " "
            + safe(first.getDescription());

    String secondText =
            safe(second.getTitle()) + " "
            + safe(second.getDescription());

    if (wordSimilarity(firstText, secondText) >= 0.20) {
        reasons.add("Similar title or description");
    }

    // Location
    if (wordSimilarity(
            safe(first.getLocation()),
            safe(second.getLocation())) >= 0.30) {

        reasons.add("Similar reported location");
    }

    // Date
    if (first.getEventDate() != null
            && second.getEventDate() != null) {

        long days = Math.abs(
                ChronoUnit.DAYS.between(
                        first.getEventDate(),
                        second.getEventDate()));

        if (days <= 3) {
            reasons.add("Reported dates are close together");
        }
    }

    if (reasons.isEmpty()) {
        reasons.add("Some report details are similar");
    }

    return reasons;
}

}
