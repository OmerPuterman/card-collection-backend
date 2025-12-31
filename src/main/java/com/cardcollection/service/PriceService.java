package com.cardcollection.service;

import com.cardcollection.model.Card;
import com.cardcollection.model.PriceHistory;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class PriceService {

    private final Firestore firestore;
    private final CardService cardService;
    private static final String COLLECTION_NAME = "prices";

    public PriceService(Firestore firestore, CardService cardService) {
        this.firestore = firestore;
        this.cardService = cardService;
    }

    /**
     * Add a price point for a card
     */
    public PriceHistory addPricePoint(PriceHistory priceHistory) 
            throws ExecutionException, InterruptedException {
        
        if (priceHistory.getId() == null || priceHistory.getId().isEmpty()) {
            priceHistory.setId(UUID.randomUUID().toString());
        }
        
        long now = System.currentTimeMillis();
        priceHistory.setCreatedAt(now);
        
        if (priceHistory.getTimestamp() == null) {
            priceHistory.setTimestamp(now);
        }
        
        if (priceHistory.getCurrency() == null) {
            priceHistory.setCurrency("USD");
        }
        
        if (priceHistory.getCondition() == null) {
            priceHistory.setCondition("Near Mint");
        }
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(priceHistory.getId());
        docRef.set(priceHistory).get();
        
        System.out.println("✅ Price point added for card: " + priceHistory.getCardId() + " - $" + priceHistory.getPrice());
        return priceHistory;
    }

    /**
     * Get price history for a specific card
     */
    public List<PriceHistory> getCardPriceHistory(String cardId) 
            throws ExecutionException, InterruptedException {
        
        QuerySnapshot querySnapshot = firestore
            .collection(COLLECTION_NAME)
            .whereEqualTo("cardId", cardId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .get();

        return querySnapshot.getDocuments().stream()
            .map(doc -> doc.toObject(PriceHistory.class))
            .collect(Collectors.toList());
    }

    /**
     * Get price history for a card within a time range
     */
    public List<PriceHistory> getCardPriceHistory(String cardId, Long startTime, Long endTime) 
            throws ExecutionException, InterruptedException {
        
        Query query = firestore
            .collection(COLLECTION_NAME)
            .whereEqualTo("cardId", cardId)
            .whereGreaterThanOrEqualTo("timestamp", startTime)
            .whereLessThanOrEqualTo("timestamp", endTime)
            .orderBy("timestamp", Query.Direction.ASCENDING);

        QuerySnapshot querySnapshot = query.get().get();

        return querySnapshot.getDocuments().stream()
            .map(doc -> doc.toObject(PriceHistory.class))
            .collect(Collectors.toList());
    }

    /**
     * Get latest price for a card
     */
    public PriceHistory getLatestPrice(String cardId) 
            throws ExecutionException, InterruptedException {
        
        QuerySnapshot querySnapshot = firestore
            .collection(COLLECTION_NAME)
            .whereEqualTo("cardId", cardId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .get();

        if (!querySnapshot.getDocuments().isEmpty()) {
            return querySnapshot.getDocuments().get(0).toObject(PriceHistory.class);
        }
        return null;
    }

    /**
     * Calculate price change percentage
     */
    public PriceChange getPriceChange(String cardId, Long daysAgo) 
            throws ExecutionException, InterruptedException {
        
        long now = System.currentTimeMillis();
        long millisecondsAgo = daysAgo * 24 * 60 * 60 * 1000;
        long pastTime = now - millisecondsAgo;
        
        // Get current price
        PriceHistory currentPrice = getLatestPrice(cardId);
        if (currentPrice == null) {
            return null;
        }
        
        // Get price from X days ago
        QuerySnapshot querySnapshot = firestore
            .collection(COLLECTION_NAME)
            .whereEqualTo("cardId", cardId)
            .whereLessThanOrEqualTo("timestamp", pastTime)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .get();
        
        if (querySnapshot.getDocuments().isEmpty()) {
            return new PriceChange(currentPrice.getPrice(), currentPrice.getPrice(), 0.0, 0.0);
        }
        
        PriceHistory oldPrice = querySnapshot.getDocuments().get(0).toObject(PriceHistory.class);
        
        double change = currentPrice.getPrice() - oldPrice.getPrice();
        double changePercent = (change / oldPrice.getPrice()) * 100;
        
        return new PriceChange(
            oldPrice.getPrice(),
            currentPrice.getPrice(),
            change,
            changePercent
        );
    }

    /**
     * Update card's current price based on latest price history
     */
    public void updateCardCurrentPrice(String cardId) 
            throws ExecutionException, InterruptedException {
        
        PriceHistory latestPrice = getLatestPrice(cardId);
        if (latestPrice != null) {
            Card card = cardService.getCardById(cardId);
            if (card != null) {
                card.setCurrentPrice(latestPrice.getPrice());
                card.setUpdatedAt(System.currentTimeMillis());
                
                DocumentReference docRef = firestore.collection("cards").document(cardId);
                docRef.set(card).get();
                
                System.out.println("✅ Updated current price for " + card.getName() + " to $" + latestPrice.getPrice());
            }
        }
    }

    /**
     * Inner class for price change data
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PriceChange {
        private double oldPrice;
        private double newPrice;
        private double change;
        private double changePercent;
    }
}