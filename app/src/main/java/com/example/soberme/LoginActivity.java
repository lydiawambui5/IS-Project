package com.example.soberme;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etLoginEmail,etLoginPassword;
    private TextInputLayout emailInputLayout,passwordInputLayout;
    private MaterialButton btnLogin,forgotPassword;
    private ProgressBar progressbar;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //inputLayouts
        emailInputLayout=findViewById(R.id.email_input_layout);
        passwordInputLayout=findViewById(R.id.password_input_layout);
        //inputs
        etLoginEmail=findViewById(R.id.email_input_text);
        etLoginPassword=findViewById(R.id.password_edit_text);
        //buttons
        btnLogin=findViewById(R.id.btnLogin);
        forgotPassword=findViewById(R.id.forgot_password);
        //progress bar
        progressbar=findViewById(R.id.progressBar);

        mAuth=FirebaseAuth.getInstance();

        etLoginEmail.setOnClickListener(view->{
            emailInputLayout.setError(null);
        });
        etLoginPassword.setOnClickListener(view->{
            passwordInputLayout.setError(null);
        });

        btnLogin.setOnClickListener(view->{
            loginUser();
        });
        forgotPassword.setOnClickListener(view->{
            startActivity(new Intent(this,PasswordResetActivity.class));
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void loginUser(){
        String email=etLoginEmail.getText().toString().trim();
        String password= etLoginPassword.getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            emailInputLayout.setError("email cannot be empty");
            etLoginEmail.requestFocus();
        }else if(TextUtils.isEmpty(password)){
            passwordInputLayout.setError("password cannot be empty");
            etLoginPassword.requestFocus();
        }else{
            progressbar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        boolean emailVerified = currentUser.isEmailVerified();
                        if(emailVerified==true){
                            Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Successful login",
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                            finish();
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        }else if(emailVerified==false){
                            Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Verify email first",
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                            progressbar.setVisibility(View.INVISIBLE);
                        }


                    }
                    else{
                        progressbar.setVisibility(View.INVISIBLE);
//                        Toast.makeText(LoginActivity.this, "login Error:"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        try {
                            throw task.getException();
                        }  catch(FirebaseAuthInvalidCredentialsException e) {
                            Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Invalid login credentials",
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                        } catch (FirebaseAuthInvalidUserException e){
                            Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Register with us first",
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                        catch(Exception e) {
                            Snackbar.make(findViewById(R.id.myCoordinatorLayout), "login error:"+task.getException().getMessage(),
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }
            });
        }
    }
    public void registerOpen(View view){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
}