package com.forever;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private EditText firstName, lastName, birthDate;
    private Button confirmButton, backButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mTruckDatabase, mCarDatabase;
    private String userID;
    private String mFirstName, mLastName, mBirthDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        birthDate = findViewById(R.id.birthDate);

        confirmButton = findViewById(R.id.confirmButton);
        backButton = findViewById(R.id.backButton);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mTruckDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Trucks").child(userID);

        getUserInfo();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }

    private void getUserInfo(){
        mTruckDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("firstName") != null){
                        mFirstName = map.get("firstName").toString();
                        firstName.setText(mFirstName);
                    }
                    if(map.get("lastName") != null){
                        mLastName = map.get("lastName").toString();
                        lastName.setText(mLastName);
                    }
                    if(map.get("birthDate") != null){
                        mBirthDate = map.get("birthDate").toString();
                        birthDate.setText(mBirthDate);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void saveUserInformation() {
        mFirstName = firstName.getText().toString();
        mLastName = lastName.getText().toString();
        mBirthDate = birthDate.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("firstName", mFirstName);
        userInfo.put("lastName", mLastName);
        userInfo.put("birthDate", mBirthDate);
        mTruckDatabase.updateChildren(userInfo);
        finish();
    }
}