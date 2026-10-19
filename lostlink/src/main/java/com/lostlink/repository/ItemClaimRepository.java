
package com.lostlink.repository;
import org.springframework.data.jpa.repository.EntityGraph;
import com.lostlink.model.ItemClaim;
import com.lostlink.model.ClaimStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemClaimRepository
        extends JpaRepository<ItemClaim, Long> {

    // Claims submitted by a student
    @EntityGraph(attributePaths = "item")
List<ItemClaim> findByClaimantIdOrderByCreatedAtDesc(
        String claimantId);

    // Claims for a particular FOUND item
    List<ItemClaim> findByItem_IdOrderByCreatedAtDesc(
            Long itemId);

    // Prevent duplicate active claims
    boolean existsByItem_IdAndClaimantIdAndStatusIn(
            Long itemId,
            String claimantId,
            List<ClaimStatus> statuses);
            
@EntityGraph(attributePaths = "item")
List<ItemClaim> findByItem_ReportedByOrderByCreatedAtDesc(
        String reportedBy);
    
boolean existsByItem_IdAndStatus(
        Long itemId,
        ClaimStatus status);
boolean existsByItem_Id(Long itemId);
}

