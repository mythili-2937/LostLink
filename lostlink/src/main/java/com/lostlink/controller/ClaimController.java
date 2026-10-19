
package com.lostlink.controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.lostlink.model.ClaimStatus;
import com.lostlink.model.ItemClaim;
import com.lostlink.model.ItemStatus;
import com.lostlink.model.ItemType;
import com.lostlink.model.LostFoundItem;
import com.lostlink.repository.ItemClaimRepository;
import com.lostlink.repository.LostFoundItemRepository;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/claims")
public class ClaimController {

    private final ItemClaimRepository claimRepository;
    private final LostFoundItemRepository itemRepository;

    public ClaimController(
            ItemClaimRepository claimRepository,
            LostFoundItemRepository itemRepository) {

        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
    }

    // Display the ownership verification form
    @GetMapping("/new/{itemId}")
    public String showClaimForm(
            @PathVariable Long itemId,
            Authentication authentication,
            Model model) {

        if (authentication == null) {
            return "redirect:/login";
        }

        LostFoundItem item = itemRepository
                .findById(itemId)
                .orElse(null);

        if (item == null) {
            return "redirect:/items";
        }

        if (item.getType() != ItemType.FOUND
                || item.getStatus() != ItemStatus.OPEN) {
            return "redirect:/items";
        }

        // A student cannot claim their own report
        if (authentication.getName()
                .equals(item.getReportedBy())) {
            return "redirect:/items";
        }

        model.addAttribute("item", item);
        return "claim-form";
    }

    // Process the submitted claim
    @PostMapping("/submit/{itemId}")
    public String submitClaim(
            @PathVariable Long itemId,
            @RequestParam String proofDescription,
            Authentication authentication,
            Model model) {

        if (authentication == null) {
            return "redirect:/login";
        }

        LostFoundItem item = itemRepository
                .findById(itemId)
                .orElse(null);

        if (item == null
                || item.getType() != ItemType.FOUND
                || item.getStatus() != ItemStatus.OPEN) {
            return "redirect:/items";
        }

        String studentId = authentication.getName();

        // Prevent claiming your own report
        if (studentId.equals(item.getReportedBy())) {
            return "redirect:/items";
        }

        // Validate ownership proof
        String proof = proofDescription == null
                ? ""
                : proofDescription.trim();

        if (proof.length() < 15 || proof.length() > 2000) {
            model.addAttribute("item", item);
            model.addAttribute(
                    "error",
                    "Ownership proof must contain 15 to 2000 characters.");
            model.addAttribute("proofDescription", proof);
            return "claim-form";
        }

        // Prevent duplicate active claims
        boolean alreadyClaimed =
                claimRepository
                        .existsByItem_IdAndClaimantIdAndStatusIn(
                                itemId,
                                studentId,
                                List.of(
                                        ClaimStatus.PENDING,
                                        ClaimStatus.APPROVED
                                ));

        if (alreadyClaimed) {
            model.addAttribute("item", item);
            model.addAttribute(
                    "error",
                    "You already have an active claim for this item.");
            return "claim-form";
        }

        ItemClaim claim = new ItemClaim();

        claim.setItem(item);
        claim.setClaimantId(studentId);
        claim.setProofDescription(proof);
        claim.setStatus(ClaimStatus.PENDING);

        claimRepository.save(claim);

        return "redirect:/claims/success";
    }

