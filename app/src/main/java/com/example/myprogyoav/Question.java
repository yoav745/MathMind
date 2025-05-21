package com.example.myprogyoav;

import android.content.Context;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Question {
    private final String TAG = "Question";
    private Context context;
    private String name;
    private double difficultyrating;
    private double funrating;
    private long counter = -1;
    private List<HashMap<String,Object>> answers;
    private ArrayList<Question> arrayList;
    private String referance = "";
    private ListView lv;
    private String author;
    private String content;
    private String answer;
    private String subject;
    Runnable inside;
    DocumentSnapshot document = null;

    private void AddSolver(Answer ans){

        ChangeRatings(ans.GetdiffRating(), ans.GetFunRating());






        GetSolvedList().thenApply(list ->{

            list.add(ans.GetUser());

            FirebaseFirestore db;
            db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("questions").document(this.referance);
            docRef.update("Solvers",list);
            return null;
        });



    }
    private CompletableFuture<ArrayList<String>> GetSolvedList(){
        {
            CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();
            GetQuestionDocumentData().thenAccept(document ->{
                if (document != null && document.exists()) {
                    ArrayList<String> solved = (ArrayList<String>)document.get("Solvers");
                    future.complete(solved);
                }
            });
            return future;
        }
    }
    public CompletableFuture<Boolean> IsSolved(String userid){
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        GetSolvedList().thenApply(list -> {
            if(list.contains(userid)) {
                future.complete(true);
            }
            else{
                future.complete(false);
            }
            return null;
        });
        return future;
    }
    private void ChangeRatings(double DiffRating,double FunRating){

        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("questions").document(this.referance);

        GetQuestionDocumentData().thenAccept(document ->{
            Long newSolvers = document.getLong("NumSolved") + 1;
            double NewDiffRating = ((document.getDouble("difficultyRating") * (newSolvers-1) + DiffRating)) / newSolvers;
            double NewFunRating = ((document.getDouble("IntrestRating") * (newSolvers-1) + FunRating)) / newSolvers;

            docRef.update("NumSolved",newSolvers);
            docRef.update("difficultyRating",NewDiffRating);
            docRef.update("IntrestRating",NewFunRating);


        });
    }



    private CompletableFuture<DocumentSnapshot> GetQuestionDocumentData(){
        CompletableFuture<DocumentSnapshot> future = new CompletableFuture<>();


        if (this.document !=null){
            future.complete(this.document);
        }

        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();
        if (!referance.isEmpty()) {

            DocumentReference docRef = db.collection("questions").document(this.referance);

            // Fetch document
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    future.complete(document);

                }

            });
        }
        return future;
    }



    public void UploadAnswer(Answer answerfromuser) {
        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("questions").document(this.referance);

        GetQuestionDocumentData().thenAccept(document ->{
            if (document != null && document.exists()) {
                // Initialize the answer list
                ArrayList<HashMap<String, Object>> answerList;

                if (document.contains("answers")) {
                    // Get the existing answers
                    answerList = (ArrayList<HashMap<String, Object>>) document.get("answers");
                } else {
                    // Create a new list if "answers" field doesn't exist
                    answerList = new ArrayList<>();
                }

                HashMap<String, Object> map = new HashMap<>();
                map.put("content", answerfromuser.GetDescription());
                map.put("user", answerfromuser.GetUser());

                // Add the new answer to the list
                answerList.add(map);

                docRef.update("answers", answerList)
                        .addOnSuccessListener(aVoid -> {
                            System.out.println("Answer added successfully!");
                            AddSolver(answerfromuser);
                        })
                        .addOnFailureListener(e -> {
                            System.err.println("Error adding answer: " + e.getMessage());
                        });


            };
        });
    }















    public Question(HashMap<String,Object> map){

        /*
                            map.put("Refrence" , document.getId());
                            map.put("Subject" , document.getString("subject"));
                            map.put("Author" , document.getString("author"));
                            map.put("Content" , document.getString("content"));
                            map.put("Counter" , document.getLong("counter"));
                            map.put("DifficultyRating" , document.getLong("difficultyRating"));
                            map.put("Name" , document.getString("name"));
                            map.put("FunRating",document.getDouble("IntrestRating"));

         */




        this.subject = (String)map.get("Subject");
        this.author = (String)map.get("Author");
        this.content =(String)map.get("Content");
        this.counter = (Long)map.get("Counter");
        this.funrating = (Double) map.get("FunRating");
        this.name = (String)map.get("Name");
        this.difficultyrating = (Double)map.get("DifficultyRating");
        this.referance = (String)map.get("Refrence");
    }
    public Question(String refrance,Runnable func){
        this.inside = func;
        this.referance = refrance;
        getDataFromFirestore();

    }
    public Question(String referance, AtomicInteger atomicInteger){
        this.referance = referance;
        Thread thread = new Thread(() ->{

            getDataFromFirestore().thenAccept(bool ->{
                synchronized (atomicInteger){
                    atomicInteger.incrementAndGet();
                    atomicInteger.notify();
                }

            });

        });
        thread.start();


    }









    private void LoadListView(){

        QuestionAdapter questionAdapter = new QuestionAdapter(context,0,0,arrayList);
        lv.setAdapter(questionAdapter);
        Log.d("Adapter", "creating adapter");

    }
    public List<Answer> GetAnswers(){

        ArrayList<Answer> toreturn = new ArrayList<>();
        if(answers == null){
            return toreturn;
        }
        for (HashMap<String,Object> map :answers) {
            toreturn.add(new Answer(map));

        }



        return toreturn;

    }
    public String GetSubject(){return this.subject;}
    public long GetCounter(){return this.counter;}
    public String GetName(){return this.name;}
    public double GetDifficultyrating(){return this.difficultyrating;}
    public double GetFunrating(){return this.funrating;}
    public String GetReferance(){return this.referance;}
    public String GetAuthorId(){return this.author;}
    public String getContent() {return this.content;}
    public boolean CheckAnswer(String Answer){
        return Answer.equals(answer);
    }

    @NonNull
    @Override
    public String toString() {
        return this.referance+" " + this.counter;
    }
    private CompletableFuture<Boolean> getDataFromFirestore() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();
        if (!referance.isEmpty()){

            DocumentReference docRef = db.collection("questions").document(this.referance); // Replace with your collection and document ID

            // Fetch document
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {


                        this.subject = document.getString("subject");
                        this.author = document.getString("author");
                        this.content = document.getString("content");
                        this.counter = document.getLong("counter");
                        this.difficultyrating = document.getDouble("difficulty");
                        this.name = document.getString("name");
                        this.answer = document.getString("answer");
                        this.answers = (List<HashMap<String,Object>>) document.get("answers");
                        future.complete(true);
                        if (inside!=null){
                            new Handler(Looper.getMainLooper()).post(inside);
                        }
                    }

                }
            });

        }
        else if(this.counter!=-1){
            db.collection("questions")  // Replace "users" with your collection name
                    .whereEqualTo("counter", this.counter)  // Field name 'counter' and value to match
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    this.author = document.getString("author");
                                    this.content = document.getString("content");
                                    this.counter = document.getLong("counter");
                                    this.funrating = document.getDouble("IntrestRating");
                                    this.difficultyrating = document.getDouble("difficultyRating");
                                    this.name = document.getString("name");
                                }
                                arrayList.add(this);
                                LoadListView();
                            } else {
                                Log.d("Firestore", "No document found with the given counter value");
                            }
                        } else {
                            Log.d("Firestore", "Error getting documents: ", task.getException());
                        }
                    });
        }




        return future;
    }




    // Statics




    public static CompletableFuture<Boolean> FetchQuestions(QuestionAdapter adapter,ArrayList<Question> questions){
        questions.clear();
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        FirebaseFirestore db;
        int counting = 0;
        db = FirebaseFirestore.getInstance();
        db.collection("questions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            HashMap<String,Object> map = new HashMap<>();

                            map.put("Refrence" , document.getId());
                            map.put("Subject" , document.getString("subject"));
                            map.put("Author" , document.getString("author"));
                            map.put("Content" , document.getString("content"));
                            map.put("Counter" , document.getLong("counter"));
                            map.put("DifficultyRating" , document.getDouble("difficultyRating"));
                            map.put("Name" , document.getString("name"));
                            map.put("FunRating",document.getDouble("IntrestRating"));
                            // Add the fetched data to the list
                            questions.add(new Question(map));
                        }

                        // Notify the adapter that the data has changed
                        adapter.notifyDataSetChanged();
                        future.complete(true);
                        // Notify the correct adapter
                    } else {
                        Log.e("FetchQuestions", "Error fetching questions", task.getException());
                    }
                });
        return future;

    }




    public static void FilterQuestions(QuestionAdapter adapter,ArrayList<Question> questions,ArrayList<Question> filtered,String type){
        filtered.clear();
        adapter.notifyDataSetChanged();

        for (Question question:questions) {
            Log.d("TYPE", question.GetSubject());
            if (question.GetSubject().equals(type)) {
                filtered.add(question);
            }

        }
        adapter.notifyDataSetChanged();
    }




    public static CompletableFuture<String> UploadQuestionToFirebase(HashMap<String,Object> map){
        CompletableFuture<String> future = new CompletableFuture<>();

        map.put("Answers", new ArrayList<HashMap<String,Object>>());
        map.put("Solvers",new ArrayList<String>());
        map.put("NumSolved",0);
        map.put("IntrestRating",0);
        map.put("difficultyRating",0);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference counterRef = db.collection("count").document("counter");



        counterRef.update("counter", FieldValue.increment(1));
        db.collection("count").document("counter").get()
                .addOnSuccessListener(documentSnapshot ->{

                    if(documentSnapshot.exists()){
                        String temp = documentSnapshot.get("counter").toString();
                        map.put("counter",Long.parseLong(temp));
                        UploadHashMap(map,future);
                    }
                });

        return future;
    }




private static void UploadHashMap(HashMap<String,Object> map,CompletableFuture<String> future){
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection("questions")
            .add(map).addOnSuccessListener(refrence ->{
                future.complete(refrence.getId().toString());
            });

}



}
