package com.cardcollection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private String id;
    private String name;
    private String cardType;        // CHARACTER, EVENT, STAGE, LEADER
    private String game;            // ONE_PIECE_TCG, SOCCER_CARDS
    
    // One Piece TCG Specific
    private String color;           // RED, BLUE, GREEN, PURPLE, BLACK, YELLOW
    private Integer cost;
    private Integer power;
    private Integer counter;
    private String attribute;       // Strike, Slash, etc.
    private String effect;
    private String trigger;
    
    // Soccer Cards Specific
    private String playerName;
    private String team;
    private String league;
    private String position;
    private Integer rating;
    private String season;
    
    // Common Fields
    private String set;
    private String setCode;
    private String number;
    private String rarity;
    private String imageUrl;
    private String artist;
    private Double currentPrice;
    private String releaseDate;
    private List<String> tags;
    private Map<String, Object> attributes;
    
    // Metadata
    private Long createdAt;
    private Long updatedAt;
}