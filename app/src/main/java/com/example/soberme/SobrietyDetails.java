package com.example.soberme;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class SobrietyDetails extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener  {
    private TextInputEditText quitDate,dailyConsumedUnits,emergencyContact,unitCost,drinkingDays;
    private TextInputLayout quitDateInputLayout,drinkingDaysInputLayout,dailyConsumedUnitsInputLayout,emergencyContactInputLayout,unitCostInputLayout;
    private MaterialButton submitSobrietyDetails;
    private FirebaseFirestore fStore;
    private FirebaseAuth mAuth;
    private int mYear;
    private int mDay;
    private int mMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobriety_details);

        mAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        quitDate=findViewById(R.id.quitDateEditText);
        quitDateInputLayout=findViewById(R.id.quitDateInputLayout);
        dailyConsumedUnits=findViewById(R.id.dailyUnitsEditText);
        dailyConsumedUnitsInputLayout=findViewById(R.id.dailyUnitsInputLayout);
        unitCost=findViewById(R.id.unitcostEditText);
        unitCostInputLayout=findViewById(R.id.unitCostInputLayout);
        emergencyContact=findViewById(R.id.emergencyContactEditText);
        emergencyContactInputLayout=findViewById(R.id.emergencyContactInputLayout);
        drinkingDaysInputLayout=findViewById(R.id.drinkingDaysInputLayout);
        drinkingDays=findViewById(R.id.drinkingDaysEditText);
        submitSobrietyDetails=findViewById(R.id.sober_submit);

        submitSobrietyDetails.setOnClickListener(this);
        quitDate.setOnClickListener(this);
        quitDateInputLayout.setOnClickListener(this);
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        quitDate.setText(mDay+"/"+(mMonth+1+"/"+mYear));
    }
    @Override
    public void onItemSelected (AdapterView < ? > parent, View view,int position, long id){

    }

    @Override
    public void onNothingSelected (AdapterView < ? > parent){

    }
    @Override
    public void onClick(View v) {

        if (v == quitDate || v == quitDateInputLayout) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                    //6.4 Set the date on the EditText variable
                    quitDate.setText(dayOfMonth + "/" + (month + 1 + "/" + year));
                }
            }, mYear, mMonth, mDay);
            //6.5 Show the date picker dialog
            datePickerDialog.show();
        } else if (v == submitSobrietyDetails) {
//              int unitsConsumed=Integer.parseInt(Objects.requireNonNull(dailyConsumedUnits.getText()).toString().trim());
            String startDate=quitDate.getText().toString().trim();
            String unitsConsumed = dailyConsumedUnits.getText().toString().trim();
            int dailyConsumption=Integer.parseInt(unitsConsumed);
            String unitPrice = unitCost.getText().toString().trim();
            int unitNetPrice=Integer.parseInt(unitPrice);
            String emergencyContactNumber = emergencyContact.getText().toString().trim();
            String daysDrunk= drinkingDays.getText().toString() .trim();
            int daysOfWeekDrunk=Integer.parseInt(daysDrunk);

            if (TextUtils.isEmpty(unitsConsumed)) {
                dailyConsumedUnitsInputLayout.setError("please input daily consumption");
            }else if(TextUtils.isEmpty(startDate)){
                drinkingDaysInputLayout.setError("please input start date");
            }
            else if(TextUtils.isEmpty(daysDrunk)){
                drinkingDaysInputLayout.setError("please input drinking days");
            }else if(daysOfWeekDrunk<=0){
                drinkingDaysInputLayout.setError("Days drunk must be more than zero");
            } else if(daysOfWeekDrunk>7){
                drinkingDaysInputLayout.setError("weekly drinking days cannot be more than seven");
            }
            else if(dailyConsumption<=0){
                dailyConsumedUnitsInputLayout.setError("Units consumed must be more than zero");
            }else if (TextUtils.isEmpty(unitPrice)) {
                unitCostInputLayout.setError("please input price of each unit");
            }else if(unitNetPrice<=0){
                drinkingDaysInputLayout.setError("Unit price must be more than zero");
            } else if (TextUtils.isEmpty(emergencyContactNumber)) {
                emergencyContactInputLayout.setError("please input emergency contact details");
            } else if (emergencyContactNumber.length() < 10) {
                emergencyContactInputLayout.setError("phone number should have 10 digits");
            } else {
                String userId = mAuth.getUid();
                DocumentReference docRef = fStore.collection("users").document(userId);
                docRef
                        .update(
                                "soberDateSet", true,
                                "emergencyContact",emergencyContactNumber,
                                "unitPrice",unitNetPrice,
                                "startDate",startDate,
                                "unitsConsumed",dailyConsumption,
                                "daysOfWeekDrunk",daysOfWeekDrunk
                        )

                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(SobrietyDetails.this, "User details updated", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SobrietyDetails.this,MainActivity.class));
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

        }


    }
}