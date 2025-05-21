package com.example.myprogyoav;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firestore.admin.v1.Index;

import org.w3c.dom.Text;

import java.util.List;

public class QuestionAdapter extends ArrayAdapter<Question> {
    Context context;
    List<Question> questions;

    public QuestionAdapter(Context context,int resource,int textViewResourceId,List<Question> questionList){
        super(context,resource,textViewResourceId,questionList);

        this.context = context;
        this.questions = questionList;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Reuse the convertView if it is not null, otherwise inflate a new view
        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.question, parent, false);
        }

        // Find the views inside the converted view
        TextView indexOfTheQuestion = (TextView) convertView.findViewById(R.id.TheNumberOfTheQuestion);
        TextView questionName = (TextView) convertView.findViewById(R.id.Questinname);
        RatingBar diffratingBar = (RatingBar) convertView.findViewById(R.id.diffrate);
        RatingBar funratingBar = (RatingBar) convertView.findViewById(R.id.funrate);

        // Get the current question from the list
        Question temp = questions.get(position);

        questionName.setText(temp.GetName());
        indexOfTheQuestion.setText("" + temp.GetCounter());
        diffratingBar.setRating((float) temp.GetDifficultyrating());
        funratingBar.setRating((float)temp.GetFunrating());



        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InsideAQuestion.class);
                intent.putExtra("referance", temp.GetReferance());
                context.startActivity(intent);
            }
        });




        return convertView;
    }

}
