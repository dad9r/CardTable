package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by dad9r on 8/7/13.
 */
public class HandActivity extends Activity {

    private final int handSize = 5;

    private TextView textView;
    private Button deal1Button;
    private Button dealHandButton;

    private int nextCard;

    private TableRPCIFace table;

    private Button[] cards = new Button[handSize];
    private Card[] hand = new Card[handSize];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand);

        table = null;

        textView = (TextView) findViewById(R.id.text_view);
        deal1Button = (Button) findViewById(R.id.deal_1_button);
        dealHandButton = (Button) findViewById(R.id.deal_hand_button);

        cards[0] = (Button) findViewById(R.id.card_button_1);
        cards[1] = (Button) findViewById(R.id.card_button_2);
        cards[2] = (Button) findViewById(R.id.card_button_3);
        cards[3] = (Button) findViewById(R.id.card_button_4);
        cards[4] = (Button) findViewById(R.id.card_button_5);
        for (int i = 0; i < handSize; ++i) {
            cards[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int which;
                    switch (view.getId()) {
                        case R.id.card_button_1:
                            which = 0;
                            break;
                        case R.id.card_button_2:
                            which = 1;
                            break;
                        case R.id.card_button_3:
                            which = 2;
                            break;
                        case R.id.card_button_4:
                            which = 3;
                            break;
                        case R.id.card_button_5:
                            which = 4;
                            break;
                        default:
                            // impossible, currently
                            which = -1;
                    }
                    table.discard(hand[which]);
                    nextCard--;
                    for (int i = which; i < nextCard; ++i) {
                        hand[i] = hand[i+1];
                        cards[i].setText(cards[i+1].getText());
                    }
                    hand[nextCard] = null;
                    cards[nextCard].setText("");
                    cards[nextCard].setVisibility(View.INVISIBLE);
                }
            });
        }
        nextCard = 0;

        deal1Button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (nextCard < handSize) {
                    dealOneCard();
                }
                else {
                    makeToast("Full hand");
                }
            }
        });

        dealHandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                while (nextCard < handSize) {
                    dealOneCard();
                }
            }
        });
    }

    private void dealOneCard() {
        try {
            Card card = table.draw();
            textView.setText(card.toString());
            cards[nextCard].setText(card.toString());
            cards[nextCard].setVisibility(View.VISIBLE);
            hand[nextCard] = card;
            nextCard++;
        } catch (CardDeck.DeckExhaustedException e) {
            e.printStackTrace();
        }
    }

    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}