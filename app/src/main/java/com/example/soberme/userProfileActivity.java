package com.example.soberme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class userProfileActivity extends AppCompatActivity{

    private TextView userName,emergencyContact,emailAddress,phoneNumber,resetCounter,startDate;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private FirebaseStorage fStorage;
    private StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userName=findViewById(R.id.username);
        emergencyContact=findViewById(R.id.emergency_contact);
        startDate=findViewById(R.id.start_date);
        emailAddress=findViewById(R.id.user_email);
        phoneNumber=findViewById(R.id.user_phone);
        resetCounter=findViewById(R.id.soberiety_reset);
        profileImage=findViewById(R.id.profile_image);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fStorage = FirebaseStorage.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }else {
            String userId = currentUser.getUid();
            boolean emailVerified = currentUser.isEmailVerified();
            DocumentReference docRef = fStore.collection("users").document(userId);
            docRef.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    userName.setText(document.getString("userName"));
                                    emergencyContact.setText(document.getString("emergencyContact"));
                                    startDate.setText(document.getString("startDate"));
                                    emailAddress.setText(document.getString("email"));
                                    phoneNumber.setText(document.getString("phoneNumber"));

                                    resetCounter.setText(String.valueOf(document.getLong("sobrietyResetCount")));
                                    profileImage.setImageResource(R.mipmap.ic_launcher);
                                }
                                else{
                                    Log.d(TAG, "No such document");
                                    Toast.makeText(userProfileActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Log.d(TAG, "get failed with ", task.getException());
                                Toast.makeText(userProfileActivity.this, "error encountered", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}