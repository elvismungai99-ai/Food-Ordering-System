package com.foodordering.rider;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiderRepository
        extends JpaRepository<Rider, UUID> {

    Optional<Rider> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    boolean existsByEmail(String email);

    List<Rider> findByStatusAndOperationalStatusAndOnline(
            RiderStatus status,
            RiderOperationalStatus operationalStatus,
            boolean online
    );
}
