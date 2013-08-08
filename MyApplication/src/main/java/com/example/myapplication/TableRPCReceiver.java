package com.example.myapplication;

/**
 * Created by dad9r on 8/7/13.
 */
public class TableRPCReceiver implements TableRPCIFace {

    private TableActivity table;

    public TableRPCReceiver(TableActivity tableActivity) {
        table = tableActivity;
    }

    public void discard(Card card) {
        table.insertCardBottom(card);
    }

    public Card draw() throws CardDeck.DeckExhaustedException {
        return table.getTopCard();
    }
}
