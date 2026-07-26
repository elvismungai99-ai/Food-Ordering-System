package com.foodordering.restaurant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository
        extends JpaRepository<Restaurant, UUID> {

    /*
     * Check whether a restaurant already exists
     * for a specific owner.
     */
    boolean existsByOwnerId(
            UUID ownerId
    );

    /*
     * Find the restaurant belonging to
     * a specific owner.
     */
    Optional<Restaurant> findByOwnerId(
            UUID ownerId
    );

    /*
     * Search restaurants using an optional search term
     * and an optional category.
     *
     * The search checks:
     * - restaurant name
     * - description
     * - address
     *
     * Both parameters may be null or blank.
     */
    @Query("""
            SELECT r
            FROM Restaurant r
            WHERE
                (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(r.name)
                        LIKE LOWER(
                            CONCAT(
                                '%',
                                :search,
                                '%'
                            )
                        )
                    OR LOWER(
                        COALESCE(
                            r.description,
                            ''
                        )
                    )
                        LIKE LOWER(
                            CONCAT(
                                '%',
                                :search,
                                '%'
                            )
                        )
                    OR LOWER(
                        COALESCE(
                            r.address,
                            ''
                        )
                    )
                        LIKE LOWER(
                            CONCAT(
                                '%',
                                :search,
                                '%'
                            )
                        )
                )
                AND
                (
                    :category IS NULL
                    OR :category = ''
                    OR LOWER(
                        COALESCE(
                            r.category,
                            ''
                        )
                    ) = LOWER(:category)
                )
            ORDER BY r.name ASC
            """)
    List<Restaurant> searchRestaurants(
            @Param("search")
            String search,

            @Param("category")
            String category
    );

    /*
     * Return unique restaurant categories
     * for the customer category filter.
     */
    @Query("""
            SELECT DISTINCT r.category
            FROM Restaurant r
            WHERE r.category IS NOT NULL
              AND TRIM(r.category) <> ''
            ORDER BY r.category ASC
            """)
    List<String> findDistinctCategories();
}