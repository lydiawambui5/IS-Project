package com.example.soberme;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private String userID,userEmail;
    private TextInputEditText etRegEmail, etRegPassword,etRegUserName,etPhoneNumber;
    private TextInputLayout emailInputLayout,userNameInputLayout,phoneInputLayout,passwordInputLayout;
    private Button registerBtn;
     private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //buttons
        registerBtn=findViewById(R.id.sign_up_btn);
        //input
        etRegEmail=findViewById(R.id.regEmail);
        etRegPassword=findViewById(R.id.regPassword);
        etRegUserName=findViewById(R.id.regUserName);
        etPhoneNumber=findViewById(R.id.regPhone);
        //input layouts
        emailInputLayout=findViewById(R.id.email_input_layout);
        userNameInputLayout=findViewById(R.id.userNameInputLayout);
        phoneInputLayout=findViewById(R.id.phone_input_layout);
        passwordInputLayout=findViewById(R.id.password_input_layout);

        //progress bar
        progressBar=findViewById(R.id.progressBar);

        mAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();


        registerBtn.setOnClickListener(view->{
            createUser();
        });
    }
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
    public void loginOpen(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    private void createUser(){
        String email=etRegEmail.getText().toString().trim();
        String password= etRegPassword.getText().toString().trim();
        String userName = etRegUserName.getText().toString().trim();
        String phone=etPhoneNumber.getText().toString();

        if(TextUtils.isEmpty(email)){
            emailInputLayout.setError("Email cannot be empty");
            etRegEmail.requestFocus();
        }else if(TextUtils.isEmpty(password)){
            passwordInputLayout.setError("password cannot be empty");
            etRegPassword.requestFocus();
        }else if(password.length()<6){
            passwordInputLayout.setError("password needs to be six characters or more");
            etRegPassword.requestFocus();
        }else if(TextUtils.isEmpty(phone)){
            phoneInputLayout.setError("username cannot be empty");
            etPhoneNumber.requestFocus();
        }else if(TextUtils.isEmpty(userName)){
            userNameInputLayout.setError("Username cannot be empty");
            etRegUserName.requestFocus();
        }
        else if(userName.length()<2){
            userNameInputLayout.setError("user name needs to be two characters or more");
            etRegUserName.requestFocus();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){
                        userID= mAuth.getCurrentUser().getUid();
                        userEmail=mAuth.getCurrentUser().getEmail();
                        //store user information
                        Map<String, Object> user = new HashMap<>();
                        user.put("userName", userName);
                        user.put("email", userEmail);
                        user.put("phoneNumber",phone);
                        user.put("soberDateSet",false);
                        user.put("journalContributions",0);
                        fStore.collection("users").document(userID)
                                .set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                        //send verification email after successful registration
                                        mAuth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Email sent.");
                                                    Toast.makeText(SignUpActivity.this, "User registered successfully"+userEmail, Toast.LENGTH_SHORT).show();
                                                    finish();
                                                    startActivity(new Intent(SignUpActivity.this,LoginActivity.class));
                                                }
                                                else{
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(SignUpActivity.this, "Unable to register user", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });
                    }
                    else{
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SignUpActivity.this, "registration Error:"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            });
        }
    }
}