package com.example.myprogyoav;

import android.app.Dialog;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;


public class InsideAQuestion extends AppCompatActivity implements View.OnClickListener {
    Question thisquestion;
    Button checkanswer;
    EditText AnswerEdittext;
    Dialog d;
    Button publishAnswer;
    FirebaseAuth auth;

    FirebaseUser user;
    TextView contenttext;
    ListView lv;
    EditText AddDescription;

    RatingBar diffRating;
    RatingBar interestRating;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inside_aquestion);


         AnswerEdittext = findViewById(R.id.answeredit);
         contenttext = findViewById(R.id.textcontent);


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        checkanswer = findViewById(R.id.uploadanswerbutton);
        checkanswer.setOnClickListener(this);







        Runnable afterquestionsetup = new Runnable() {
            @Override
            public void run() {
                MoreSetup();
            }
        };
        lv = findViewById(R.id.listviewinsideaquestion);
        thisquestion = new Question(this.getIntent().getStringExtra("referance"),afterquestionsetup);


        thisquestion.IsSolved(user.getUid()).thenApply(IsSolved ->{
            if(IsSolved){
                AnswerEdittext.setVisibility(View.GONE);
                checkanswer.setVisibility(View.GONE);
            }
            else{
                lv.setVisibility(View.GONE);

            }
            return null;
        });
    }
    private void MoreSetup(){
        contenttext.setText(thisquestion.getContent());

        List<Answer> answers =  thisquestion.GetAnswers();
        AnswerAdapter adapter = new AnswerAdapter(this,0,0,answers);
        lv.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if(v == checkanswer){
            String answer = AnswerEdittext.getText().toString();
            String text = "Your answer is right";


            d= new Dialog(this);
            d.setContentView(R.layout.dialoug);
            d.setCancelable(true);
            d.setTitle("upload");


            AddDescription = d.findViewById(R.id.AddedContext);
            publishAnswer = d.findViewById(R.id.publishanswer);
            diffRating = d.findViewById(R.id.ratediff);
            interestRating = d.findViewById(R.id.rateinterest);

            TextView texttohide1 = d.findViewById(R.id.texttohide1);
            TextView texttohide2 = d.findViewById(R.id.texttohide2);

            if (thisquestion.CheckAnswer(answer) == false){
                text = "Your answer is wrong";
                texttohide1.setVisibility(View.GONE);
                texttohide2.setVisibility(View.GONE);
                AddDescription.setVisibility(View.GONE);
                publishAnswer.setVisibility(View.GONE);
                diffRating.setVisibility(View.GONE);
                interestRating.setVisibility(View.GONE);
            }
            TextView t = d.findViewById(R.id.rightorwrong);
            t.setText(text);


            publishAnswer.setOnClickListener(this);

            d.show();
        }
        if (v == publishAnswer){
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            String desc = AddDescription.getText().toString();

            double InterstRating = interestRating.getRating();
            double DiffRating = diffRating.getRating();

            Answer ans1 = new Answer(desc,user.getUid(),InterstRating,DiffRating);

            thisquestion.UploadAnswer(ans1);
            d.cancel();
            finish();

            Toast.makeText(InsideAQuestion.this, "Uploaded Your Answer", Toast.LENGTH_SHORT).show();
        }
    }


}