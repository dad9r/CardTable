package com.example.myapplication;

/**
 * Created by dad9r on 8/2/13.
 */
public class Card {
    public enum cardSuit {
        Spades,
        Diamonds,
        Clubs,
        Hearts
    }
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
        King
    }

    public String toString() {
        return value.toString() + " of " + suit.toString();
    }

    private cardSuit suit;
    public cardSuit getSuit() {
        return suit;
    }


    private cardValue value;
    public cardValue getValue() {
        return value;
    }

    public Card(int suit, int value) {
        this.suit = cardSuit.values()[suit];
        this.value = cardValue.values()[value];
    }

    public Card(cardSuit suit, cardValue value) {
        this.suit = suit;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Card)) return false;
        Card c = (Card) o;
        return value.equals(c.getValue()) && suit.equals(c.getSuit());
    }
}
