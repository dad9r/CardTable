package com.example.myapplication;

/**
 * Created by dad9r on 8/7/13.
 */
public interface TableRPCIFace {
    public void discard(Card card);
    public Card draw() throws CardDeck.DeckExhaustedException;
}
