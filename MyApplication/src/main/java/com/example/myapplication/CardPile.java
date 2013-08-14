package com.example.myapplication;

import java.util.Collection;
import java.util.Stack;

/**
 * Created by dad9r on 8/14/13.
 */
public class CardPile {
    Stack<Card> pile;

    public CardPile() {
        pile = new Stack<Card>();
    }

    public int numCardsInPile() {
        return pile.size();
    }

    public void insertCardTop(Card card) {
        pile.push(card);
    }

    public Collection<Card> removeAll () {
        Collection<Card> toReturn = pile;
        pile = new Stack<Card>();
        return toReturn;
    }

    public Card drawTopCard() {
        return pile.pop();
    }
}
