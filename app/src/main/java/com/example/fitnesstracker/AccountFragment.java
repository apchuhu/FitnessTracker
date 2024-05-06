package com.example.fitnesstracker;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {

    String thisUser;

    FirebaseAuth auth;
    DatabaseReference mDatabase;

    Button button;
    TextView userText;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        button = view.findViewById(R.id.logout);
        userText = view.findViewById(R.id.user_Details);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    public String checkForDBUser() {

        if (auth.getCurrentUser() != null) {
            mDatabase.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // User exists in the database
                        // Assuming you have a field called "name" in your user data
                        thisUser = snapshot.child("Username").getValue(String.class);
                        userText.setText("Hello " + thisUser);
                    } else {
                        // User does not exist in the database
                        thisUser = "Guest";
                        userText.setText("Hello " + thisUser);
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

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        } else {
            checkForDBUser();
        }
    }
}