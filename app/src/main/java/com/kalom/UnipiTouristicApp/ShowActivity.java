package com.kalom.UnipiTouristicApp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        image = findViewById(R.id.imageView);
        pointOfInterest = (PositionModel) getIntent().getSerializableExtra("pointOfInterest");
        setViewContent(pointOfInterest);


    }

    private void setViewContent(PositionModel pointOfInterest) {
        title.setText(pointOfInterest.getTitle());
        description.setText(pointOfInterest.getDesc());
        category.setText(pointOfInterest.getCateg());
        setImage(pointOfInterest.getTitle());
    }

    private void setImage(String title) {
        switch (title) {
            case "Acropolis":
                image.setImageResource(R.drawable.parthenon);
                break;
            case "kalhmarmaro":
                image.setImageResource(R.drawable.kalimarmaro);
                break;
            case "Stiles tou Dios":
                image.setImageResource(R.drawable.stileDios);
                break;
            case "Arxaia Korinthos":
                image.setImageResource(R.drawable.arxaiaKorintos);
                break;
            default:
                message("Error in images!");
                break;
        }
    }

    private void message(String messageKey) {
        Toast.makeText(this, messageKey, Toast.LENGTH_SHORT).show();
    }
}
