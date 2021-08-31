package com.example.soberme;

import android.annotation.TargetApi;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class JournalEntryActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private TextView dateView;
    private MaterialButton backbtn,journalSubmit;
    private  SimpleDateFormat sdf;
    private TextInputEditText journalEditText;
    private TextInputLayout journalInputLayout;
    private ProgressBar progressBar;

    String currentDateTime,currentDate,currentTime;

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_entry);

        dateView=findViewById(R.id.date);
        backbtn=findViewById(R.id.back_btn);
        journalInputLayout=findViewById(R.id.journal_input_layout);
        journalEditText=findViewById(R.id.journal_edit_text);
        journalSubmit=findViewById(R.id.journal_submit);
        //progress bar
        progressBar=findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

         sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
         currentDate = sdf.format(new Date());
        sdf = new SimpleDateFormat("h:mm a");
        currentTime = sdf.format(new Date());
        sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        currentDateTime = sdf.format(new Date());
         
        dateView.setText(currentDate);

        backbtn.setOnClickListener(view->{
           finish();

        });
        journalSubmit.setOnClickListener(view->{
            submitJournal();
        });

    }
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userID = currentUser.getUid();

    }
    private void submitJournal() {
        String journalInputEntry= journalEditText.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userID = currentUser.getUid();

        if(TextUtils.isEmpty(journalInputEntry)){
            journalInputLayout.setError("cannot be empty");
        }
        else if(journalInputEntry.length()<6){
            journalInputLayout.setError("Input six or more characters");
        }else{
            progressBar.setVisibility(View.VISIBLE);

            DocumentReference docRef = fStore.collection("users").document(userID);
            docRef.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Long journalContributions=document.getLong("journalContributions")+1;
                                    String newJournalContributionsCount= String.valueOf(journalContributions);
                                    //store journal entry information
                                    Map<String, Object> journalInfo = new HashMap<>();
                                    journalInfo.put("message", journalInputEntry);
                                    journalInfo.put("date", currentDate);
                                    journalInfo.put("time",currentTime);

                                    fStore.collection("journalEntries").document(userID).collection(newJournalContributionsCount).document("journalEntry")
                                            .set(journalInfo)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                                    docRef

                                                            .update(
                                                                    "journalContributions", FieldValue.increment(1)
                                                            )

                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(JournalEntryActivity.this, "journal entry has been succesful", Toast.LENGTH_SHORT).show();

                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Error updating user details",
                                                                            Snackbar.LENGTH_SHORT)
                                                                            .show();
                                                                }
                                                            });

                                                    Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Successful journal entry",
                                                            Snackbar.LENGTH_SHORT)
                                                            .show();
                                                    startActivity(new Intent(JournalEntryActivity.this, MainActivity.class));
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error writing document", e);
                                                    Snackbar.make(findViewById(R.id.myCoordinatorLayout), "error adding journal entry",
                                                            Snackbar.LENGTH_SHORT)
                                                            .show();
                                                }
                                            });
                                }else{
                                    Toast.makeText(JournalEntryActivity.this, "unable to retrive user detialss", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    })   ;

        }
    }
}