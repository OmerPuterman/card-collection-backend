package com.cardcollection.controller;

import com.cardcollection.model.CollectionItem;
import com.cardcollection.service.CollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collections")
@CrossOrigin(origins = "*")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CollectionItem>> getUserCollection(@PathVariable String userId) {
        try {
            List<CollectionItem> collection = collectionService.getUserCollection(userId);
            return ResponseEntity.ok(collection);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{userId}/value")
    public ResponseEntity<Map<String, Double>> getTotalValue(@PathVariable String userId) {
        try {
            Double totalValue = collectionService.getTotalCollectionValue(userId);
            Map<String, Double> response = new HashMap<>();
            response.put("totalValue", totalValue);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<CollectionService.CollectionStats> getCollectionStats(@PathVariable String userId) {
        try {
            CollectionService.CollectionStats stats = collectionService.getCollectionStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{userId}/test/add-card")
    public ResponseEntity<CollectionItem> testAddCard(
            @PathVariable String userId,
            @RequestParam String cardId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @RequestParam(defaultValue = "Near Mint") String condition,
            @RequestParam(required = false) Double purchasePrice) {
        try {
            CollectionItem item = new CollectionItem();
            item.setCardId(cardId);
            item.setQuantity(quantity);
            item.setCondition(condition);
            item.setPurchasePrice(purchasePrice);
            item.setIsWishlist(false);
            
            CollectionItem created = collectionService.addToCollection(userId, item);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeFromCollection(@PathVariable String itemId) {
        try {
            collectionService.removeFromCollection(itemId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}