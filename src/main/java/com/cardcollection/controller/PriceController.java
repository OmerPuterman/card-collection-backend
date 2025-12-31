package com.cardcollection.controller;

import com.cardcollection.model.PriceHistory;
import com.cardcollection.service.PriceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cardcollection.model.Card;
import com.cardcollection.service.CardService;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "*")
public class PriceController {

    private final PriceService priceService;
    private final CardService cardService;  // Add this line

    public PriceController(PriceService priceService, CardService cardService) {  // Add cardService parameter
        this.priceService = priceService;
        this.cardService = cardService;  // Add this line
    }

    /**
     * POST /api/prices
     * Add a price point for a card
     */
    @PostMapping
    public ResponseEntity<PriceHistory> addPricePoint(@RequestBody PriceHistory priceHistory) {
        try {
            PriceHistory created = priceService.addPricePoint(priceHistory);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/prices/card/{cardId}
     * Get all price history for a card
     */
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<PriceHistory>> getCardPriceHistory(@PathVariable String cardId) {
        try {
            List<PriceHistory> history = priceService.getCardPriceHistory(cardId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/prices/card/{cardId}/range
     * Get price history within a time range
     */
    @GetMapping("/card/{cardId}/range")
    public ResponseEntity<List<PriceHistory>> getCardPriceHistoryRange(
            @PathVariable String cardId,
            @RequestParam Long startTime,
            @RequestParam Long endTime) {
        try {
            List<PriceHistory> history = priceService.getCardPriceHistory(cardId, startTime, endTime);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/prices/card/{cardId}/latest
     * Get latest price for a card
     */
    @GetMapping("/card/{cardId}/latest")
    public ResponseEntity<PriceHistory> getLatestPrice(@PathVariable String cardId) {
        try {
            PriceHistory latest = priceService.getLatestPrice(cardId);
            if (latest != null) {
                return ResponseEntity.ok(latest);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/prices/card/{cardId}/change
     * Get price change over X days
     */
    @GetMapping("/card/{cardId}/change")
    public ResponseEntity<PriceService.PriceChange> getPriceChange(
            @PathVariable String cardId,
            @RequestParam(defaultValue = "7") Long daysAgo) {
        try {
            PriceService.PriceChange priceChange = priceService.getPriceChange(cardId, daysAgo);
            if (priceChange != null) {
                return ResponseEntity.ok(priceChange);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/prices/card/{cardId}/test/add-price
     * Test endpoint - add a price point (for testing)
     */
    @GetMapping("/card/{cardId}/test/add-price")
    public ResponseEntity<PriceHistory> testAddPrice(
            @PathVariable String cardId,
            @RequestParam Double price,
            @RequestParam(defaultValue = "Near Mint") String condition,
            @RequestParam(defaultValue = "manual") String source) {
        try {
            PriceHistory priceHistory = new PriceHistory();
            priceHistory.setCardId(cardId);
            priceHistory.setPrice(price);
            priceHistory.setCondition(condition);
            priceHistory.setSource(source);
            priceHistory.setCurrency("USD");
            
            PriceHistory created = priceService.addPricePoint(priceHistory);
            
            // Also update the card's current price
            priceService.updateCardCurrentPrice(cardId);
            
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/prices/card/{cardId}/update-current
     * Update card's current price from latest price history
     */
    @PostMapping("/card/{cardId}/update-current")
    public ResponseEntity<Void> updateCardCurrentPrice(@PathVariable String cardId) {
        try {
            priceService.updateCardCurrentPrice(cardId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/prices/card/{cardId}/test/add-history
     * Test endpoint - add 30 days of sample price history
     */
    @GetMapping("/card/{cardId}/test/add-history")
    public ResponseEntity<String> testAddPriceHistory(@PathVariable String cardId) {
        try {
            long now = System.currentTimeMillis();
            long oneDay = 24 * 60 * 60 * 1000L;
            
            double basePrice = 40.0;
            
            for (int i = 30; i >= 0; i--) {
                PriceHistory priceHistory = new PriceHistory();
                priceHistory.setCardId(cardId);
                
                // Simulate price fluctuation
                double fluctuation = (Math.random() - 0.5) * 5; // +/- $2.50
                double trendIncrease = (30 - i) * 0.3; // Gradual increase
                
                priceHistory.setPrice(basePrice + fluctuation + trendIncrease);
                priceHistory.setCondition("Near Mint");
                priceHistory.setSource("manual");
                priceHistory.setCurrency("USD");
                priceHistory.setTimestamp(now - (i * oneDay));
                
                priceService.addPricePoint(priceHistory);
            }
            
            priceService.updateCardCurrentPrice(cardId);
            
            return ResponseEntity.ok("✅ Added 31 price points for card " + cardId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * GET /api/prices/test/add-history-all
     * Test endpoint - add 30 days of price history for ALL cards
     */
    @GetMapping("/test/add-history-all")
    public ResponseEntity<String> testAddPriceHistoryForAllCards() {
        try {
            // Get all cards
            List<Card> allCards = cardService.getAllCards();
            
            if (allCards.isEmpty()) {
                return ResponseEntity.ok("No cards found");
            }
            
            long now = System.currentTimeMillis();
            long oneDay = 24 * 60 * 60 * 1000L;
            int totalPointsAdded = 0;
            
            // For each card, add 31 price points
            for (Card card : allCards) {
                String cardId = card.getId();
                
                // Use current price as base, or default to $20-$100 random
                double basePrice = card.getCurrentPrice() != null ? 
                    card.getCurrentPrice() : 
                    (20.0 + Math.random() * 80.0);
                
                // Add 31 days of price history
                for (int i = 30; i >= 0; i--) {
                    PriceHistory priceHistory = new PriceHistory();
                    priceHistory.setCardId(cardId);
                    
                    // Simulate price fluctuation
                    double fluctuation = (Math.random() - 0.5) * (basePrice * 0.2); // +/- 20%
                    double trendIncrease = (30 - i) * (basePrice * 0.01); // Gradual 1% increase per day
                    
                    priceHistory.setPrice(basePrice + fluctuation + trendIncrease);
                    priceHistory.setCondition("Near Mint");
                    priceHistory.setSource("simulated");
                    priceHistory.setCurrency("USD");
                    priceHistory.setTimestamp(now - (i * oneDay));
                    
                    priceService.addPricePoint(priceHistory);
                    totalPointsAdded++;
                }
                
                // Update card's current price
                priceService.updateCardCurrentPrice(cardId);
            }
            
            return ResponseEntity.ok(
                "✅ Added price history for " + allCards.size() + " cards (" + 
                totalPointsAdded + " total price points)"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
}