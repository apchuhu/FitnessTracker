package com.example.fitnesstracker;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StepsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StepsFragment extends Fragment implements SensorEventListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int mTotalSteps = 0;
    private int mPreTotalSteps = 0;
    String thisUser;
    private ProgressBar mProgressBar;
    private TextView mStepText;
    private SensorManager mSensManager;
    private Sensor mStepSensor;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView userText;
    private DatabaseReference mStepsRef;
    private String lastDate = "";

    public StepsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StepsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StepsFragment newInstance(String param1, String param2) {
        StepsFragment fragment = new StepsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_steps, container, false);
        mProgressBar = view.findViewById(R.id.progressBar);
        mStepText = view.findViewById(R.id.stepsText);
        userText = view.findViewById(R.id.user_Details);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        userText.setText(checkForDBUser());
        resetSteps();
        loadData();
        mSensManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        mStepSensor = mSensManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepsRef = FirebaseDatabase.getInstance().getReference().child("steps");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStepSensor == null) {
            Toast.makeText(requireContext(), "Device has no sensor", Toast.LENGTH_SHORT).show();
        } else {
            mSensManager.registerListener(this, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensManager.unregisterListener(this);
    }

    private void resetSteps() {
        if (mStepText != null) {
            mStepText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(requireContext(), "long hold to rest", Toast.LENGTH_SHORT).show();
                }
            });

            mStepText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mPreTotalSteps = mTotalSteps;
                    mStepText.setText("0");
                    mProgressBar.setProgress(0);
                    saveData();
                    return true;
                }
            });
        } else {
            // Log an error or display a message to help identify the issue
            Toast.makeText(requireContext(), "stepsText is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveData() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("key1", String.valueOf(mPreTotalSteps));
        editor.apply();


    }

//    private void loadData() {
//        if (mAuth.getCurrentUser() != null) {
//            String currentDate = getCurrentDate();
//            if (currentDate != null){
//                mDatabase.child(mAuth.getCurrentUser().getUid()).child(currentDate).child("steps").setValue(0);
//            }else{
//            mDatabase.child(mAuth.getCurrentUser().getUid()).child(currentDate).child("steps")
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if (snapshot.exists()) {
//                                Long savedSteps = snapshot.getValue(Long.class);
//                                mPreTotalSteps = savedSteps.intValue();
//                                mStepText.setText(String.valueOf(mTotalSteps - mPreTotalSteps));
//                                mProgressBar.setProgress(mTotalSteps - mPreTotalSteps);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                            Log.e(TAG, "Failed to read value.", error.toException());
//                        }
//                    });
//            }
//        }
//    }

    private void loadData() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        String savedNum = sharedPref.getString("key1", "0");
        mPreTotalSteps = Integer.parseInt(savedNum);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            mTotalSteps = (int) event.values[0];
            int currentSteps = mTotalSteps - mPreTotalSteps;
            mStepText.setText(String.valueOf(currentSteps));
            mProgressBar.setProgress(currentSteps);

            // Get the current date
            String currentDate = getCurrentDate();

            // Check if the current date has changed from the last recorded date
            if (!currentDate.equals(lastDate)) {
                // Reset the step counter to 0
                mPreTotalSteps = mTotalSteps;
                // Update the last recorded date
                lastDate = currentDate;
                // Save the last recorded date in SharedPreferences
                saveLastDate();
            }

            // Save the steps data under the current user and date
            if (mAuth.getCurrentUser() != null) {
                mDatabase.child(mAuth.getCurrentUser().getUid()).child(currentDate).child("steps").setValue(currentSteps);
            }
        }
    }

    // Method to save the last recorded date in SharedPreferences
    private void saveLastDate() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("lastDate", lastDate);
        editor.apply();
    }

    // Method to load the last recorded date from SharedPreferences
    private void loadLastDate() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        lastDate = sharedPref.getString("lastDate", "");
    }

    // Method to get the current date in the format MM-dd-yy
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public int getmTotalSteps() {
        return mTotalSteps;
    }

    public int getmPreTotalSteps() {
        return mPreTotalSteps;
    }

    // Method should find one user id in the database
    // if the user is found return the users information
    // if the user is not found return not logged in.
    public String checkForDBUser() {

        if (mAuth.getCurrentUser() != null) {
            mDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // User exists in the database
                        // Assuming you have a field called "name" in your user data
                        thisUser = snapshot.child("Username").getValue(String.class);
                        userText.setText("Progress for " + thisUser);
                    } else {
                        // User does not exist in the database
                        thisUser = "Guest";
                        userText.setText("Progress for " + thisUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Getting user data failed, log a message
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        } else {
            // User is not authenticated
            thisUser = "Guest";
            userText.setText("Progress for " + thisUser);
        }

        return "Progress for " + thisUser;
    }
}