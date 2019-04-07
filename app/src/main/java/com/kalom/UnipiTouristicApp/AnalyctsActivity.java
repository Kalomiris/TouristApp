package com.kalom.UnipiTouristicApp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AnalyctsActivity extends AppCompatActivity {

    private TextView lastPOIs;
    private TextView counterPOIs;
    private Button returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analicts);
        lastPOIs = findViewById(R.id.lastPOIsText);
        counterPOIs = findViewById(R.id.counterText);
        returnButton = findViewById(R.id.returnButton);
        TimestampPosition timestampPosition = (TimestampPosition) getIntent().getSerializableExtra("analytics");
        int counter = getIntent().getIntExtra("counter", 0);
        counterPOIs.setText("" + counter);
        lastPOIs.setText(timestampPosition.getPositionModel().getTitle());

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AnalyctsActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

    }
}
