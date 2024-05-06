package com.example.fitnesstracker;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class AccountFragment extends Fragment {

    FirebaseAuth auth;
    Button button;
    TextView textView;
    Button button2;

    // Key for storing and retrieving the selected theme from SharedPreferences
    private static final String PREF_SELECTED_THEME = "selected_theme";

    // Theme IDs
    private static final int THEME_LOGIN = R.style.Theme_Login;
    private static final int THEME_FITNESS_TRACKER = R.style.Base_Theme_FitnessTracker;

    private boolean isThemeLogin = true; // Flag to track the current theme

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        // Apply the selected theme when the fragment is created or resumed
        applySelectedTheme();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        button = view.findViewById(R.id.logout);
        textView = view.findViewById(R.id.user_Details);
        button2 = view.findViewById(R.id.theme);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call method to change the theme
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

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        } else {
            textView.setText(user.getEmail());
        }
    }

    // Method to toggle between themes
    private void toggleTheme() {
        int newTheme;
        if (isThemeLogin) {
            newTheme = THEME_FITNESS_TRACKER; // Switch to Fitness Tracker theme
        } else {
            newTheme = THEME_LOGIN; // Switch back to Login theme
        }

        // Save the selected theme to SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putInt(PREF_SELECTED_THEME, newTheme)
                .apply();

        // Apply the selected theme
        applySelectedTheme();

        // Recreate the current activity to apply the new theme (optional)
        requireActivity().recreate();

        // Update the flag to track the current theme
        isThemeLogin = !isThemeLogin;
    }

    // Method to apply the selected theme
    private void applySelectedTheme() {
        // Get the selected theme from SharedPreferences, defaulting to the light theme
        int selectedTheme = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getInt(PREF_SELECTED_THEME, THEME_LOGIN); // Default to Theme.Login

        // Set the theme for the fragment
        requireContext().getTheme().applyStyle(selectedTheme, true);

        // Update the flag to track the current theme
        isThemeLogin = selectedTheme == THEME_LOGIN;
    }
}