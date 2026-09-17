package com.restaurant.hub.entity;

import com.restaurant.hub.enums.SpiceLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_items", indexes = {
        @Index(name = "idx_menu_items_restaurant", columnList = "restaurant_id"),
        @Index(name = "idx_menu_items_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountPrice;

    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean vegetarian = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private SpiceLevel spiceLevel = SpiceLevel.NONE;

    @ElementCollection
    @CollectionTable(name = "menu_item_ingredients", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "ingredient")
    @Builder.Default
    private List<String> ingredients = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "menu_item_allergens", joinColumns = @JoinColumn(name = "menu_item_id"))
    @Column(name = "allergen")
    @Builder.Default
    private List<String> allergens = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean available = true;

    private Integer preparationTimeMinutes;

    @Column(nullable = false)
    @Builder.Default
    private boolean featured = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean popular = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
