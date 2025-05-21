package com.example.myprogyoav;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class Answer {
    private String Description;
    private User User;
    private String user;
    private Double DiffRating;
    private Double FunRating;

    private CompletableFuture<Integer> IsUserReady;

    public Answer(HashMap<String,Object> map){
        this.Description = map.get("content").toString();
        this.User = new User(map.get("user").toString());
        this.user = map.get("user").toString();
        if (map.get("IntrestRating") != null){
            this.FunRating =  Double.parseDouble(map.get("IntrestRating").toString());
        }
        if (map.get("difficultyRating") != null){
            this.DiffRating =  Double.parseDouble(map.get("difficultyRating").toString());
        }


        this.IsUserReady = new CompletableFuture<>();
        User.LoadDataFromFirebase().thenAccept(number ->{
            IsUserReady.complete(number);
        });

    }
    public User GetClassUser(){
        return User;
    }
    public Answer(String description,String user,Double Fun,Double Diff){
        this.Description = description;
        this.user = user;
        this.FunRating = Fun;
        this.DiffRating = Diff;
    }


    public String GetDescription(){return this.Description;}
    public String GetUser(){return this.user;}

    public CompletableFuture<Integer> PrepareUser(){
        return IsUserReady;
    }
    public double GetdiffRating(){return this.DiffRating;}
    public double GetFunRating(){return this.FunRating;}
    public Bitmap GetImage(){
        return User.GetImage();
    }
    public String GetName(){
        return User.GetName();
    }
}
