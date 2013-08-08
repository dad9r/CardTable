package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private Intent serverIntent;
    private Intent playerIntent;

    public static final int[] availablePorts = {80, 123, 777};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button serverButton = (Button) findViewById(R.id.server_button);
        Button playerButton = (Button) findViewById(R.id.client_button);
        serverIntent = new Intent(MainActivity.this, PortChooserActivity.class);
        playerIntent = new Intent(MainActivity.this, ConnectActivity.class);

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
}
