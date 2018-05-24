package com.forever;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button mTruck, mCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTruck = findViewById(R.id.truck);
        mCar = findViewById(R.id.car);

        mTruck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TruckLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CarLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
}



