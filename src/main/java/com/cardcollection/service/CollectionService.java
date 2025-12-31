package com.cardcollection.service;

import com.cardcollection.model.Card;
import com.cardcollection.model.CollectionItem;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CollectionService {

    private final Firestore firestore;
    private final CardService cardService;
    private static final String COLLECTION_NAME = "collections";

    public CollectionService(Firestore firestore, CardService cardService) {
        this.firestore = firestore;
        this.cardService = cardService;
    }

    public CollectionItem addToCollection(String userId, CollectionItem item) 
            throws ExecutionException, InterruptedException {
        
        if (item.getId() == null || item.getId().isEmpty()) {
            item.setId(UUID.randomUUID().toString());
        }
        
        item.setUserId(userId);
        
        long now = System.currentTimeMillis();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        
        if (item.getDateAcquired() == null) {
            item.setDateAcquired(now);
        }
        
        Card card = cardService.getCardById(item.getCardId());
        if (card == null) {
            throw new IllegalArgumentException("Card with ID " + item.getCardId() + " not found!");
        }
        item.setCard(card);
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(item.getId());
        docRef.set(item).get();
        
        System.out.println("✅ Added to collection: " + card.getName() + " for user " + userId);
        return item;
    }

    public List<CollectionItem> getUserCollection(String userId) 
            throws ExecutionException, InterruptedException {
        
        QuerySnapshot querySnapshot = firestore
            .collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .get()
            .get();

        return querySnapshot.getDocuments().stream()
            .map(doc -> doc.toObject(CollectionItem.class))
            .collect(Collectors.toList());
    }

    public CollectionItem getCollectionItemById(String itemId) 
            throws ExecutionException, InterruptedException {
        
        DocumentSnapshot document = firestore
            .collection(COLLECTION_NAME)
            .document(itemId)
            .get()
            .get();

        if (document.exists()) {
            return document.toObject(CollectionItem.class);
        }
        return null;
    }

    public void removeFromCollection(String itemId) 
            throws ExecutionException, InterruptedException {
        
        firestore.collection(COLLECTION_NAME).document(itemId).delete().get();
        System.out.println("✅ Removed from collection: " + itemId);
    }

    public Double getTotalCollectionValue(String userId) 
            throws ExecutionException, InterruptedException {
        
        List<CollectionItem> collection = getUserCollection(userId);
        
        return collection.stream()
            .filter(item -> !Boolean.TRUE.equals(item.getIsWishlist()))
            .mapToDouble(item -> {
                if (item.getCard() != null && item.getCard().getCurrentPrice() != null) {
                    return item.getCard().getCurrentPrice() * item.getQuantity();
                }
                return 0.0;
            })
            .sum();
    }

    public CollectionStats getCollectionStats(String userId) 
            throws ExecutionException, InterruptedException {
        
        List<CollectionItem> collection = getUserCollection(userId);
        List<CollectionItem> ownedItems = collection.stream()
            .filter(item -> !Boolean.TRUE.equals(item.getIsWishlist()))
            .collect(Collectors.toList());
        
        int totalCards = ownedItems.stream()
            .mapToInt(CollectionItem::getQuantity)
            .sum();
        
        double totalValue = getTotalCollectionValue(userId);
        
        double totalInvested = ownedItems.stream()
            .mapToDouble(item -> {
                if (item.getPurchasePrice() != null) {
                    return item.getPurchasePrice() * item.getQuantity();
                }
                return 0.0;
            })
            .sum();
        
        double profitLoss = totalValue - totalInvested;
        
        return new CollectionStats(
            ownedItems.size(),
            totalCards,
            totalValue,
            totalInvested,
            profitLoss
        );
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CollectionStats {
        private int uniqueCards;
        private int totalCards;
        private double totalValue;
        private double totalInvested;
        private double profitLoss;
    }
}