    // Confirmation page
    @GetMapping("/success")
    public String claimSuccess(Authentication authentication) {

        if (authentication == null) {
            return "redirect:/login";
        }

        return "claim-success";
    }
    
@GetMapping("/mine")
public String myClaims(
        Authentication authentication,
        Model model) {

    if (authentication == null) {
        return "redirect:/login";
    }

    String studentId = authentication.getName();

    // Retrieve claims submitted by this student only
    List<ItemClaim> claims =
            claimRepository
                    .findByClaimantIdOrderByCreatedAtDesc(
                            studentId);

    // Count claims by status
    long pendingCount = claims.stream()
            .filter(c -> c.getStatus() == ClaimStatus.PENDING)
            .count();

    long approvedCount = claims.stream()
            .filter(c -> c.getStatus() == ClaimStatus.APPROVED)
            .count();

    long rejectedCount = claims.stream()
            .filter(c -> c.getStatus() == ClaimStatus.REJECTED)
            .count();

    model.addAttribute("claims", claims);
    model.addAttribute("totalClaims", claims.size());
    model.addAttribute("pendingCount", pendingCount);
    model.addAttribute("approvedCount", approvedCount);
    model.addAttribute("rejectedCount", rejectedCount);

    return "my-claims";
}

@PostMapping("/cancel/{claimId}")
public String cancelClaim(
        @PathVariable Long claimId,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    if (authentication == null) {
        return "redirect:/login";
    }

    ItemClaim claim = claimRepository
            .findById(claimId)
            .orElse(null);

    if (claim == null) {
        redirectAttributes.addFlashAttribute(
                "error",
                "Claim request not found.");

        return "redirect:/claims/mine";
    }

    // Only the claimant can cancel their own request
    if (!claim.getClaimantId()
            .equals(authentication.getName())) {

        redirectAttributes.addFlashAttribute(
                "error",
                "You are not authorized to cancel this claim.");

        return "redirect:/claims/mine";
    }

    // Only PENDING claims can be cancelled
    if (claim.getStatus() != ClaimStatus.PENDING) {

        redirectAttributes.addFlashAttribute(
                "error",
                "Only pending claims can be cancelled.");

        return "redirect:/claims/mine";
    }

    claim.setStatus(ClaimStatus.CANCELLED);
    claimRepository.save(claim);

    redirectAttributes.addFlashAttribute(
            "success",
            "Your claim request has been cancelled.");

    return "redirect:/claims/mine";
}

@GetMapping("/incoming")
public String incomingClaims(
        Authentication authentication,
        Model model) {

    if (authentication == null) {
        return "redirect:/login";
    }

    String studentId = authentication.getName();

    List<ItemClaim> claims = claimRepository
            .findByItem_ReportedByOrderByCreatedAtDesc(
                    studentId);

    long pendingCount = claims.stream()
            .filter(c -> c.getStatus() == ClaimStatus.PENDING)
            .count();

    model.addAttribute("claims", claims);
    model.addAttribute("pendingCount", pendingCount);

    return "incoming-claims";
}

@PostMapping("/review/{claimId}")
@Transactional
public String reviewClaim(
        @PathVariable Long claimId,
        @RequestParam String decision,
        @RequestParam(required = false) String reviewNotes,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

    if (authentication == null) {
        return "redirect:/login";
    }

    ItemClaim claim = claimRepository
            .findById(claimId)
            .orElse(null);

    if (claim == null) {
        redirectAttributes.addFlashAttribute(
                "error", "Claim not found.");
        return "redirect:/claims/incoming";
    }

    // Only the person who reported the FOUND item
    // can review its claims.
    if (!claim.getItem().getReportedBy()
            .equals(authentication.getName())) {

        redirectAttributes.addFlashAttribute(
                "error", "You are not authorized to review this claim.");
        return "redirect:/claims/incoming";
    }

    // Only pending claims can be reviewed.
    if (claim.getStatus() != ClaimStatus.PENDING) {
        redirectAttributes.addFlashAttribute(
                "error", "This claim has already been processed.");
        return "redirect:/claims/incoming";
    }

    if (claim.getItem().getType() != ItemType.FOUND
            || claim.getItem().getStatus() != ItemStatus.OPEN) {
        redirectAttributes.addFlashAttribute(
                "error", "This item is no longer available for claims.");
        return "redirect:/claims/incoming";
    }

    if (!decision.equals("APPROVE")
            && !decision.equals("REJECT")) {
        redirectAttributes.addFlashAttribute(
                "error", "Invalid review decision.");
        return "redirect:/claims/incoming";
    }

    String notes = reviewNotes == null
            ? ""
            : reviewNotes.trim();

    if (notes.length() > 1000) {
        redirectAttributes.addFlashAttribute(
                "error", "Review notes cannot exceed 1000 characters.");
        return "redirect:/claims/incoming";
    }

    if (decision.equals("APPROVE")) {

        // Prevent multiple approved claims for one item.
        boolean alreadyApproved = claimRepository
                .existsByItem_IdAndStatus(
                        claim.getItem().getId(),
                        ClaimStatus.APPROVED);

        if (alreadyApproved) {
            redirectAttributes.addFlashAttribute(
                    "error", "Another claim is already approved for this item.");
            return "redirect:/claims/incoming";
        }

        claim.setStatus(ClaimStatus.APPROVED);

    } else {
        claim.setStatus(ClaimStatus.REJECTED);
    }

    claim.setReviewNotes(notes);
    claim.setReviewedAt(LocalDateTime.now());

    claimRepository.save(claim);

    redirectAttributes.addFlashAttribute(
            "success", "Claim reviewed successfully.");

    return "redirect:/claims/incoming";
}

}
