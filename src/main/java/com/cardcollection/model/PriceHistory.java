package com.cardcollection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {
    private String id;
    private String cardId;           // Which card
    private Double price;            // Price at this point in time
    private String currency;         // USD, EUR, etc.
    private String condition;        // Mint, Near Mint, etc.
    private String source;           // manual, tcgplayer, ebay, etc.
    private Long timestamp;          // When this price was recorded
    private String notes;            // Optional notes
    
    // Metadata
    private Long createdAt;
}