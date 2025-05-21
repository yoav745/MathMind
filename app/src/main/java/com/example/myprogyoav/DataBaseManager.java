package com.example.myprogyoav;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.google.api.LogDescriptor;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

public class DataBaseManager {

    public static void CreateNotification(Context context) {

        // 1. Create notification channel (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "dailyNotify",
                    "Daily Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 2. Set alarm time to 4 PM today or tomorrow if passed
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 16); // 4 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If 4 PM already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // 3. Prepare intent & pending intent to trigger NotificationReceiver
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 4. Schedule repeating alarm every 24 hours (no exact alarm permission required)
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }






    public DataBaseManager(){

    }
    public static void UpdateTags(String questionrefrence, ArrayList<String> tags) {

        Thread thisthread = new Thread(() -> {

            for (String tag : tags) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docref = db.collection("Tags").document("FP8j47QkR2K8Y7H5s4D5");

                docref.get().addOnSuccessListener(Documentsnapshot -> {
                    ArrayList<String> currenttag = (ArrayList<String>)Documentsnapshot.get(tag);


                    if (currenttag == null){
                        ArrayList<String> arr = new ArrayList<>();
                        arr.add(questionrefrence);
                        docref.update(tag,arr);
                    }
                    else if (currenttag.isEmpty()){
                        ArrayList<String> arr = new ArrayList<>();
                        arr.add(questionrefrence);
                        docref.update(tag,arr);
                    }
                    else{
                        currenttag.add(questionrefrence);

                        docref.update(tag,currenttag);
                    }
                    Log.d("hello", tag);


                });

            }


        });

        thisthread.start();
    }
    public static CompletableFuture<Boolean> SearchQuestionsByTag(String tag,ArrayList<Question> questions){
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        questions.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AtomicInteger sum = new AtomicInteger(0);
        db.collection("Tags").document("FP8j47QkR2K8Y7H5s4D5").get().addOnSuccessListener(Documentsnapshot ->{
            ArrayList<String> arr = (ArrayList<String>) Documentsnapshot.get(tag);
            if(arr == null){
                questions.clear();
                return;

            }
            Thread thread = new Thread(() ->{
               synchronized (sum){
                   while(sum.get() != arr.size()){
                       try {
                           sum.wait();

                       } catch (InterruptedException e) {
                           Thread.currentThread().interrupt();
                       }

                       future.complete(true);

                   }
               }
            });
            thread.start();
            if (arr != null){
                for (String temptag : arr){
                    questions.add(new Question(temptag,sum));
                }
            }


        });


        return future;

    }
}



