package com.example.soberme;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class ChatActivity extends AppCompatActivity {
    private CircleImageView profile_image;
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private TextView userName,dateView,chatMessageCount;
    private SimpleDateFormat sdf;
    private TextInputEditText message;
    private TextInputLayout messageInputLayout;
    private MaterialButton messageSubmit;
    private String username;
    String currentDateTime,currentDate,currentTime;
    private  Long    totalChats;
    private LinearLayout messagesView;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dateView=findViewById(R.id.date);

        userName=findViewById(R.id.username);
        profile_image=findViewById(R.id.profile_image);
        messageSubmit=findViewById(R.id.message_submit);
        messageInputLayout=findViewById(R.id.message_input_layout);
        message=findViewById(R.id.message_edit_text);
        chatMessageCount=findViewById(R.id.chat_messages_count);
        messagesView=findViewById(R.id.messages_view);

        sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
        currentDate = sdf.format(new Date());
        sdf = new SimpleDateFormat("h:mm a");
        currentTime = sdf.format(new Date());
        sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        currentDateTime = sdf.format(new Date());

        dateView.setText(currentDate);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        
        messageSubmit.setOnClickListener(view->{
            messageSubmit();
        });

    }

    private void messageSubmit() {
        String messageText=message.getText().toString().trim();
        if(TextUtils.isEmpty(messageText)){
            messageInputLayout.setError("message cannot be empty");
        }else if(messageText.length()<6) {
            messageInputLayout.setError("Input six or more characters");
        }
        else{
            Map<String, Object> messageInfo = new HashMap<>();
            messageInfo.put("messageText",messageText);
            messageInfo.put("dateTime",currentDateTime);
            messageInfo.put("user",username);
            totalChats=totalChats+1;
            String chatCount=String.valueOf(totalChats);

            fStore.collection("chat").document("mainChatStream").collection(chatCount).document("chatMessage")
            .set(messageInfo)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    DocumentReference docRef = fStore.collection("chat").document("mainChatStream");
                    docRef

                            .update(
                                    "chatMessageCount", FieldValue.increment(1)
                            );
                    finish();
                    startActivity(getIntent());
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, "Unable to send message", Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();


        if (currentUser == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        else{
            String userId = currentUser.getUid();
            DocumentReference docRef = fStore.collection("users").document(userId);
            docRef.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    profile_image.setImageResource(R.mipmap.ic_launcher);
                                    userName.setText(document.getString("userName"));
                                    username=document.getString("userName");
                                }else{
                                    Toast.makeText(ChatActivity.this, "Unable to retrive user details", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
            DocumentReference chatDocRef =fStore.collection("chat").document("mainChatStream");
            chatDocRef.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull  Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    chatMessageCount.setText(String.valueOf(document.getLong("chatMessageCount"))+"total message(s)");
                                    totalChats=document.getLong("chatMessageCount");
                                    int totalChatsount=totalChats.intValue();
                                    for (int i=1;i<=totalChatsount;i++){
                                        DocumentReference messagesDocRef= fStore.collection("chat").document("mainChatStream").collection(String.valueOf(i)).document("chatMessage");
                                        messagesDocRef.get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            if (document.exists()) {
                                                                TextView sender = new TextView(ChatActivity.this);
                                                                sender.setText(document.getString("user"));
                                                                sender.setTextColor(ChatActivity.this.getResources().getColor(R.color.teal_700));
                                                                sender.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                ((LinearLayout) messagesView).addView(sender);

                                                                TextView valueTV = new TextView(ChatActivity.this);
                                                                valueTV.setText(document.getString("messageText"));
                                                                valueTV.setPadding(20,20,0,20);
                                                                valueTV.setTextColor(ChatActivity.this.getResources().getColor(R.color.black));
                                                                valueTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                ((LinearLayout) messagesView).addView(valueTV);

                                                                TextView timeTV = new TextView(ChatActivity.this);
                                                                timeTV.setText(document.getString("dateTime"));
                                                                timeTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                                                ((LinearLayout) messagesView).addView(timeTV);

                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                }else{
                                    Toast.makeText(ChatActivity.this, "unable to retrive chat details", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

        }
    }
}