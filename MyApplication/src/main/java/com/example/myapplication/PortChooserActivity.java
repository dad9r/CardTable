package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

/**
 * Created by dad9r on 8/8/13.
 */
public class PortChooserActivity extends Activity {

    private Button okButton;
    private Button cancelButton;

    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_port_chooser);

        okButton = (Button) findViewById(R.id.port_chooser_ok_button);
        cancelButton = (Button) findViewById(R.id.port_chooser_cancel_button);

        radioGroup = (RadioGroup) findViewById(R.id.port_radio_group);

       okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PortChooserActivity.this, TableActivity.class);
                int port = -1;
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.port_radio_1:
                        port = MainActivity.availablePorts[0];
                        break;
                    case R.id.port_radio_2:
                        port = MainActivity.availablePorts[1];
                        break;
                    case R.id.port_radio_3:
                        port = MainActivity.availablePorts[2];
                        break;
                }
                intent.putExtra("port", port);
                startActivity(intent);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
