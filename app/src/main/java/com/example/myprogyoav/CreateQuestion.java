package com.example.myprogyoav;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateQuestion extends AppCompatActivity implements View.OnClickListener {

    ArrayList<String> tags;
    EditText tagtext;
    Button Buttonupload;
    EditText EditTextname,EditTextcontent,EditTextanswer;
    FirebaseAuth auth;
    FirebaseUser user;
    RadioGroup radioGroup;
    Button tagbutton;
    ListView lv;
    TagAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question);


        tags = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        Buttonupload = findViewById(R.id.uploadquestion);
        Buttonupload.setOnClickListener(this);
        tagbutton = findViewById(R.id.createanewtag);
        tagbutton.setOnClickListener(this);


        tagtext = findViewById(R.id.edittexttags);
        radioGroup = findViewById(R.id.radiogroup);
        EditTextname = findViewById(R.id.NameOfQuestion);
        EditTextcontent = findViewById(R.id.ContentInsideQuestion);
        EditTextanswer = findViewById(R.id.AnswerOfTheQuesiton);



        lv = findViewById(R.id.lvtags);
        adapter= new TagAdapter(this,0,0,tags);
        lv.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if(v == Buttonupload){
            String name = EditTextname.getText().toString();
            String content = EditTextcontent.getText().toString();
            String answer = EditTextanswer.getText().toString();
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (answer.isEmpty() || content.isEmpty() || name.isEmpty() || selectedId == -1){
                Toast.makeText(this,"One of your fields are empty",Toast.LENGTH_SHORT).show();
                return;
            }


            HashMap<String, Object> fullQuestion = new HashMap<>();
            fullQuestion.put("name", name);
            fullQuestion.put("author", user.getUid());
            fullQuestion.put("difficulty", 0);
            fullQuestion.put("NumAnswers", 0);
            fullQuestion.put("content", content);
            fullQuestion.put("answer",answer);
            fullQuestion.put("Tags", tags);
            if (selectedId == R.id.radiomath){
                fullQuestion.put("subject","math");
            }else if(selectedId == R.id.radiocomputerscience){
                fullQuestion.put("subject","computerscience");
            }








            Question.UploadQuestionToFirebase(fullQuestion).thenAccept(refrence ->{

                DataBaseManager.UpdateTags(refrence,tags);
            });
            User.Created(user.getUid());
            finish();

        }
        if(v == tagbutton){
            String tag = tagtext.getText().toString();
            if(tag != null){
                if(!tag.isEmpty()){
                    tagtext.setText("");
                    tags.add(tag);
                    adapter.notifyDataSetChanged();
                }
            }

        }







    }



}