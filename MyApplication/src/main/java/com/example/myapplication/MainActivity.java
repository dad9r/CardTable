package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    Intent serverIntent;
    Intent playerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button serverButton = (Button) findViewById(R.id.server_button);
        Button playerButton = (Button) findViewById(R.id.client_button);
        serverIntent = new Intent(MainActivity.this, TableActivity.class);
        playerIntent = new Intent(MainActivity.this, HandActivity.class);

        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(serverIntent);
            }
        });

        playerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(playerIntent);
            }
        });
    }


    public TableRPCIFace attachToServer() {
        serverIntent.
        return null;
    }
}
