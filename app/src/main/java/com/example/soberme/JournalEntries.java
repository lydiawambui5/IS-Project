package com.example.soberme;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class JournalEntries<currentUser> extends AppCompatActivity {
    private FirebaseFirestore fStore;
    private TextView journalEntriesCount;
    private FirebaseAuth mAuth;
    private CircleImageView profile_image;
    private TextView username;
    private LinearLayout journalCardView;
    private View linearLayout;
    private int journalContributions;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_entries);

        mAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        journalEntriesCount=findViewById(R.id.journal_entries_count);
        profile_image=findViewById(R.id.profile_image);
        username=findViewById(R.id.username);
        journalCardView=findViewById(R.id.journal_cards_view);
         linearLayout =  findViewById(R.id.journal_cards_view);




    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        DocumentReference docRef = fStore.collection("users").document(userId);
        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                profile_image.setImageResource(R.mipmap.ic_launcher);
                                username.setText(document.getString("userName"));
                                journalEntriesCount.setText(String.valueOf(document.getLong("journalContributions"))+" journal entries");
                                journalContributions=(document.getLong("journalContributions")).intValue();
                            }
                        }else{
                            Log.d(TAG, "No such document");

                        }

                    }
                });
        DocumentReference journalRef = fStore.collection("journalEntries").document(userId);
        journalRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                int journalContiributionsCount=journalContributions;
                                for (int i=1;i<=journalContiributionsCount;i++){
                                    DocumentReference journalEntryRef = fStore.collection("journalEntries").document(userId).collection(String.valueOf(i)).document("journalEntry");
                                    journalEntryRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {

                                                    TextView journalDate = new TextView(JournalEntries.this);
                                                    journalDate.setText(document.getString("date"));
                                                    journalDate.setTextColor(JournalEntries.this.getResources().getColor(R.color.black));
                                                    journalDate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                    ((LinearLayout) linearLayout).addView(journalDate);

                                                    TextView valueTV = new TextView(JournalEntries.this);
                                                    valueTV.setText(document.getString("message"));
                                                    valueTV.setPadding(20,20,0,20);     
                                                    valueTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                    ((LinearLayout) linearLayout).addView(valueTV);

                                                }else{
                                                    TextView valueTV = new TextView(JournalEntries.this);
                                                    valueTV.setText("unable to retrive this entry");
                                                    valueTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                    ((LinearLayout) linearLayout).addView(valueTV);
                                                }
                                            }
                                        }
                                    });

                                }
                            }
                        }else{
                            Log.d(TAG, "No such document");
                            journalEntriesCount.setText("No Journal entries from user");
                        }
                    }
                });
    }


}