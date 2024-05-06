package com.example.fitnesstracker;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int thisUserSteps;
    private ProgressBar bar;

    private DatabaseReference dbStepsRef;
    private FirebaseAuth userAuth;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        bar = view.findViewById(R.id.progressBar2);
        userAuth = FirebaseAuth.getInstance();
        dbStepsRef = FirebaseDatabase.getInstance().getReference().child("users").child(userAuth.getCurrentUser().getUid());
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        StepsFragment steps = new StepsFragment();
//        bar.setProgress(steps.getmTotalSteps() - steps.getmPreTotalSteps());

        if(userAuth.getCurrentUser() != null){
            dbStepsRef.child(userAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    thisUserSteps = snapshot.child("steps").getValue(Integer.class);
                    bar.setProgress(thisUserSteps);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Getting user data failed, log a message
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }else{
            bar.setProgress(0);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    //    public void updateProgress(){
//        StepsFragment steps = new StepsFragment();
//        bar.setProgress(steps.getmTotalSteps() - steps.getmPreTotalSteps());
//    }

}