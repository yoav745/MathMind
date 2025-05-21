package com.example.myprogyoav;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WatchProfile extends AppCompatActivity {
    private String TAG = "WatchProfile";
    private String useruid;
    private ImageView pfp;

    private TextView nametext,solvedtext,createdtext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_profile);


        Intent thisintent = this.getIntent();
        useruid = thisintent.getStringExtra("User");
        Log.d(TAG, useruid);

        pfp = findViewById(R.id.pfp);
        nametext = findViewById(R.id.nametext);
        solvedtext = findViewById(R.id.solvedtext);
        createdtext = findViewById(R.id.createdtext);


        User thisuser = new User(useruid);

        thisuser.LoadDataFromFirebase().thenApply(number -> {
            Bitmap bitmap = thisuser.GetImage();
            if (bitmap != null) {
                pfp.setImageBitmap(bitmap);
            }

            nametext.setText("Username: "+thisuser.GetName());
            solvedtext.setText("Questions solved:" + thisuser.GetSolved());
            createdtext.setText("Questions created: " + thisuser.GetCreated());

            return null;
        });

    }
}