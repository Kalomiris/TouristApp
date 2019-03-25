package com.kalom.UnipiTouristicApp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowActivity extends AppCompatActivity {

    private static final int CONTENT_VIEW_ID = 10101010;
    private PositionModel pointOfInterest;
    private TextView title;
    private TextView description;
    private TextView category;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        title = findViewById(R.id.titleView);
        description = findViewById(R.id.descriptionView);
        category = findViewById(R.id.categoryView);

        pointOfInterest = (PositionModel) getIntent().getSerializableExtra("pointOfInterest");
        setViewContent(pointOfInterest);

        FrameLayout frame = new FrameLayout(this);
        frame.setId(CONTENT_VIEW_ID);
        setContentView(frame, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

    }

    private void setViewContent(PositionModel pointOfInterest) {
        title.setText(pointOfInterest.getTitle());
        description.setText(pointOfInterest.getDesc());
        category.setText(pointOfInterest.getCateg());
    }
}
