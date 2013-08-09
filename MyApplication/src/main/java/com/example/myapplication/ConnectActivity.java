package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by dad9r on 8/8/13.
 */
public class ConnectActivity extends Activity {

    private Intent handIntent;
    private EditText ipText;
    private EditText portText;
    private Button connectButton;
    private final String defaultIP = "IP Address";
    private final String defaultPort = "Port";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        ipText = (EditText) findViewById(R.id.connect_ip_text_box);
        ipText.setText(defaultIP);

        portText = (EditText) findViewById(R.id.connect_port_text_box);
        portText.setText(defaultPort);

        connectButton = (Button) findViewById(R.id.connect_ok_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = ipText.getText().toString();
                String port = portText.getText().toString();

                handIntent.putExtra("serverAddr", ip);
                handIntent.putExtra("serverPort", port);

                startActivity(handIntent);

                finish();
            }
        });

        ipText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && ipText.getText().toString().equals(defaultIP)) {
                    ipText.setText("");
                }
                else if (!b && ipText.getText().toString().equals("")) {
                    ipText.setText(defaultIP);
                }
            }
        });
        portText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && portText.getText().toString().equals(defaultPort)) {
                    portText.setText("");
                }
                else if (!b && portText.getText().toString().equals("")) {
                    portText.setText(defaultPort);
                }
            }
        });

        handIntent = new Intent(this, HandActivity.class);
    }
}