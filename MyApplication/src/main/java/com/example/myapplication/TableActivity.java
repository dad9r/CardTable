package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by dad9r on 8/7/13.
 */
public class TableActivity extends Activity {

    private CardDeck deck;
    private CardPile discard;

    private Button deckButton;
    private Button discardButton;
    private Button quitButton;

    private TextView infoText;

    private TableRPCReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        int port = getIntent().getIntExtra("port", 0);

        deck = new CardDeck();
        discard = new CardPile();

        quitButton = (Button) findViewById(R.id.table_quit_button);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            receiver.terminate();
            finish();
            }
        });

        deckButton = (Button) findViewById(R.id.table_deck_button);
        deckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deck.shuffleRemainingDeck();
                deckButton.setText("Deck (" + deck.numCardsInDeck() + ")");
                discardButton.setText("Discard (" + discard.numCardsInPile() + ")");
            }
        });

        discardButton = (Button) findViewById(R.id.table_discard_button);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deck.insertPileBottom(discard.removeAll());
                deckButton.setText("Deck (" + deck.numCardsInDeck() + ")");
                discardButton.setText("Discard (" + discard.numCardsInPile() + ")");
            }
        });

        receiver = new TableRPCReceiver(this, port);

        infoText = (TextView) findViewById(R.id.table_ip_text_view);
        infoText.setText("IP: " + receiver.getIP() + " Port: " + receiver.getPort());
    }

    public Card getTopCard() throws CardDeck.DeckExhaustedException {
        Card ret = deck.getTopCard();
//        deckButton.setText("Shuffle Deck (" + deck.numCardsInPile() + ")");
        return ret;
    }

    public void insertCardBottom(Card card) {
        deck.insertCardBottom(card);
//        deckButton.setText("Shuffle Deck (" + deck.numCardsInPile() + ")");
    }

    public void addToDiscard(Card card) {
        discard.insertCardTop(card);
    }

    public void shuffle() {
        deck.shuffleRemainingDeck();
    }
}
