package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by dad9r on 8/7/13.
 */
public class HandActivity extends Activity {

    private final int maxHandSize = 5;

    private Random r = new Random();

    private TextView textView;
    private LinearLayout cardButtonFrame;

    private TableRPCSender table;

    private ArrayList<Pair<Card, Button>> hand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand);

        String ip = getIntent().getStringExtra("serverAddr");
        String port = getIntent().getStringExtra("serverPort");

        try {
            table = new TableRPCSender(ip, port);
        } catch (IOException e) {
            Toast.makeText(this, "Cannot connect to host", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }

        hand = new ArrayList<Pair<Card, Button>>();
        cardButtonFrame = (LinearLayout) findViewById(R.id.hand_card_frame);
        textView = (TextView) findViewById(R.id.hand_text_view);
        Button deal1Button = (Button) findViewById(R.id.hand_deal_1_button);
        Button dealHandButton = (Button) findViewById(R.id.hand_deal_hand_button);
        Button quitButton = (Button) findViewById(R.id.hand_quit_button);

        deal1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitListener.reset();
                if (hand.size() < maxHandSize) {
                    dealOneCard();
                } else {
                    makeToast("Full hand");
                }
            }
        });

        dealHandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quitListener.reset();
                while (hand.size() < maxHandSize) {
                    dealOneCard();
                }
            }
        });

        quitButton.setOnClickListener(quitListener);
    }

    class QuitListener implements View.OnClickListener {
        private boolean verify = false;

        public void reset() {
            verify = false;
        }

        @Override
        public void onClick(View view) {
            if (verify) {
                while (hand.size() > 0) {
                    table.discard(hand.remove(0).first);
                }
                table.requestTerminate();
                finish();
            }
            else {
                makeToast("Are you sure? Press the quit button again to drop out of the game");
                verify = true;
            }
        }
    }
    private QuitListener quitListener = new QuitListener();

    private View.OnClickListener cardButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            quitListener.reset();
            if (!table.stillConnected()) {
                makeToast("Lost connection to table, shutting down");
                table.requestTerminate();
                finish();
            }
            else {
                Pair<Card, Button> which = null;
                for (Pair<Card, Button> p : hand) {
                    if (p.second.getId() == view.getId()) {
                        which = p;
                    }
                }
                if (which != null) {
                    hand.remove(which);
                    table.discard(which.first);
                    cardButtonFrame.removeView(which.second);
                }
            }
        }
    };

    private void dealOneCard() {
        if (!table.stillConnected()) {
            makeToast("Lost connection to table, shutting down");
            table.requestTerminate();
            finish();
        }
        else {
            Card card = table.draw();
            textView.setText(card.toString());

            Button newButton = new Button(this);
            LinearLayout.LayoutParams newButtonParams =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            newButton.setId(r.nextInt());
            newButton.setText(card.toString());
            newButton.setVisibility(View.VISIBLE);
            newButton.setOnClickListener(cardButtonListener);

            cardButtonFrame.addView(newButton, newButtonParams);
            hand.add(new Pair<Card, Button>(card, newButton));
        }
    }

    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
