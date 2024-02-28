package com.example.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fitnesstracker.databinding.ActivityMainBinding;
import com.example.fitnesstracker.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    ActivityMainBinding binding;

    private SensorManager sensManager = null;
    private Sensor stepSensor;
    private int totalSteps = 0;
    private int preTotalSteps = 0;
    private ProgressBar progressBar;
    private TextView stepText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            }
            else if (itemId == R.id.track) {
                replaceFragment(new TrackFragment());
            }
            else if (itemId == R.id.plan) {
                replaceFragment(new PlanFragment());
            }
            else if (itemId == R.id.steps){
                replaceFragment(new StepsFragment());
            }
            return true;
        });

        progressBar = findViewById(R.id.progressBar);
        stepText = findViewById(R.id.stepsText);

        resetSteps();
        loadData();
        sensManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        stepSensor = sensManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }


    protected void onResume(){
        super.onResume();

        if(stepSensor == null){
            Toast.makeText(this, "Device has no sensor", Toast.LENGTH_SHORT).show();
        }
        else {
            sensManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    protected void onPause(){
        super.onPause();
        sensManager.unregisterListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            totalSteps = (int) event.values[0];
            int currentSteps = totalSteps - preTotalSteps;
            stepText.setText(String.valueOf(currentSteps));

            progressBar.setProgress(currentSteps);
        }
    }

    //Mainly for testing
    private void resetSteps(){
        if (stepText != null) {
            stepText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "long hold to rest", Toast.LENGTH_SHORT).show();
                }
            });

            stepText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    preTotalSteps = totalSteps;
                    stepText.setText("0");
                    progressBar.setProgress(0);
                    saveData();
                    return true;
                }
            });
        } else {
            // Log an error or display a message to help identify the issue
            Toast.makeText(this, "stepsText is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveData(){
        SharedPreferences sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("key1", String.valueOf(preTotalSteps));
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE);
        int savedNum = (int) sharedPref.getFloat("key1", 0f);
        preTotalSteps = savedNum;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}