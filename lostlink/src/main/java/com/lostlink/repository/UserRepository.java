
package com.lostlink.repository;

import com.lostlink.model.CampusUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<CampusUser, Long> {

    Optional<CampusUser> findByCampusId(String campusId);

    boolean existsByCampusId(String campusId);
}
