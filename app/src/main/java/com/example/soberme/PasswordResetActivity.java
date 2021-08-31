package com.example.soberme;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.content.ContentValues.TAG;

public class PasswordResetActivity extends AppCompatActivity {
    private TextView passwordResetHeader;
    private TextInputEditText userEmail;
    private TextInputLayout emailInputLayout;
    private String forgotPassword;
    private LinearProgressIndicator progressBar;
    private MaterialButton backBtn, resetButton;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        //inputLayouts
        emailInputLayout=findViewById(R.id.email_input_layout);
        //labels
        forgotPassword=getString(R.string.reset_password);
        passwordResetHeader=findViewById(R.id.password_reset_header);
        //inputs
        userEmail=findViewById(R.id.user_email);
        //buttons
        resetButton=findViewById(R.id.reset_password_btn);
        backBtn=findViewById(R.id.back_btn);
        //progress bar
        progressBar=findViewById(R.id.progressBar);

        mAuth=FirebaseAuth.getInstance();

        backBtn.setOnClickListener(view->{
            finish();
        });
        userEmail.setOnClickListener(view->{
            emailInputLayout.setError(null);
        });
        resetButton.setOnClickListener(view->{
            passwordResetEmail();
        });

    }

    private void passwordResetEmail() {
        String emailAddress = userEmail.getText().toString().trim();
        if(TextUtils.isEmpty(emailAddress)){
            emailInputLayout.setError("Email address cannot be empty");
            userEmail.requestFocus();
        }else if(emailAddress.length()<6){
            emailInputLayout.setError("email address cannot be less than six characters");
            userEmail.requestFocus();
        }else if(mAuth.getCurrentUser()!=null && !mAuth.getCurrentUser().getEmail().toLowerCase().equals(emailAddress.toLowerCase())){
            emailInputLayout.setError("Insert email address for currently logged in user");
            userEmail.requestFocus();
        }else {
            progressBar.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
//                                Toast.makeText(PasswordResetActivity.this, "Password reset email sent to " + emailAddress, Toast.LENGTH_SHORT).show();
                                Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Email sent",
                                        Snackbar.LENGTH_SHORT)
                                        .show();
                                finish();
                            }else {
                                Log.d(TAG,"Email not sent");
                                progressBar.setVisibility(View.INVISIBLE);
                                Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Email sent",
                                        Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            passwordResetHeader.setText(forgotPassword);
        }
    }
}