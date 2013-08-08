package com.example.myapplication;

/**
 * Created by dad9r on 8/2/13.
 */
public class Card {
    public enum cardSuit {
        Spades,
        Diamonds,
        Clubs,
        Hearts;
    };
    public final String[] cardSuitName = {
            "Spades",
            "Diamonds",
            "Clubs",
            "Hearts"
    };
    public enum cardValue {
        Ace,
        Two,
        Three,
        Four,
        Five,
        Six,
        Seven,
        Eight,
        Nine,
        Ten,
        Jack,
        Queen,
        King;
    };
    public final String[] cardValueName = {
            "Ace",
            "Two",
            "Three",
            "Four",
            "Five",
            "Six",
            "Seven",
            "Eight",
            "Nine",
            "Ten",
            "Jack",
            "Queen",
            "King"
    };

    public String toString() {
        return cardValueName[value.ordinal()] + " of " + cardSuitName[suit.ordinal()];
    }

    private cardSuit suit;
    public cardSuit getSuit() {
        return suit;
    }


    private cardValue value;
    public cardValue getValue() {
        return value;
    }

    private Card() {
        suit = null;
        value = null;
    }

    public Card(cardSuit suit, cardValue value) {
        this.suit = suit;
        this.value = value;
    }

}
