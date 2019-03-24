package com.kalom.UnipiTouristicApp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ShowActivity extends AppCompatActivity {

    private PositionModel pointOfInterest;
    private TextView title;
    private TextView description;
    private TextView category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        title = findViewById(R.id.titleView);
        description = findViewById(R.id.descriptionView);
        category = findViewById(R.id.categoryView);
        pointOfInterest = (PositionModel) getIntent().getSerializableExtra("pointOfInterest");
        setViewContent(pointOfInterest);

    }

    private void setViewContent(PositionModel pointOfInterest) {
        title.setText(pointOfInterest.getTitle());
        description.setText(pointOfInterest.getDesc());
        category.setText(pointOfInterest.getCateg());
    }
}
