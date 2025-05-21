package com.example.myprogyoav;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AnswerAdapter  extends ArrayAdapter<Answer> {
    Context context;
    List<Answer> answers;

    public AnswerAdapter(Context context,int resource,int textViewResourceId,List<Answer> answerslist){
        super(context,resource,textViewResourceId,answerslist);

        this.context = context;
        this.answers = answerslist;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reuse the convertView if it is not null, otherwise inflate a new view
        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.answerbuild, parent, false);
        }

        TextView usern = (TextView) convertView.findViewById(R.id.usernameanswer);
        ImageButton pfp = (ImageButton) convertView.findViewById(R.id.pfpanswer);
        TextView answercontent = (TextView) convertView.findViewById(R.id.textforanswer);


        // Get the current question from the list
        Answer temp = answers.get(position);

        // Set data to the views

        answercontent.setText(temp.GetDescription());


        temp.PrepareUser().thenAccept(number ->{
            Bitmap profileImage = temp.GetImage();
            if(profileImage!=null){
                pfp.setImageBitmap(profileImage);

            }

            String name = temp.GetName();
            if(name!=null){
                usern.setText(name);
            }
        });



        pfp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WatchProfile.class);
                temp.GetClassUser().Solved();
                intent.putExtra("User",temp.GetUser());
                ((Activity)context).startActivity(intent);

            }
        });

        return convertView;
    }

}
