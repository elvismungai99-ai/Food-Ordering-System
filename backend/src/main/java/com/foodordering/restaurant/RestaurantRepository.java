package com.foodordering.restaurant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    boolean existsByOwnerId(UUID ownerId);

    Optional<Restaurant> findByOwnerId(UUID ownerId);

    @Query("""
            SELECT r
            FROM Restaurant r
            WHERE (
                :search IS NULL
                OR :search = ''
                OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(r.address) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (
                :category IS NULL
                OR :category = ''
                OR LOWER(r.category) = LOWER(:category)
            )
            """)
    List<Restaurant> searchRestaurants(
            @Param("search") String search,
            @Param("category") String category
    );

    @Query("""
            SELECT DISTINCT r.category
            FROM Restaurant r
            WHERE r.category IS NOT NULL
            AND r.category <> ''
            ORDER BY r.category
            """)
    List<String> findDistinctCategories();
}