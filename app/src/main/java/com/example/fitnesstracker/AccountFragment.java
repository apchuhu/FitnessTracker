package com.example.fitnesstracker;

import android.content.Context;
import static android.content.ContentValues.TAG;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    TextView textView;
    Button button2;

    // key for storing and retrieving the theme from shared preference object
    private static final String PREF_SELECTED_THEME = "selected_theme";

    // Theme IDs
    private static final int THEME_LOGIN = R.style.Theme_Login;
    private static final int THEME_FITNESS_TRACKER = R.style.Base_Theme_FitnessTracker;

    //boolean to make isThemeLogin true that way it starts at Theme.Login first
    private boolean isThemeLogin = true;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        // call applySelectedTheme to set theme to the other theme
        applySelectedTheme();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        button = view.findViewById(R.id.logout);
        textView = view.findViewById(R.id.user_Details);
        button2 = view.findViewById(R.id.theme);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
      
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTheme();
            }
        });
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
                        textView.setText("Hello " + thisUser);
                    } else {
                        // User does not exist in the database
                        thisUser = "Guest";
                        textView.setText("Hello " + thisUser);
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
            textView.setText("Progress for " + thisUser);
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

    // method to toggle between Login theme and Fitness Tracker dark theme
    private void toggleTheme() {
        int newTheme;
        if (isThemeLogin) {
            newTheme = THEME_FITNESS_TRACKER;
        } else {
            newTheme = THEME_LOGIN;
        }

        // This stores the theme into a shared preference for it to change to the new theme easier
        SharedPreferences preferences = requireContext().getSharedPreferences(
                requireContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        preferences.edit()
                .putInt(PREF_SELECTED_THEME, newTheme)
                .apply();
        applySelectedTheme();

        // reloads the "Activity" (Page) to apply the new theme
        requireActivity().recreate();

        // updates boolean in the instance variable to track which theme is on
        isThemeLogin = !isThemeLogin;
    }


    //method to apply the theme to the page
    private void applySelectedTheme() {
        // get the selected theme from shared preference object, it automatically defaults to the light theme
        SharedPreferences preferences = requireContext().getSharedPreferences(
                requireContext().getPackageName() + "_preferences", Context.MODE_PRIVATE);
        int selectedTheme = preferences.getInt(PREF_SELECTED_THEME, THEME_LOGIN);

        // sets the theme
        requireContext().getTheme().applyStyle(selectedTheme, true);

        // update the boolean to track the current theme
        isThemeLogin = selectedTheme == THEME_LOGIN;
    }

}