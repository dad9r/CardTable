package com.example.myapplication;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Created by dad9r on 8/2/13.
 */
public class CardDeck {
    public final int maxCardsInDeck;

    private ArrayDeque<Card> cardsInDeck;

    public int numCardsInDeck() { return cardsInDeck.size();}

    public CardDeck() {
        maxCardsInDeck = Card.cardSuit.values().length * Card.cardValue.values().length;
        initializeDeck();
    }

    public void shuffleRemainingDeck() {
        ArrayList<Card> tmp = new ArrayList<Card>();
        tmp.addAll(cardsInDeck);
        cardsInDeck.clear();

        Random r = new Random();
        while (tmp.size() > 0){
            Card next = tmp.remove(r.nextInt(tmp.size()));
            cardsInDeck.addLast(next);
        }
    }

    public void shuffleFullDeck() {
        cardsInDeck = new ArrayDeque<Card>();
        for (Card.cardSuit suit : Card.cardSuit.values()) {
            for (Card.cardValue value : Card.cardValue.values()) {
                cardsInDeck.addLast(new Card(suit, value));
            }
        }
        shuffleRemainingDeck();
    }

    public Card getTopCard() throws DeckExhaustedException {
        Card result;
        if (numCardsInDeck() > 0) {
            result = cardsInDeck.getFirst();
            cardsInDeck.remove(result);
        }
        else {
            throw new DeckExhaustedException("Deck is empty");
        }
        if (result == null) {
            throw new DeckExhaustedException("Card returned is null");
        }
        return result;
    }

    public Card getBottomCard() throws DeckExhaustedException {
        Card result;
        if (numCardsInDeck() > 0) {
            result = cardsInDeck.getLast();
            cardsInDeck.remove(result);
        }
        else {
            throw new DeckExhaustedException("Deck is empty");
        }
        return result;
    }

    public void insertCardTop(Card card) {
        cardsInDeck.addFirst(card);
    }

    public void insertCardBottom(Card card) {
        cardsInDeck.addLast(card);
    }

    public void insertPileBottom(Collection<Card> cards) {
        cardsInDeck.addAll(cards);
    }

    private void initializeDeck() {
        if (cardsInDeck == null) {
            cardsInDeck = new ArrayDeque<Card>(maxCardsInDeck);
        }

        for (Card.cardSuit suit : Card.cardSuit.values()) {
            for (Card.cardValue value : Card.cardValue.values()) {
                cardsInDeck.add(new Card(suit, value));
            }
        }
    }

    public class DeckExhaustedException extends Exception {
        public DeckExhaustedException(String s) {
            super(s);
        }
    }

}
