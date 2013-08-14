package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HandActivity.this.openContextMenu(view);
                }
            });
            registerForContextMenu(newButton);

            cardButtonFrame.addView(newButton, newButtonParams);
            hand.add(new Pair<Card, Button>(card, newButton));
        }
    }

    private Pair<Card, Button> clicked;
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        for (Pair<Card, Button> p : hand) {
            if (p.second.getId() == v.getId()) {
                clicked = p;
            }
        }

        if (clicked != null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_hand, menu);
        }
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        clicked = null;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.hand_context_menu_discard:
                hand.remove(clicked);
                table.discard(clicked.first);
                cardButtonFrame.removeView(clicked.second);
                clicked = null;
                break;
            case R.id.hand_context_menu_pass:
            case R.id.hand_context_menu_play:
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
