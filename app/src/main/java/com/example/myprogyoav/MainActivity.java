package com.example.myprogyoav;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth auth;
    ArrayList<Question> questions , filtered;
    ListView lv;
    RadioGroup radioGroup;
    QuestionAdapter adapter; // Use this consistently
    FirebaseFirestore db;
    ImageButton sendtoprofile;
    Button tocreatequestion;
    Button search;
    EditText searchtext;
    String sub;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sub = "math";
        searchtext = findViewById(R.id.searchtext);
        search = findViewById(R.id.buttontosearch);
        search.setOnClickListener(this);
        // Check if user is logged in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Intent loginIntent = new Intent(MainActivity.this, Login.class);
            startActivity(loginIntent);
            finish();
        }

        // Initialize questions list and adapter
        questions = new ArrayList<>();
        filtered = new ArrayList<>();
        lv = findViewById(R.id.qlv);
        adapter = new QuestionAdapter(this, 0, 0, filtered);
        adapter.notifyDataSetChanged();
        lv.setAdapter(adapter);
        radioGroup = findViewById(R.id.radiogroupsort);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checked = radioGroup.getCheckedRadioButtonId();
                String sub = null;
                if(checked == R.id.sortmath){
                    sub = "math";
                } else if(checked == R.id.sortcs){
                    sub = "computerscience";
                }

                Question.FilterQuestions(adapter,questions,filtered,sub);
            }
        });

        // Fetch questions from Firestore
        Question.FetchQuestions(adapter,questions).thenApply(boo ->{

            Question.FilterQuestions(adapter,questions,filtered,sub);
            return null;
        });

        tocreatequestion = findViewById(R.id.ToCreatingQuestions);
        tocreatequestion.setOnClickListener(this);
        sendtoprofile = findViewById(R.id.toeditprofile);
        sendtoprofile.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                this.startActivity(intent);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show();
                DataBaseManager.CreateNotification(this);
            } else {
                Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == sendtoprofile) {
            Intent intent = new Intent(MainActivity.this, EditProfile.class);
            startActivity(intent);
        }
        if (v == tocreatequestion) {
            Intent intent = new Intent(MainActivity.this, CreateQuestion.class);
            startActivity(intent);
        }
        if (v == search){
            String tag = searchtext.getText().toString();
            new Thread(() -> {
                if (tag != null && !tag.isEmpty()) {
                    DataBaseManager.SearchQuestionsByTag(tag, filtered).thenAccept(bool -> {
                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            hideKeyboard(MainActivity.this);
                        });
                    });
                } else {
                    Question.FetchQuestions(adapter, questions).thenApply(boo -> {
                        runOnUiThread(() -> {
                            Question.FilterQuestions(adapter, questions, filtered, sub);
                            adapter.notifyDataSetChanged();
                        });
                        return null;
                    });
                }
            }).start();

        }
    }
    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Get the root view
        View view = activity.findViewById(android.R.id.content);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }







}
