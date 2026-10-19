
package com.lostlink.service;

import com.lostlink.model.CampusUser;
import com.lostlink.repository.UserRepository;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampusUserDetailsService
        implements UserDetailsService {

    private final UserRepository repository;

    public CampusUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String campusId)
            throws UsernameNotFoundException {

        CampusUser user = repository.findByCampusId(campusId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Invalid campus ID or password"));

        return new User(
                user.getCampusId(),
                user.getPasswordHash(),
                user.isEnabled(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()))
        );
    }
}
