package com.cardcollection.controller;

import com.cardcollection.model.Card;
import com.cardcollection.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * GET /api/cards
     * Get all cards
     */
    @GetMapping
    public ResponseEntity<List<Card>> getAllCards() {
        try {
            List<Card> cards = cardService.getAllCards();
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/cards/{id}
     * Get card by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Card> getCardById(@PathVariable String id) {
        try {
            Card card = cardService.getCardById(id);
            if (card != null) {
                return ResponseEntity.ok(card);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/cards
     * Create new card
     */
    @PostMapping
    public ResponseEntity<Card> createCard(@RequestBody Card card) {
        try {
            Card created = cardService.createCard(card);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/cards/search
     * Search cards with filters
     */
    @GetMapping("/search")
    public ResponseEntity<List<Card>> searchCards(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String game,
            @RequestParam(required = false) String cardType) {
        try {
            List<Card> cards = cardService.searchCards(query, game, cardType);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/cards/{id}
     * Delete card by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable String id) {
        try {
            cardService.deleteCard(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/cards/test/add-luffy
     * Test endpoint - adds Luffy card
     */
    @GetMapping("/test/add-luffy")
    public ResponseEntity<Card> addLuffyTest() {
        try {
            Card luffy = new Card();
            luffy.setName("Monkey.D.Luffy");
            luffy.setCardType("LEADER");
            luffy.setGame("ONE_PIECE_TCG");
            luffy.setColor("RED");
            luffy.setCost(0);
            luffy.setPower(5000);
            luffy.setAttribute("Strike");
            luffy.setEffect("[DON!! x1] [When Attacking] Give up to 1 of your Leader or Character cards +1000 power during this battle.");
            luffy.setSet("Romance Dawn");
            luffy.setNumber("OP01-001");
            luffy.setRarity("LEADER");
            luffy.setCurrentPrice(45.99);
            luffy.setTags(List.of("Straw Hat Pirates", "Protagonist", "Captain"));
            luffy.setReleaseDate("2022-07-08");
            
            Card created = cardService.createCard(luffy);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/cards/import
     * Bulk import cards from JSON file
     */
    @GetMapping("/import")
    public ResponseEntity<Map<String, Object>> importCards() {
        try {
            List<Card> cards = cardService.importCardsFromJson();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", cards.size());
            response.put("message", "Successfully imported " + cards.size() + " cards!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    
}