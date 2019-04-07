package com.kalom.UnipiTouristicApp;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowActivity extends AppCompatActivity {

    private static final int CONTENT_VIEW_ID = 10101010;
    private PositionModel pointOfInterest;
    private TextView title;
    private TextView category;
    private ImageView image;
    private Button speekerButton;
    private Speaker speeker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        speeker = new Speaker(this);
        title = findViewById(R.id.titleView);
        category = findViewById(R.id.categoryView);
        image = findViewById(R.id.imageView);
        speekerButton = findViewById(R.id.button2);
        pointOfInterest = (PositionModel) getIntent().getSerializableExtra("pointOfInterest");
        setViewContent(pointOfInterest);
        speekerButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                speeker.speak(pointOfInterest.getDesc());
            }
        });


    }

    private void setViewContent(PositionModel pointOfInterest) {
        title.setText(pointOfInterest.getTitle());
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
                image.setImageResource(R.drawable.stiledios);
                break;
            case "Arxaia Korinthos":
                image.setImageResource(R.drawable.arxaiakorintos);
                break;
            case "Nea Smirni alsos":
                image.setImageResource(R.drawable.neasmirni);
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
