package com.cardcollection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionItem {
    private String id;
    private String userId;           // Which user owns this
    private String cardId;           // Reference to the card
    private Card card;               // The actual card data (embedded)
    
    // Collection-specific fields
    private Integer quantity;        // How many copies
    private String condition;        // Mint, Near Mint, Excellent, Good, Poor
    private Double purchasePrice;    // What they paid for it
    private String purchaseCurrency; // USD, EUR, etc.
    private Long dateAcquired;       // When they got it
    private String notes;            // Personal notes
    private Boolean isWishlist;      // Is this a wishlist item?
    
    // Metadata
    private Long createdAt;
    private Long updatedAt;
}