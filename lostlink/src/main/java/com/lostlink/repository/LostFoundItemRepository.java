
package com.lostlink.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lostlink.model.LostFoundItem;

import jakarta.persistence.LockModeType;

public interface LostFoundItemRepository
        extends JpaRepository<LostFoundItem, Long> {

    List<LostFoundItem> findAllByOrderByCreatedAtDesc();

    List<LostFoundItem>
    findByReportedByOrderByCreatedAtDesc(String reportedBy);

    @Query("""
        SELECT i FROM LostFoundItem i
        WHERE
            LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(i.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(i.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY i.createdAt DESC
    """)
    List<LostFoundItem> searchItems(
            @Param("keyword") String keyword
    );
    
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT i FROM LostFoundItem i WHERE i.id = :id")
Optional<LostFoundItem> findByIdForUpdate(
        @Param("id") Long id);

}
