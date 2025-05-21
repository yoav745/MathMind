package com.example.myprogyoav;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.LogDescriptor;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

public class User {

    private String Base64Image;
    private String userid;

    private String name;

    private Long Solved;

    private Long Created;
    
    private String TAG = "USER";

    public User(String userid){
        this.userid = userid;
    }
    public CompletableFuture<Integer> LoadDataFromFirebase(){
        CompletableFuture<Integer> future = new CompletableFuture<>();

        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(this.userid);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                     Base64Image = document.getString("profileImage");
                     name = document.getString("fName");
                     Solved = document.getLong("SolvedQuestions");
                     Created = document.getLong("QuestionsCreated");
                     future.complete(new Integer(1));
                }

            }
        });


        return future;
    }


    public Bitmap GetImage(){
        if(Base64Image !=null){
            byte[] decodedBytes = Base64.decode(Base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            return bitmap;
        }
        else{
            return null;
        }
    }
    public String GetUserID(){return this.userid;}
    public String GetName(){return this.name;}
    public Long GetSolved(){return this.Solved;}
    public Long GetCreated(){return this.Created;}

    public void Solved(){

        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(this.userid);

        // Increment the "Solved" field by 1
        docRef.update("SolvedQuestions", FieldValue.increment(1));

    }
    public void Created(){

        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(this.userid);

        // Increment the "Solved" field by 1
        docRef.update("QuestionsCreated", FieldValue.increment(1));

    }
    public static void Created(String Userid){
        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(Userid);

        // Increment the "Solved" field by 1
        docRef.update("QuestionsCreated", FieldValue.increment(1));

    }
    public static void Solved(String Userid){

        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(Userid);

        // Increment the "Solved" field by 1
        docRef.update("SolvedQuestions", FieldValue.increment(1));

    }



}
