package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by dad9r on 8/7/13.
 */
public class TableActivity extends Activity {

    private CardDeck deck;

    private Button shuffleButton;

    private TableRPCReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        int port = getIntent().getIntExtra("port", 0);

        deck = new CardDeck();

        shuffleButton = (Button) findViewById(R.id.shuffle_button);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deck.shuffleRemainingDeck();
                shuffleButton.setText("Shuffle Deck (" + deck.numCardsInDeck() + ")");
            }
        });

        receiver = new TableRPCReceiver(this, port);
    }

    public Card getTopCard() throws CardDeck.DeckExhaustedException {
        Card ret = deck.getTopCard();
        shuffleButton.setText("Shuffle Deck (" + deck.numCardsInDeck() + ")");
        return ret;
    }

    public void insertCardBottom(Card card) {
        deck.insertCardBottom(card);
        shuffleButton.setText("Shuffle Deck (" + deck.numCardsInDeck() + ")");
    }
}
