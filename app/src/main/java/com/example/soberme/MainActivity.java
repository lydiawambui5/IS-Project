package com.example.soberme;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;



import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private FirebaseStorage fStorage;
    private StorageReference storageRef;
    private MaterialButton addJournal, signOutBtn, resetPasswordBtn,sobrietyResetBtn,chatBtn,viewJournals;
    private FloatingActionButton floatingActionButton;
    private CircleImageView profile_image;
    private String currentDate,currentDateTime;
    private  SimpleDateFormat sdf;
    private TextView  navUsername ,username,dailySavings,totalSavings,projectedSavings,unitsNotConsumed,motivationText,dateView,soberDateCount;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_main);

        dailySavings=findViewById(R.id.daily_savings);
        totalSavings=findViewById(R.id.total_savings);
        projectedSavings=findViewById(R.id.projected_annual);
        unitsNotConsumed=findViewById(R.id.unitsNotConsumed);
        soberDateCount=findViewById(R.id.soberDaysCount);
        motivationText=findViewById(R.id.motivation_text);
        dateView=findViewById(R.id.dateView);
        profile_image=findViewById(R.id.profile_image);
        username=findViewById(R.id.username);

        floatingActionButton=findViewById(R.id.floating_action_button);


        sdf = new SimpleDateFormat("dd/MM/yyyy");
        currentDate = sdf.format(new Date());
        dateView.setText("Today is "+currentDate);
        sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        currentDateTime = sdf.format(new Date());

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fStorage = FirebaseStorage.getInstance();
        Calendar calendar = Calendar.getInstance();

        NavigationView navigationView=findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
         navUsername = (TextView) headerView.findViewById(R.id.navUserName);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                if(item.getItemId()==R.id.nav_home){

                }else if(item.getItemId()==R.id.nav_log_out){
                    logOut();
                }else if(item.getItemId()==R.id.nav_reset_password){
                    startActivity(new Intent(MainActivity.this, PasswordResetActivity.class));
                }
                else if(item.getItemId()==R.id.nav_view_account){
                    startActivity(new Intent(MainActivity.this, userProfileActivity.class));
                }
                else if(item.getItemId()==R.id.nav_add_journal){
                    startActivity(new Intent(MainActivity.this, JournalEntryActivity.class));
                }else if(item.getItemId()==R.id.nav_view_journal){
                    startActivity(new Intent(MainActivity.this, JournalEntries.class));
                }
                else if(item.getItemId()==R.id.nav_chat){
                    startActivity(new Intent(MainActivity.this, ChatActivity.class));
                }
                else if(item.getItemId()==R.id.nav_reset_sobriety){
                  resetSobriety();
                }


                DrawerLayout drawerLayout=findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });


    }

    private void resetSobriety() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.dialog_title);
        alertDialogBuilder.setMessage("Are you sure you want to reset your sobriety?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String userId = mAuth.getUid();
                        DocumentReference docRef = fStore.collection("users").document(userId);
                        docRef

                                .update(
                                        "soberDateSet", false,
                                        "sobrietyResetCount", FieldValue.increment(1)
                                )

                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(MainActivity.this, "User has reset sobriety status", Toast.LENGTH_SHORT).show();
                                        dialog.cancel();
                                        finish();
                                        startActivity(getIntent());
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
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        //Setting the title manually
        alert.show();
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }else{
            String userId = currentUser.getUid();
            boolean emailVerified = currentUser.isEmailVerified();
            DocumentReference docRef = fStore.collection("users").document(userId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            profile_image.setImageResource(R.mipmap.ic_launcher);
                            username.setText(document.getString("userName"));
                            navUsername.setText(document.getString("userName"));
                            boolean soberDateSet= document.getBoolean("soberDateSet");
                            if (soberDateSet==true){
                                Long unitPrice=document.getLong("unitPrice");
                                Long drunkDays=document.getLong("daysOfWeekDrunk");
                                Long dailyConsumption=document.getLong("unitsConsumed");
                                String emergencyContact=document.getString("emergencyContact");
                                String startDate=document.getString("startDate");
                                String soberDays="";
                                try {
                                    //quitDate=new SimpleDateFormat("dd/MM/yyyy").parse(startDate);
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                                    Date quitDate=sdf.parse(startDate);
                                    Date todayDate = sdf.parse(currentDate);
                                    long diffInMillies = Math.abs(todayDate.getTime() - quitDate.getTime());
                                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                                    soberDays=String.valueOf(diff);


                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                Long netDailySavings= unitPrice*dailyConsumption;
                                Long netProjectedAnnual= netDailySavings*52*(drunkDays);
                                int soberDaysValue=Integer.parseInt(soberDays);
                                int soberWeeks = (int) Math.floor(soberDaysValue / 7);
                                int soberDaysRem=soberDaysValue%7;
                                Long netSavings=soberWeeks*netDailySavings*drunkDays;
                                Long netUnitsNotConsumed=soberWeeks*drunkDays*dailyConsumption;
                                if(soberDaysRem>=drunkDays){
                                    netSavings=netSavings+(drunkDays*netDailySavings);
                                    netUnitsNotConsumed += (drunkDays * dailyConsumption);
                                }

                                unitsNotConsumed.setText(String.valueOf(netUnitsNotConsumed));
                                projectedSavings.setText(String.valueOf(netProjectedAnnual));
                                soberDateCount.setText((String.valueOf(soberDaysValue))+" days");
                                dailySavings.setText(String.valueOf(netDailySavings));
                                totalSavings.setText(String.valueOf(netSavings));
                                floatingActionButton.setOnClickListener(view->{
                                    Uri u= Uri.parse("tel:"+emergencyContact);
                                    Intent i = new Intent(Intent.ACTION_DIAL, u);
                                    try
                                    {
                                        // Launch the Phone app's dialer with a phone
                                        // number to dial a call.
                                        startActivity(i);
                                    }
                                    catch (SecurityException s)
                                    {
                                        // show() method display the toast with
                                        // exception message.
                                        Toast.makeText(MainActivity.this, "Unable to open dialer", Toast.LENGTH_LONG)
                                                .show();
                                    }
                                });

                            }else{
                                startActivity(new Intent(MainActivity.this, SobrietyDetails.class));
                                Toast.makeText(MainActivity.this, "please set sober date", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.d(TAG, "No such document");

                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());

                    }
                }
            });
        }
    }

}