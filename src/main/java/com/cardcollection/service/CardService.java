package com.cardcollection.service;

import com.cardcollection.model.Card;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@Service
public class CardService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "cards";

    public CardService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Create a new card in Firestore
     */
    public Card createCard(Card card) throws ExecutionException, InterruptedException {
        if (card.getId() == null || card.getId().isEmpty()) {
            card.setId(UUID.randomUUID().toString());
        }
        
        long now = System.currentTimeMillis();
        card.setCreatedAt(now);
        card.setUpdatedAt(now);

        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(card.getId());
        docRef.set(card).get();

        System.out.println("✅ Card created: " + card.getName() + " (ID: " + card.getId() + ")");
        return card;
    }

    /**
     * Get card by ID
     */
    public Card getCardById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore
            .collection(COLLECTION_NAME)
            .document(id)
            .get()
            .get();

        if (document.exists()) {
            return document.toObject(Card.class);
        }
        return null;
    }

    /**
     * Get all cards
     */
    public List<Card> getAllCards() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore
            .collection(COLLECTION_NAME)
            .get()
            .get();

        return querySnapshot.getDocuments().stream()
            .map(doc -> doc.toObject(Card.class))
            .collect(Collectors.toList());
    }

    /**
     * Search cards with filters
     */
    public List<Card> searchCards(String query, String game, String cardType) 
            throws ExecutionException, InterruptedException {
        
        Query firestoreQuery = firestore.collection(COLLECTION_NAME);

        if (game != null && !game.isEmpty()) {
            firestoreQuery = firestoreQuery.whereEqualTo("game", game);
        }

        if (cardType != null && !cardType.isEmpty()) {
            firestoreQuery = firestoreQuery.whereEqualTo("cardType", cardType);
        }

        QuerySnapshot querySnapshot = firestoreQuery.get().get();
        List<Card> cards = querySnapshot.getDocuments().stream()
            .map(doc -> doc.toObject(Card.class))
            .collect(Collectors.toList());

        if (query != null && !query.isEmpty()) {
            String lowerQuery = query.toLowerCase();
            cards = cards.stream()
                .filter(card -> card.getName().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
        }

        return cards;
    }

    /**
     * Delete card by ID
     */
    public void deleteCard(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION_NAME).document(id).delete().get();
        System.out.println("✅ Card deleted: " + id);
    }
    
    /**
     * Bulk import cards from JSON file
     */
    public List<Card> importCardsFromJson() throws IOException, ExecutionException, InterruptedException {
        // Read JSON file from resources
        InputStream inputStream = getClass()
            .getClassLoader()
            .getResourceAsStream("data/cards.json");
        
        if (inputStream == null) {
            throw new IOException("cards.json file not found!");
        }
        
        // Parse JSON to Card objects
        ObjectMapper objectMapper = new ObjectMapper();
        Card[] cardsArray = objectMapper.readValue(inputStream, Card[].class);
        
        List<Card> importedCards = new ArrayList<>();
        
        // Save each card to Firestore
        for (Card card : cardsArray) {
            Card created = createCard(card);
            importedCards.add(created);
        }
        
        System.out.println("✅ Successfully imported " + importedCards.size() + " cards!");
        return importedCards;
    }
}