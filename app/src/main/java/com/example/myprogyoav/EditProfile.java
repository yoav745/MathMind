package com.example.myprogyoav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    EditText profileFullName;
    ImageView profileImageView;
    Button saveBtn;
    Button signout;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent data = getIntent();
        final String fullName = data.getStringExtra("fullName");

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();

        profileFullName = findViewById(R.id.profileFullName);
        profileImageView = findViewById(R.id.profileImageView);
        saveBtn = findViewById(R.id.saveProfileInfo);
        signout = findViewById(R.id.signout);


        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fAuth.signOut();
            }
        });
        // Set current data in the views
        profileFullName.setText(fullName);

        // If the user has an image in Firestore, decode and set it in ImageView
        loadImageFromFirestore();

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

        saveBtn.setOnClickListener(v -> {
            String updatedName = profileFullName.getText().toString().trim();

            if (updatedName.isEmpty()) {
                Toast.makeText(EditProfile.this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Firestore with the new name
            updateNameInFirestore(updatedName);
        });
    }

    private void updateNameInFirestore(String updatedName) {
        // Get the Firestore document reference for the current user
        DocumentReference docRef = fStore.collection("users").document(user.getUid());

        // Create a map to store the new name
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("fullName", updatedName);

        // Update Firestore with the new name
        docRef.update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfile.this, "Name updated successfully!", Toast.LENGTH_SHORT).show();
                    // Optionally navigate to another activity or finish the current one
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfile.this, "Failed to update name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadImageToFirestore(imageUri);
        }
    }

    private void uploadImageToFirestore(Uri imageUri) {
        try {
            // Convert URI to Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Convert Bitmap to Base64 string
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Save encoded image to Firestore
            DocumentReference docRef = fStore.collection("users").document(user.getUid());
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("profileImage", encodedImage);

            docRef.update(imageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(EditProfile.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfile.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadImageFromFirestore() {
        DocumentReference docRef = fStore.collection("users").document(user.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String encodedImage = documentSnapshot.getString("profileImage");
                if (encodedImage != null) {
                    byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    profileImageView.setImageBitmap(decodedImage);
                }
                else{
                    profileImageView.setImageResource(R.drawable.ic_person_black_24dp);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(EditProfile.this, "Failed to load image", Toast.LENGTH_SHORT).show();
        });
    }
}
